package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";

    private DemographicsFragment demographicsFragment;
    private HousingFragment housingFragment;

    private Location mLastLocation;
    private GeoCoder gc;
    private CBSAPI api;
    private Buurt currentLocation;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("BuurtStats");

        loadState();

        gc = new GeoCoder(this);
        api = new CBSAPI(this);

        Authenticator.getInstance().createServices(this);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawer = (ListView) findViewById(R.id.navigation_drawer);
        FrameLayout fragmentContainer = (FrameLayout) findViewById(R.id.stats_container);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        abdt.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(abdt);
        abdt.syncState();

        String[] pages = {"Bevolking", "Wonen"};
        drawer.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_adapter, pages));

        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPage = position;
                updateFragment();
                drawerLayout.closeDrawers();
            }
        });

        demographicsFragment = new DemographicsFragment();
        housingFragment = new HousingFragment();
        getFragmentManager().beginTransaction().add(R.id.stats_container, demographicsFragment).commit();
        getFragmentManager().beginTransaction().add(R.id.stats_container, housingFragment).commit();

        updateDisplay();
        updateFragment();
    }

    private void updateFragment() {
        switch(currentPage) {
            case 0: getFragmentManager().beginTransaction().show(demographicsFragment).commit();
                getFragmentManager().beginTransaction().hide(housingFragment).commit();
                break;
            case 1: getFragmentManager().beginTransaction().hide(demographicsFragment).commit();
                getFragmentManager().beginTransaction().show(housingFragment).commit();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        Authenticator.getInstance().connectServices();
        Authenticator.getInstance().restoreSignIn();
        updateDisplay();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("OnConnected");
        LocationRequest locReq = LocationRequest.create();
        locReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locReq.setInterval(100);

        try { //todo: request permission
            LocationServices.FusedLocationApi.requestLocationUpdates(Authenticator.getInstance().getApiClient(), locReq, this);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("Saving state");
        outState.putSerializable("currentLocation", currentLocation);
        outState.putParcelable("location", mLastLocation);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        System.out.println("Restoring state");
        currentLocation = (Buurt) savedInstanceState.getSerializable("currentLocation");
        mLastLocation = savedInstanceState.getParcelable("location");
        updateDisplay();

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Authenticator.getInstance().disconnectServices();
        saveState();
    }

    private void saveState() {
        SharedPreferences.Editor editor = getSharedPreferences("storage", MODE_PRIVATE).edit();
        editor.putInt("currentPage", currentPage);
        editor.apply();

        try {
            FileOutputStream fos = this.openFileOutput("buurt", MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(currentLocation);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("storage", MODE_PRIVATE);
        currentPage = prefs.getInt("currentPage", 0);

        try {
            FileInputStream fis = this.openFileInput("buurt");
            ObjectInputStream is = new ObjectInputStream(fis);
            currentLocation = (Buurt) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Google API connection failed");
        System.out.println(connectionResult.getErrorCode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
//        location = new Location("");
//        location.setLatitude(52.3544d);
//        location.setLongitude(4.955d);

        double distance;
        if(mLastLocation != null) {
            distance = Math.sqrt(Math.pow(mLastLocation.getLatitude() - location.getLatitude(), 2) +
                    Math.pow(mLastLocation.getLongitude() - location.getLongitude(), 2));
        } else {
            distance = 1;
        }

        if(distance > 0.0001) {
            mLastLocation = location;

            //gc.requestBuurtID(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            new APIManager().getBuurtfromLocation(location, new APIListener() {
                @Override
                public void onAPIResult(JSONObject result) {
                    demographicsFragment.setData(result);
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onDBResult(Map<String, String> id, String name) {
        if(currentLocation == null || !currentLocation.getName().equals(name)) {
            currentLocation = new Buurt(id, name);
            api.getData(currentLocation);
        }
    }

    public void updateDisplay() {
        if(currentLocation != null) {
            setTitle(currentLocation.getName());
            //demographicsFragment.setData(currentLocation);
        }

    }
}

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

import org.json.JSONException;
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
    private int currentPage;

    private Location lastLocation;
    private JSONObject currentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("BuurtStats");

        Authenticator.getInstance().createServices(this);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawer = (ListView) findViewById(R.id.navigation_drawer);

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

        loadState();
        Authenticator.getInstance().connectServices();
        Authenticator.getInstance().restoreSignIn();
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
    protected void onStop() {
        Authenticator.getInstance().disconnectServices();
        saveState();

        super.onStop();
    }

    private void saveState() {
        SharedPreferences.Editor editor = getSharedPreferences("storage", MODE_PRIVATE).edit();
        editor.putInt("currentPage", currentPage);
        editor.putString("data", currentData.toString());
        editor.apply();

    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("storage", MODE_PRIVATE);
        currentPage = prefs.getInt("currentPage", 0);

        try {
            currentData = new JSONObject(prefs.getString("data", null));
            updateData();
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
        if(lastLocation != null) {
            distance = Math.sqrt(Math.pow(lastLocation.getLatitude() - location.getLatitude(), 2) +
                    Math.pow(lastLocation.getLongitude() - location.getLongitude(), 2));
        } else {
            distance = 1;
        }

        if(distance > 0.0001) {
            lastLocation = location;

            new APIManager().getBuurtfromLocation(location, new APIListener() {
                @Override
                public void onAPIResult(JSONObject result) {
                    currentData = result;
                    demographicsFragment.setData(result);
                    updateData();
                }
            });
        }
    }

    private void updateData() {
        if(currentData != null) {
            setTitle("Bob");
            demographicsFragment.setData(currentData);
            //housingFragment.setData(currentData);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

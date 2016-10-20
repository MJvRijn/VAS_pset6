package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";

    private DemographicsFragment demographicsFragment;

    private Location mLastLocation;
    private GeoCoder gc;
    private CBSAPI api;
    private Buurt currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("BuurtStats");

        gc = new GeoCoder(this);
        api = new CBSAPI(this);

        Authenticator.getInstance().createServices(this);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawer = (ListView) findViewById(R.id.navigation_drawer);
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

        demographicsFragment = new DemographicsFragment();
        getFragmentManager().beginTransaction().replace(R.id.stats_container, demographicsFragment).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        Authenticator.getInstance().connectServices();
        Authenticator.getInstance().signIn(this);
        updateDisplay();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        if(mLastLocation == null) {
            mLastLocation = location;
            gc.requestBuurtID(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else {
            double distance = Math.sqrt(Math.pow(mLastLocation.getLatitude() - location.getLatitude(), 2) +
                    Math.pow(mLastLocation.getLongitude() - location.getLongitude(), 2));

            System.out.println(distance);
            if(distance > 0.0001) {
                mLastLocation = location;

                gc.requestBuurtID(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onDBResult(String id, String name) {
        if(currentLocation == null || !currentLocation.getId().equals(id)) {
            currentLocation = new Buurt(id, name);
            api.getData(currentLocation);
        }
    }

    public void updateDisplay() {
        if(currentLocation != null) {
            setTitle(currentLocation.getName());
            demographicsFragment.setData(currentLocation);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Authenticator.getInstance().completeSignIn(requestCode, resultCode, data, this);
    }
}

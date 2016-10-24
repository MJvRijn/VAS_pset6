package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";

    private StatsFragment[] fragments;
    private int currentFragment;

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
                currentFragment = position;
                updateFragment();
                drawerLayout.closeDrawers();
            }
        });

        fragments = new StatsFragment[2];
        fragments[0] = new DemographicsFragment();
        fragments[1] = new HousingFragment();

        FragmentManager fm = getFragmentManager();
        for(StatsFragment f : fragments) {
            fm.beginTransaction().add(R.id.stats_container, f).commit();
        }
    }

    private void updateFragment() {
        FragmentManager fm = getFragmentManager();

        for(int i = 0; i < fragments.length; i++) {
            if(i == currentFragment) {
                fm.beginTransaction().show(fragments[i]).commit();
            } else {
                fm.beginTransaction().hide(fragments[i]).commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadState();
        updateFragment();
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
        editor.putInt("currentFragment", currentFragment);
        editor.putFloat("currentLatitude", (float) lastLocation.getLatitude());
        editor.putFloat("currentLongitude", (float) lastLocation.getLongitude());
        editor.putString("data", currentData.toString());
        editor.apply();
    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("storage", MODE_PRIVATE);
        currentFragment = prefs.getInt("currentFragment", 0);

        float lat = prefs.getFloat("currentLatitude", 52.3f);
        float lon = prefs.getFloat("currentLongitude", 4.7f);

        Location l = new Location("");
        l.setLatitude(lat);
        l.setLongitude(lon);
        lastLocation = l;

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
                    updateData();
                }
            });
        }
    }

    private void updateData() {
        if(currentData != null) {
            try {
                String title = String.format("%s, %s", currentData.getString("name"), currentData.getString("city"));
                setTitle(title);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(StatsFragment f : fragments) {
                f.setData(currentData);
            }
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

package nl.mjvrijn.matthewvanrijn_pset6;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

/* MainActivity
 *
 * MainActivity is the class of the primary application activity. This activity shows the user all
 * the neighbourhood stats. It has a drawer to select different categories, which are displayed as
 * fragments in a frame. The menu bar has a menu button and a settings button. */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final double UPDATE_MIN_DISTANCE = 0.005;

    private GoogleApiClient apiClient;

    // State variables
    private StatsFragment[] fragments;
    private int currentFragment;
    private Location lastLocation;
    private JSONObject currentData;

    /** App initialisation methods (called once) **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpDrawer(toolbar);
        setUpFragments();
        setUpLocationServices();
    }

    /* Add the settings button to the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Make the app go to the settings activity when the settings button is pressed. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Set up the drawer, which involves creating a listener to listen for drawer events, an adapter
     * to handle the drawer menu and a listener to listen for menu clicks.
     */
    private void setUpDrawer(Toolbar t) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawer = (ListView) findViewById(R.id.navigation_drawer);

        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, drawerLayout, t,
                R.string.open_drawer, R.string.close_drawer) {
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

        drawer.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_adapter,
                new String[]{"Bevolking", "Wonen", "Geld", "Overig"}));

        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentFragment = position;
                updateFragments();
                drawerLayout.closeDrawers();
            }
        });
    }

    /* Set up the fragments. Every category of stats has a separate fragment. This function creates
     * them and adds them to the frame.
     */
    private void setUpFragments() {
        fragments = new StatsFragment[]{new DemographicsFragment(), new HousingFragment(),
                new MoneyFragment(), new MiscFragment()};

        FragmentManager fm = getFragmentManager();
        for(StatsFragment f : fragments) {
            fm.beginTransaction().add(R.id.stats_container, f).commit();
        }
    }

    /* Set up the location services. The location service is a google service, so it requires
     * a connection to the google api to be made. On a successful connection location updates
     * will be requested from the API.
     */
    private void setUpLocationServices() {
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.w(TAG, "Connection to google location API failed.");
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "Connected to google location API.");
                        requestLocationUpdates();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.w(TAG, "Connection to google location API suspended.");
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    /* Ask the google location API to provide periodic location updates, so that the app can update
     * the stats if the user moves to a new neighbourhood. A listener is declared to listen for these.
     */
    private void requestLocationUpdates() {
        LocationRequest locReq = LocationRequest.create();
        locReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locReq.setInterval(100);

        if(checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locReq, new LocationListener() {
                @Override
                public void onLocationChanged(Location l) {
                    processLocationUpdate(l);
                }
            });
        }
    }

    /* In Android 6.0+, apps require explicit user permission to access the location. This method
     * checks whether that permission has been granted and, if not, requests is. The result of the
     * request is provided in the callback below. */
    private boolean checkPermission() {
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        } else {
            return true;
        }
    }

    /* This method is the callback from the location permission request. If the user has provided
    *  the permission the app will request the location updates that it could not before.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            Toast.makeText(this, getResources().getString(R.string.loc_permission_refused), Toast.LENGTH_LONG).show();
        }
    }

    /** Update methods (called multiple times) **/

    /* Update the fragment visibility to reflect the currently selected page. This method will
     * iterate all fragments and hide all except the selected one.
     */
    private void updateFragments() {
        FragmentManager fm = getFragmentManager();

        for(int i = 0; i < fragments.length; i++) {
            if(i == currentFragment) {
                fm.beginTransaction().show(fragments[i]).commit();
            } else {
                fm.beginTransaction().hide(fragments[i]).commit();
            }
        }
    }

    /* Update the toolbar title to that of the current neighbourhood and set the data fro all
     * fragments.
     */
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

    /* On a location update, request new data from the API if the user has moved a minimum distance.
     * A listener listens for the API result and applies it.
     */
    private void processLocationUpdate(Location l) {
        double distance;

        // Calculate the distance if a previous location is known
        if(lastLocation != null) {
            distance = Math.sqrt(Math.pow(lastLocation.getLatitude() - l.getLatitude(), 2) +
                    Math.pow(lastLocation.getLongitude() - l.getLongitude(), 2));
        } else {
            distance = UPDATE_MIN_DISTANCE;
        }

        if(distance >= UPDATE_MIN_DISTANCE) {
            lastLocation = l;

            new APIManager().getStatsfromLocation(l, new APIListener() {
                @Override
                public void onAPIResult(JSONObject result) {
                    currentData = result;
                    updateData();
                }
            });
        }
    }

    /** State restoration **/

    /* Save the current location, selected fragment index and stats to local storage.
     */
    private void saveState() {
        if(currentData != null) {
            SharedPreferences.Editor editor = getSharedPreferences("storage", MODE_PRIVATE).edit();
            editor.putInt("currentFragment", currentFragment);
            editor.putFloat("currentLatitude", (float) lastLocation.getLatitude());
            editor.putFloat("currentLongitude", (float) lastLocation.getLongitude());
            editor.putString("data", currentData.toString());
            editor.apply();
        }
    }

    /* Read the previous location, selected fragment index and stats from local storage to the
     * variables.
     */
    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("storage", MODE_PRIVATE);
        currentFragment = prefs.getInt("currentFragment", 0);

        float lat = prefs.getFloat("currentLatitude", 52.3f);
        float lon = prefs.getFloat("currentLongitude", 4.7f);

        lastLocation = new Location("");
        lastLocation.setLatitude(lat);
        lastLocation.setLongitude(lon);

        try {
            currentData = new JSONObject(prefs.getString("data", null));
            updateData();
        } catch (Exception e) {
            Log.i(TAG, "No saved state to load.");
        }
    }

    /* Store and load state on acitivity start and stop. */
    @Override
    protected void onStart() {
        super.onStart();

        loadState();
        updateFragments();
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveState();
    }
}

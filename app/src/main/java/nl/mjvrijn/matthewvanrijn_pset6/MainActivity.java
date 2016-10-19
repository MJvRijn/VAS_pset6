package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";

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

        gc = new GeoCoder(this);
        api = new CBSAPI(this);

        Authenticator.getInstance().createServices(this);
    }


    @Override
    protected void onStart() {
        super.onStart();

        Authenticator.getInstance().connectServices();
        Authenticator.getInstance().signIn(this);
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
        }

        double distance = Math.sqrt(Math.pow(mLastLocation.getLatitude() - location.getLatitude(), 2) +
                Math.pow(mLastLocation.getLongitude() - location.getLongitude(), 2));

        System.out.println(distance);
        if(distance > 0.0001) {
            mLastLocation = location;

            gc.requestBuurtID(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Authenticator.getInstance().completeSignIn(requestCode, resultCode, data, this);
    }
}

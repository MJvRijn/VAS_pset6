package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/* SettingsActivity
 *
 * The SettingsActivity allows the user to change settings. The setting are synced between all of
 * the users' devices using Firebase. The user has to authenticate with google to unlock these
 * features. When a setting is changed it is immediately written to the Firebase real-time db.
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingsActivity";

    private GoogleApiClient apiClient;
    private GoogleSignInAccount acct;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthListener;

    private SignInButton signInButton;
    private Button signOutButton;
    private TextView textView;
    private CheckBox syncCheckBox;
    private Spinner gpsSpinner;
    private long gpsSpinnerPosition;

    private boolean settingsLoaded = false;

    /** Activity initialisation methods (called once) **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpServices();
        setUpListeners();
        updateSettings();
    }

    /* Set up the google services required to authenticate with google and firebase.
     */
    private void setUpServices() {
        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.w(TAG, "Connection to google services failed.");
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "Connected to google services.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.w(TAG, "Connection to google services suspended.");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Firebase
        fbAuth = FirebaseAuth.getInstance();

        fbAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Signed into Firebase as: " + user.getDisplayName());
                } else {
                    Log.d(TAG, "Signed out of Firebase");
                }
            }
        };
    }

    /* This method sets the click listeners for all objects in the settings activity. For most
     * object the listener is this activity, but since the spinner uses an adapter it requires
     * a different listener.
     */
    private void setUpListeners() {
        signInButton = (SignInButton) findViewById(R.id.settings_sign_in);
        signOutButton = (Button) findViewById(R.id.settings_sign_out);
        syncCheckBox = (CheckBox) findViewById(R.id.sync_setting) ;
        textView = (TextView) findViewById(R.id.settings_login_text);
        gpsSpinner = (Spinner) findViewById(R.id.loc_accuracy_select);

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        syncCheckBox.setOnClickListener(this);
        gpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Only write the setting if they have already been set to the values stored in
                // Firebase. This is an issue because for some reason onItemSelected is called during
                // initialisation without user input.
                if(settingsLoaded) {
                    gpsSpinnerPosition = position;
                    writeSettingsToFirebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /** Google sign-in flow functions **/

    /* Sign in with google by starting the Google sign in activity. When complete onActivityResult
     * will be called. Only works when not already logged in.
     */
    private void signInGoogle() {
        if(acct == null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
            startActivityForResult(signInIntent, 1);
        }
    }

    /* Process the results of the Google sign in. If the user is now logged in the app
     * will proceed to show the settings and connect to Firebase.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                acct = result.getSignInAccount();
                Log.i(TAG, "Signed into Google as: " + acct.getDisplayName());
                updateSettings();
                connectFirebase();
            }
        }
    }

    /* Attempt log in silently if the user has log in before or on another device. This can happen
     * instantly or be done asynchronously, at the discretion of the Google API.
     */
    public void restoreSignIn() {
        if(acct == null) {
            OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(apiClient);

            // Instant silent sign-in
            if(pendingResult.isDone()) {
                GoogleSignInResult result = pendingResult.get();
                if(result.isSuccess()) {
                    acct = result.getSignInAccount();
                    connectFirebase();
                    updateSettings();
                }
            // Asynchronous silent sign-in
            } else {
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        if(result.isSuccess()) {
                            acct = result.getSignInAccount();
                            connectFirebase();
                            updateSettings();
                        }
                    }
                });
            }
        }
    }

    /* Sign the user out of google. This will hide the settings when complete.
     */
    private void signOutGoogle() {
        Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(TAG, "Signed out of Google");
                acct = null;
                updateSettings();
            }
        });
    }

    /** Firebase functions **/

    /* Connect to Firebase using the user's google account as credentials.
     */
    private void connectFirebase() {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            readSettingsFromFirebase();
                        } else {
                            Log.w(TAG, "Firebase error: ", task.getException());
                        }
                    }
                });
    }

    /* Write the currently selected settings to the Firebase real-time database. Always update
     * the sync option, but only update the other settings if synchronisation is enabled.
     */
    private void writeSettingsToFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Long gps = gpsSpinnerPosition;
        Boolean syncSettings = syncCheckBox.isChecked();

        db.child("UserData").child(acct.getId()).child("sync").setValue(syncSettings);

        if(syncSettings) {
            db.child("UserData").child(acct.getId()).child("GPS").setValue(gps);
        }
    }

    /* Read the user's settings from the Firebase database.
     */
    private void readSettingsFromFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("UserData").child(acct.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gpsSpinnerPosition = (long) dataSnapshot.child("GPS").getValue();
                gpsSpinner.setSelection((int) gpsSpinnerPosition);

                syncCheckBox.setChecked((boolean) dataSnapshot.child("sync").getValue());

                settingsLoaded = true;
                updateSettings();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Update methods (called multiple times) **/

    /* Set the desired setting visibility and text for each of three states:
     * 1. Signed out: show no settings apart from the sign in button.
     * 2. Signed in but settings not loaded: only show sign out button.
     * 3. Signed in and settings loaded: show all settings.
     * */
    private void updateSettings() {
        if(acct == null) {
            // State 1
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            syncCheckBox.setVisibility(View.GONE);
            findViewById(R.id.gps_setting_layout).setVisibility(View.GONE);
            findViewById(R.id.settings_loading).setVisibility(View.GONE);
            textView.setText(getString(R.string.settings_sign_in_prompt));
        } else {
            // State 2 or 3
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            textView.setText(String.format(getString(R.string.settings_signed_in), acct.getDisplayName()));
            if(settingsLoaded) {
                // State 3
                syncCheckBox.setVisibility(View.VISIBLE);
                findViewById(R.id.gps_setting_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.settings_loading).setVisibility(View.GONE);
            } else {
                // State 2
                syncCheckBox.setVisibility(View.GONE);
                findViewById(R.id.gps_setting_layout).setVisibility(View.GONE);
                findViewById(R.id.settings_loading).setVisibility(View.VISIBLE);
            }
        }
    }

    /* The onclick function is called when an object is clicked and maps the click to the right
     * action.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.settings_sign_in:     signInGoogle();
                break;
            case R.id.settings_sign_out:    signOutGoogle();
                break;
            case R.id.sync_setting:         writeSettingsToFirebase();
                break;
        }
    }

    /** Activity Lifecycle **/

    /* When the activity is started set the firebase listener and attempt to sign in.
     */
    @Override
    protected void onStart() {
        super.onStart();

        fbAuth.addAuthStateListener(fbAuthListener);
        restoreSignIn();
    }

    /* When the activity is stopped remove the firebase listener.
     */
    @Override
    protected void onStop() {
        super.onStop();

        fbAuth.removeAuthStateListener(fbAuthListener);
    }
}

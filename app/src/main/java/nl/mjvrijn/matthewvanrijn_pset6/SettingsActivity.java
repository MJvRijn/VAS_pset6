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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Settings");

        createServices();

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
                if(settingsLoaded) {
                    gpsSpinnerPosition = position;
                    writeSettingsToFirebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();

        fbAuth.addAuthStateListener(fbAuthListener);
        restoreSignIn();
    }

    @Override
    protected void onStop() {
        super.onStop();

        fbAuth.removeAuthStateListener(fbAuthListener);
    }

    private void createServices() {
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

    private void signInGoogle() {
        if(acct == null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
            startActivityForResult(signInIntent, 9001);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                acct = result.getSignInAccount();
                Log.i(TAG, "Signed into Google as: " + acct.getDisplayName());
                updateSettings();
                connectFirebase();

            } else {
                Log.d(TAG, "Failed to sign into Google");
            }
        }
    }

    private void connectFirebase() {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Firebase connection: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Firebase error: ", task.getException());
                        } else {
                            readSettingsFromFirebase();
                        }
                    }
                });
    }

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

    public void restoreSignIn() {
        if(acct == null) {
            OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(apiClient);

            if(pendingResult.isDone()) {
                GoogleSignInResult result = pendingResult.get();
                if(result.isSuccess()) {
                    acct = result.getSignInAccount();
                    System.out.println("Restored Google sign in as " + acct.getDisplayName() + " immediately.");
                    connectFirebase();
                    updateSettings();
                }
            } else {
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        if(result.isSuccess()) {
                            acct = result.getSignInAccount();
                            System.out.println("Restored google sign in as " + acct.getDisplayName() + " after wait.");
                            connectFirebase();
                            updateSettings();
                        }
                    }
                });
            }
        }
    }

    private void updateSettings() {
        if(acct == null) {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            syncCheckBox.setVisibility(View.GONE);
            findViewById(R.id.gps_setting_layout).setVisibility(View.GONE);
            findViewById(R.id.settings_loading).setVisibility(View.GONE);
            textView.setText("Please sign in to change settings");
        } else {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            textView.setText("Signed in as " + acct.getDisplayName());
            if(settingsLoaded) {
                syncCheckBox.setVisibility(View.VISIBLE);
                findViewById(R.id.gps_setting_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.settings_loading).setVisibility(View.GONE);
            } else {
                syncCheckBox.setVisibility(View.GONE);
                findViewById(R.id.gps_setting_layout).setVisibility(View.GONE);
                findViewById(R.id.settings_loading).setVisibility(View.VISIBLE);
            }
        }
    }

    private void writeSettingsToFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Long gps = gpsSpinnerPosition;
        Log.d(TAG, "pos:" + gps);
        Boolean syncSettings = syncCheckBox.isChecked();

        db.child("UserData").child(acct.getId()).child("sync").setValue(syncSettings);

        if(syncSettings) {
            db.child("UserData").child(acct.getId()).child("GPS").setValue(gps);
        }
    }

    private void readSettingsFromFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("UserData").child(acct.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "FB response");
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
}

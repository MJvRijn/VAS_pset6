package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by matthew on 19-10-16.
 */

public class Authenticator {
    private static final String TAG = "Authenticator";
    private static final int RC_SIGN_IN = 9001;

    private static Authenticator instance = new Authenticator();
    private GoogleApiClient apiClient;
    private GoogleSignInAccount acct;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthListener;

    private Authenticator() {
        fbAuth = FirebaseAuth.getInstance();

        fbAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public static Authenticator getInstance() {
        System.out.println(instance.toString());
        return instance;
    }

    public void signIn(Context c) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        ((MainActivity) c).startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void completeSignIn(int requestCode, int resultCode, Intent data, Context c) {
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Attempting to get result");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                acct = result.getSignInAccount();
                Log.d(TAG, "Logged into Google as: " + acct.getDisplayName());
                connectFirebase(c);

            } else {
                Log.d(TAG, "GSI failed");
            }
        }
    }

    public void signOut() {

    }

    public void createServices(Context c) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(c.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        apiClient = new GoogleApiClient.Builder(c)
                .enableAutoManage((MainActivity) c, (MainActivity) c)
                .addConnectionCallbacks((MainActivity) c)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void connectServices() {
        apiClient.connect();
        fbAuth.addAuthStateListener(fbAuthListener);
    }

    public void disconnectServices() {
        apiClient.disconnect();
        fbAuth.removeAuthStateListener(fbAuthListener);
    }

    private void connectFirebase(Context c) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener((MainActivity) c, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                        }
                    }
                });
    }

    public GoogleApiClient getApiClient() {
        return apiClient;
    }
}

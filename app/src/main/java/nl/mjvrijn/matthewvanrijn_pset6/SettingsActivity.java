package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, ResultCallback<Status> {
    private SignInButton signInButton;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Settings");

        signInButton = (SignInButton) findViewById(R.id.settings_sign_in);
        signOutButton = (Button) findViewById(R.id.settings_sign_out);

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);

        updateSignIn();
    }

    private void updateSignIn() {
        TextView textView = (TextView) findViewById(R.id.settings_login_text);
        SignInButton signInButton = (SignInButton) findViewById(R.id.settings_sign_in);
        Button signOutButton = (Button) findViewById(R.id.settings_sign_out);

        Authenticator auth = Authenticator.getInstance();

        if(auth.getAccount() == null) {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            textView.setText("Not signed in");
        } else {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            textView.setText("Signed in as " + auth.getAccount().getDisplayName());
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.settings_sign_in) {
            Authenticator.getInstance().signIn(this);
            updateSignIn();
        } else if(v.getId() == R.id.settings_sign_out) {
            Authenticator.getInstance().signOut(this);
            updateSignIn();
        }
    }

    // Sign out callback
    @Override
    public void onResult(@NonNull Status status) {
        updateSignIn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Authenticator.getInstance().completeSignIn(requestCode, resultCode, data, this);
    }
}

package br.com.mossteam.parada;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

public class SettingsActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    private void login() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook", loginResult.toString());
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "Login process cancelled by user.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Facebook", error.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        login();

        Profile profile = Profile.getCurrentProfile();
        if(profile != null) {
            ProfilePictureView pictureView = (ProfilePictureView) findViewById(R.id.user_profile_pic);
            pictureView.setProfileId(profile.getId());
            TextView textView = (TextView) findViewById(R.id.user_name);
            textView.setText(profile.getName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        Profile profile = Profile.getCurrentProfile();
        if(profile != null) {
            ProfilePictureView pictureView = (ProfilePictureView) findViewById(R.id.user_profile_pic);
            pictureView.setProfileId(profile.getId());
            TextView textView = (TextView) findViewById(R.id.user_name);
            textView.setText(profile.getName());
        }
    }

}

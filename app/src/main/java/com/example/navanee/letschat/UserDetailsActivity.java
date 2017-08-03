package com.example.navanee.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class UserDetailsActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener {

    String userID;
    RelativeLayout profileViewLayout;
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        getSupportActionBar().setLogo(R.drawable.chatsmall);
        profileViewLayout = (RelativeLayout) findViewById(R.id.userDetailsView);
        userID = getIntent().getExtras().getString("uid");
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        setUpFragment();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_signout) {
            /*Log.d("demo",lUser.getProviderData().toString());
            Log.d("demo",lUser.getProviderId());
            Log.d("demo",lUser.getProviders().toString());*/
            mAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {

                }
            });
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public void setUpFragment() {
        ProfileFragment pFragment = new ProfileFragment();
        pFragment.setmContext(UserDetailsActivity.this);
        pFragment.setSource("details");
        pFragment.setUserID(userID);
        getFragmentManager().beginTransaction()
                .add(R.id.userDetailsView,pFragment,"profileView")
                .commit();
    }

    @Override
    public ArrayList<User> onProfileFragmentInteraction() {
        return null;
    }

    @Override
    public void editSelectedUserProfile(User cUser) {
        ProfileEditFragment pEditFragment = new ProfileEditFragment();
        pEditFragment.setUser(cUser);
        pEditFragment.setmContext(UserDetailsActivity.this);
        getFragmentManager().beginTransaction()
                .replace(R.id.userDetailsView,pEditFragment,"editProfileView")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goBackToMain() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

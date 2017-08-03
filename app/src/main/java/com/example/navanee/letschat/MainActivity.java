package com.example.navanee.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UsersFragment.OnFragmentInteractionListener, InboxFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener {

    Toolbar toolbar;
    TabLayout tabLayout;
    RelativeLayout containerView;
    FirebaseUser lUser;
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        lUser = mAuth.getCurrentUser();
        getSupportActionBar().setLogo(R.drawable.chatsmall);
        loadViews();
        setTabbedpane();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setUpContent(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
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

    public void loadViews() {
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        containerView = (RelativeLayout) findViewById(R.id.container);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    public void setTabbedpane() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_inbox));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_profile));
        setUpContent("Users");
    }

    public void setUpContent(String tabText) {
        if(tabText.equals("Users")) {
            UsersFragment uFragment = new UsersFragment();
            uFragment.setmContext(MainActivity.this);
            uFragment.setReason("userProfile");
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,uFragment,"usersView")
                    .commit();
        } else if (tabText.equals("Inbox")) {
            InboxFragment iFragment = new InboxFragment();
            iFragment.setmContext(MainActivity.this);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,iFragment,"inboxView")
                    .commit();
        } else if (tabText.equals("Profile")) {
            FirebaseUser loggedInUser = mAuth.getCurrentUser();
            ProfileFragment pFragment = new ProfileFragment();
            pFragment.setmContext(MainActivity.this);
            pFragment.setSource("profile");
            pFragment.setUserID(loggedInUser.getUid());
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,pFragment,"profileView")
                    .commit();
        } else {
            Log.d("demo","Else");
        }
    }

    @Override
    public ArrayList<User> onInboxFragmentInteraction() {
        return null;
    }

    @Override
    public void addNewMessage() {
        Intent intent = new Intent(MainActivity.this,UserSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public ArrayList<User> onProfileFragmentInteraction() {
        return null;
    }

    @Override
    public void editSelectedUserProfile(User cUser) {
        ProfileEditFragment pEditFragment = new ProfileEditFragment();
        pEditFragment.setUser(cUser);
        pEditFragment.setmContext(MainActivity.this);
        getFragmentManager().beginTransaction()
                .replace(R.id.container,pEditFragment,"editProfileView")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goBackToMain() {

    }

    @Override
    public ArrayList<User> onUsersFragmentInteraction() {
        return null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

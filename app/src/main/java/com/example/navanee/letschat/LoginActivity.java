package com.example.navanee.letschat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class  LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    TextView emailField, passwordField;
    Button loginBtn, signUpBtn;
    private FirebaseAuth mAuth;
    GoogleSignInOptions gso;
    GoogleApiClient mGoogleApiClient;
    final int RC_SIGN_IN = 4;
    DatabaseReference mDatabase;
    SignInButton googleSignInButton;
    Boolean isExists = false;
    User user;

    CallbackManager mCallbackManager;
    LoginButton facebookLoginBtn;
    FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setLogo(R.drawable.chatsmall);
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni.isConnected()) {
            mAuth = FirebaseAuth.getInstance();
            try{
                PackageInfo info = getPackageManager().getPackageInfo(
                        "com.example.navanee.letschat", PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    //Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {

            } catch (NoSuchAlgorithmException e) {

            }
            loadAllViews();
            FirebaseApp.initializeApp(this);
        } else {
            Toast.makeText(LoginActivity.this, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
        }
    }

    public void loadAllViews() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailField = (TextView) findViewById(R.id.emailField);
        passwordField = (TextView) findViewById(R.id.passwordField);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(this);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        // Initialize Facebook Login button
        facebookLoginBtn = (LoginButton) findViewById(R.id.facebook_sign_in_button);
        facebookLoginBtn.setReadPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();

        facebookLoginBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.loginBtn) {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, R.string.toast_empty_values, Toast.LENGTH_LONG).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("test", "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("test", "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.toast_loginFailed, Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        }
                    });
            }
        } else if (v.getId() == R.id.signUpBtn) {
            Intent intent = new Intent(this,SignUpActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.google_sign_in_button) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    /*-------------------Sign In with Google-------------------*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        } else {
            Log.d("demo", "inside face book onActivity Result call back");
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

   private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        //Log.d("demo", "firebaseAuthWithGoogle:" + acct.getId());

        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            user = new User();
                            user.setEmail(acct.getEmail());
                            user.setFirstName(acct.getGivenName());
                            user.setLastName(acct.getFamilyName());
                            user.setProfileURL(acct.getPhotoUrl().toString());
                            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            user.setId(uid);
                            isExists = false;
                            mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if(ds.child("id").getValue().toString().equals(uid)) {
                                            isExists = true;
                                            break;
                                        }
                                    }
                                    if(!isExists) {
                                        mDatabase.child("users").child(uid).setValue(user);
                                        Toast.makeText(LoginActivity.this, R.string.toast_signup_success, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        // ...
                    }
                });
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
   }

   private void handleFacebookAccessToken(final AccessToken token) {
        //Log.d("demo", "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("demo","Success");
                    if (!task.isSuccessful()) {
                        Log.w("demo", "signInWithCredential", task.getException());
                        Log.d("dmeo","inside facebook authentication:failure");
                        Toast.makeText(LoginActivity.this, "Facebook Authentication failed.",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Log.d("demo","inside facebook authentication:success");
                        final User fUser = new User();
                        fUser.setId(mAuth.getCurrentUser().getUid());
                        fUser.setProfileURL(mAuth.getCurrentUser().getPhotoUrl().toString());
                        fUser.setFirstName(mAuth.getCurrentUser().getDisplayName().split(" ")[0]);
                        fUser.setLastName(mAuth.getCurrentUser().getDisplayName().split(" ")[1]);
                        fUser.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        isExists = false;
                        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if(ds.child("id").getValue().toString().equals(uid)) {
                                        isExists = true;
                                        break;
                                    }
                                }
                                if(!isExists) {
                                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(fUser);
                                    Toast.makeText(LoginActivity.this, R.string.toast_signup_success, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                }
            });
   }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}

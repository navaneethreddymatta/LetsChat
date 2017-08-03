package com.example.navanee.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    TextView firstNameField, lastNameField, emailField, passwordField, cPasswordField;
    Button signUpBtn, cancelBtn;
    RadioGroup genderGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle(R.string.app_name_signUp);
        getSupportActionBar().setLogo(R.drawable.chatsmall);
        loadAllViews();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public void loadAllViews() {
        firstNameField = (TextView) findViewById(R.id.firstNameField);
        lastNameField = (TextView) findViewById(R.id.lastNameField);
        emailField = (TextView) findViewById(R.id.emailField);
        passwordField = (TextView) findViewById(R.id.passwordField);
        cPasswordField = (TextView) findViewById(R.id.cPasswordField);
        genderGroup = (RadioGroup) findViewById(R.id.genderGroup);

        signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(this);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.signUpBtn) {
            final String firstName = firstNameField.getText().toString();
            final String lastName = lastNameField.getText().toString();
            final String email = emailField.getText().toString();
            final String password = passwordField.getText().toString();
            final String cPassword = cPasswordField.getText().toString();
            final int gIndex = genderGroup.indexOfChild(findViewById(genderGroup.getCheckedRadioButtonId()));
            if(email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || cPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, R.string.toast_signup_empty_values, Toast.LENGTH_LONG).show();
            } else if(gIndex == -1) {
                Toast.makeText(SignUpActivity.this, "Select the gender", Toast.LENGTH_LONG).show();
            } else if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, R.string.toast_signup_empty_values, Toast.LENGTH_LONG).show();
            } else if (!password.equals(cPassword)) {
                Toast.makeText(SignUpActivity.this, R.string.toast_signup_passwords_does_not_match, Toast.LENGTH_LONG).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("test", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, R.string.toast_signup_error, Toast.LENGTH_LONG).show();
                                } else {
                                    FirebaseUser fUser = mAuth.getCurrentUser();
                                    if (fUser != null) {
                                        User user = new User();
                                        String useruid = fUser.getUid();
                                        user.setId(useruid);
                                        user.setFirstName(firstName);
                                        user.setLastName(lastName);
                                        user.setEmail(email);
                                        if(gIndex == 0) {
                                            user.setGender("Male");
                                        } else {
                                            user.setGender("Female");
                                        }
                                        //user.setPassword(password);
                                        mDatabase.getReference().child("users").child(useruid).setValue(user);
                                        mAuth.signOut();
                                        Toast.makeText(SignUpActivity.this, R.string.toast_signup_success, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
            }
        } else if (v.getId() == R.id.cancelBtn) {
            finish();
        }
    }
}



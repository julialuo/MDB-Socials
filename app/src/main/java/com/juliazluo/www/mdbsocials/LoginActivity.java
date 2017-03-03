package com.juliazluo.www.mdbsocials;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_NAME = "LoginActivity";
    protected static final String REMEMBER_ME = "RememberMe";
    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initiate activity components
        mAuth = FirebaseAuth.getInstance();
        ((Button) findViewById(R.id.login_btn)).setOnClickListener(this);
        ((TextView) findViewById(R.id.to_signup)).setOnClickListener(this);
    }

    private void attemptLogin() {
        //Retrieve inputted email and password
        String email = ((EditText) findViewById(R.id.email_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.password_login)).getText().toString();

        if (!email.equals("") && !password.equals("")) {
            //Attempt to sign user in with Firebase authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(CLASS_NAME, "signInWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                //Notify user that authentication failed
                                Log.w(CLASS_NAME, "signInWithEmail:failed", task.getException());
                                Toast.makeText(LoginActivity.this, "Incorrect email or password, please try again",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                //Proceed to feed activity, passing on whether user wants to be remembered
                                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                boolean rememberMe = ((CheckBox) findViewById(R.id.remember_me)).isChecked();
                                intent.putExtra(REMEMBER_ME, rememberMe);
                                startActivity(intent);
                            }
                        }
                    });
        } else {
            //Email or password is empty
            Toast.makeText(LoginActivity.this, "Please enter an email and password",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                attemptLogin();
                break;
            case R.id.to_signup:
                //Proceed to sign up screen
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                break;
        }
    }
}

package com.example.liamc.lecturetrack;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Created by liamc on 21/07/2017.
 *
 * Class allows a user to sign in.
 * Uses FirebaseAuth to sign in.
 *
 */

public class LogInActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LogInActivity";

    private EditText emailEditText, passwordEditText;
    private Button logInButton, registerButton, logOutButton;
    private TextView logInInstructions, registerInstruction, userId;

    private FirebaseAuth mFireAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailEditText = (EditText) findViewById(R.id.email_address);
        passwordEditText = (EditText) findViewById(R.id.password);
        passwordEditText.setTypeface(Typeface.DEFAULT); // sets typeface of password hint text to default.
        logInInstructions = (TextView) findViewById(R.id.log_in_instructions);
        registerInstruction = (TextView) findViewById(R.id.register_instructions);
        userId = (TextView) findViewById(R.id.user_id);

        // initialise buttons
        logInButton = (Button) findViewById(R.id.log_in_button);
        registerButton = (Button) findViewById(R.id.register_button);
        logOutButton = (Button) findViewById(R.id.log_out_button);

        logInButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        logOutButton.setOnClickListener(this);

        mFireAuth = FirebaseAuth.getInstance();


    } // end of onCreate

    /**
     * onStart method
     */
    @Override
    protected void onStart() {
        super.onStart();

        // check if there is a user logged in
        FirebaseUser currentUser = mFireAuth.getCurrentUser();
        updateUI(currentUser);



    } // end of onStart

    /**
     * signIn method handles sign in using FirebaseAuth.
     * @param email String
     * @param password String
     */
    private void signIn(String email, String password) {
        mFireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "sign in with email/password: success");
                    Toast.makeText(LogInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser currentUser = mFireAuth.getCurrentUser();
                    updateUI(currentUser);
                } else {
                    try {
                        // if sign in fails, display message to user why
                        Log.w(TAG, "sign in with email/password: failed", task.getException());
                        Toast.makeText(LogInActivity.this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        updateUI(null);
                    } catch (NullPointerException ex) {
                        Toast.makeText(LogInActivity.this, "Sign in failed, unable to obtain reason.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    /**
     * Method calls to FirebaseAuth to sign out.
     */
    private void signOut() {
        mFireAuth.signOut();
        updateUI(null);
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
    }


    /**
     * Method validates ser input in EditText log ins.
     * Client side-validation before attempting log in, prevents unnecessary calling of Firebase side
     * validation.
     * @return valid boolean
     */
    private boolean validateInput() {
        // String pattern to reference emailEditText input against.
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+[a-z]";

        boolean valid = true;

        if (emailEditText.getText().toString().length() < 4 || passwordEditText.getText().toString().length() < 6) {
            valid = false;
            Toast.makeText(this, "Please enter full email and password", Toast.LENGTH_SHORT).show();
        }

        if (emailEditText.getText().toString().matches(emailPattern)) {
            valid = false;
            Toast.makeText(this, "Please enter valid email address", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        // check if there is a user logged in and update UI accordingly.
        if(user != null) {

            try {

                // if user is logged in, remove log-in form Views.
                logInInstructions.setText(R.string.registered_signed_in);
                userId.setText(user.getEmail().trim());
                registerInstruction.setVisibility(View.GONE);
                emailEditText.setVisibility(View.GONE);
                passwordEditText.setVisibility(View.GONE);
                logInButton.setVisibility(View.GONE);
                registerButton.setVisibility(View.GONE);

                // make log-out button visible
                logOutButton.setVisibility(View.VISIBLE);

            } catch (NullPointerException ex) {
                Log.e(TAG, "Unable to retrieve and display user's email", ex);
                Toast.makeText(this, "Oops, something went wrong.", Toast.LENGTH_SHORT).show();
            }


        } else {
            // if no user is logged in, make log-in form visible.

            logInInstructions.setVisibility(View.VISIBLE);
            registerInstruction.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            logInButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);


            userId.setVisibility(View.GONE);
            logInInstructions.setText(R.string.log_in_instructions);
            emailEditText.setText(null);
            passwordEditText.setText(null);


        }
    } // end of updateUI()

    /**
     * Implementation of onClick method from View.OnClickListener
     * Switches on View parameter to check which view was clicked by user and execute accordingly.
     * @param v View that was clicked
     */
    @Override
    public void onClick(View v) {

        // switch on View clicked by user and execute accordingly.

        try {
            switch (v.getId()) {
                case R.id.register_button:
                    startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
                    break;
                case R.id.log_in_button:
                    if (validateInput()) {
                        // call signIn method
                        signIn(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
                    }
                    break;
                case R.id.log_out_button:
                    if (mFireAuth.getCurrentUser() != null) {
                        signOut();
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception ex) {
            // Log error and display notification in Toast to user.
            Log.e(TAG, ex.getMessage());
            Toast.makeText(this, "Oops, did not recognise that click!", Toast.LENGTH_SHORT).show();
        }

    }


}

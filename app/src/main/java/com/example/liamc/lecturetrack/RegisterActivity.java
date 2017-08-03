package com.example.liamc.lecturetrack;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by liamc on 21/07/2017.
 *
 * Class for RegisterActivity.
 * Allows a user to register to log in with Staff/Student number, email address,
 * name and password.
 *
 * Class uses FirebaseAuth and FirebaseDatabase to register user and insert details into DB.
 *
 */

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Views to be initialised in onCreate that refer to Views in activity_register
    private EditText numberEditText, nameEditText, passwordEditText, emailAddressEditText, confirmPassword;
    private TextView registerState, userTextView;
    private Button registerButton;
    private Spinner userType;

    // Instance of FirebaseAuth, initialised in onCreate in order to use FirebaseAuth functionality.
    private FirebaseAuth mFireAuth;

    private DatabaseReference db;

    private String userTypeSelection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initialise View objects using references to Views in activity_register.xml
        numberEditText = (EditText) findViewById(R.id.staff_student_number);
        nameEditText = (EditText) findViewById(R.id.name);
        passwordEditText = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        emailAddressEditText = (EditText) findViewById(R.id.email_address);
        registerButton = (Button) findViewById(R.id.register_button);
        registerState = (TextView) findViewById(R.id.register_state);
        userTextView = (TextView) findViewById(R.id.user_id);

        // set typeface of password abd confirm password hints text to default.
        passwordEditText.setTypeface(Typeface.DEFAULT);
        confirmPassword.setTypeface(Typeface.DEFAULT);

        // initialise spinner and populate with values from string resources using ArrayAdapter
        userType = (Spinner) findViewById(R.id.staff_student_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type, android.R.layout.simple_spinner_item);
        // specify spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Populate userType spinner using values in ArrayAdapter.
        userType.setAdapter(adapter);

        // initialise FirebaseAuth
        mFireAuth = FirebaseAuth.getInstance();

        // initialise db
        db = FirebaseDatabase.getInstance().getReference();

        // set user type selection to null by default, will assign value in onItemSelected method
        // in OnItemSelectedListener in onStart method
        userTypeSelection = null;


    }

    /**
     * onStart method calls to super class method.
     * Sets up onClickListener for registerButton. Handler inserts user entered values into DB.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFireAuth.getCurrentUser();
        updateUI(currentUser);

        // set TextValidator listeners for each input field
        numberEditText.addTextChangedListener(new TextValidator(numberEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(numberEditText.getText().toString()))  {
                    Toast.makeText(RegisterActivity.this, "Please enter Staff/Student number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        nameEditText.addTextChangedListener(new TextValidator(nameEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(nameEditText.getText().toString()))  {
                    Toast.makeText(RegisterActivity.this, "Please enter name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        passwordEditText.addTextChangedListener(new TextValidator(passwordEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if ((text.equals(null))) {
                    Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        confirmPassword.addTextChangedListener(new TextValidator(confirmPassword) {
            @Override
            public void validate(TextView textView, String text) {
                if ((text.equals(null)))  {
                    Toast.makeText(RegisterActivity.this, "Please enter password in confirm password field", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emailAddressEditText.addTextChangedListener(new TextValidator(emailAddressEditText) {
            @Override
            public void validate(TextView textView, String text) {


                if ((text.equals(null)))  {
                    Toast.makeText(RegisterActivity.this, "Please enter email address", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // set up onClickListener and Handler for registerButton
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check input in all fields is valid
                if (passwordsMatch() && validateInput()) {
                    // if valid, calls createUser and passes email address and password
                    createUser(emailAddressEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
                } else {
                    Toast.makeText(RegisterActivity.this, "Could not register, please check input", Toast.LENGTH_SHORT).show();
                }

            }
        }); // end of registerButton onClickListener event handler

        userType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userTypeSelection = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    } // end of onStart



    /**
     * Method will create new user with email address and password.
     */
    private void createUser(String emailAddress, String password) {

        mFireAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // check if user has successfully been created
                            if (task.isSuccessful()) {
                                Log.d(TAG, "create user with email and password: successful");
                                Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mFireAuth.getCurrentUser();
                                updateUI(user);
                                insertUserDetailsToDB(user);

                                // check why unsuccessful and display to user.
                            } else {

                                try {
                                    // If task is not successful, user is not created.
                                    Log.w(TAG, "create user with email and password: failed", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Unsuccessful registration: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    // update UI that no user is signed in.
                                    updateUI(null);
                                } catch (NullPointerException ex) {
                                    Log.e(TAG, "Unable to display unsuccessful registration error message.", ex);
                                    Toast.makeText(RegisterActivity.this, "Unable to register, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    });



    } // END OF createUser METHOD.

    /**
     * Method is called by createUser if create user task is successful.
     * Switches on Spinner value selected: Staff/Student
     * Uses already validated user input in EditText fields to create Staff
     * or Student object and insert details into DB.
     */
    private void insertUserDetailsToDB(FirebaseUser user) {

        switch (userTypeSelection) {
            case "Student":
                try {
                    Student student = new Student(nameEditText.getText().toString().trim(),
                            emailAddressEditText.getText().toString(),
                            mFireAuth.getCurrentUser().getUid(),
                            numberEditText.getText().toString().trim());
                    // insert Student object values into DB.
                    db.child("students").child(user.getUid()).setValue(student);

                } catch (NullPointerException ex) {
                    // Log error
                    Log.e(TAG, "User details not inserted into DB: UID not retrieved." ,
                            ex);
                    // Notify user of DB error due to UID not being retrieved. Shouldn't happen if user is created successfully.
                    Toast.makeText(RegisterActivity.this, "Could not retrieve UID, user may not have been created successfully.",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.e(TAG, "User details not inserted into DB:" + ex.getMessage(), ex);
                }
                break;

            case "Staff":
                try {
                    Staff staff = new Staff(nameEditText.getText().toString().trim(),
                    emailAddressEditText.getText().toString(),
                    mFireAuth.getCurrentUser().getUid(),
                    numberEditText.getText().toString().trim());
                    // insert Student object values into DB.
                    db.child("staff").child((staff.getStaffNumber())).setValue(staff);

                } catch (NullPointerException ex) {
                    // Log error
                    Log.e(TAG, "User details not inserted into DB: UID not retrieved." ,
                    ex);
                    // Notify user of DB error due to UID not being retrieved. Shouldn't happen if user is created successfully.
                    Toast.makeText(RegisterActivity.this, "Could not retrieve UID, user may not have been created successfully.",
                    Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.e(TAG, "User details not inserted into DB:" + ex.getMessage(), ex);
                }

        }
    } // END OF insertUserDetailsToDB method

    /**
     * updateUI method will update RegisterActivity UI depending if user is currently signed in or not.
     * If user is signed in, removes all EditTexts and Buttons and notifies user they ae currently signed in.
     * @param user Firebase User currently signed in.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {

            try {

                // user currently signed in. Notify user of this and display name of user currently signed in.
                registerState.setText(R.string.registered_signed_in);
                userTextView.setText(user.getEmail().trim());
                userTextView.setVisibility(View.VISIBLE);

                // if there is a signed in user, set visibility of register form to GONE.
                numberEditText.setVisibility(View.GONE);
                emailAddressEditText.setVisibility(View.GONE);
                nameEditText.setVisibility(View.GONE);
                passwordEditText.setVisibility(View.GONE);
                confirmPassword.setVisibility(View.GONE);
                registerButton.setVisibility(View.GONE);
                userType.setVisibility(View.GONE);
            } catch (NullPointerException ex) {
                Log.e(TAG, "Unable to retrieve and display user email", ex);
                Toast.makeText(this, "Unable to retrieve and display user email", Toast.LENGTH_SHORT).show();
            }

        } else {

            // no user signed in, display default instructions and null userId TextView
            registerState.setText(R.string.register_instructions);
            userTextView.setText(null);

            // no user signed in, so make form visible.
            numberEditText.setVisibility(View.VISIBLE);
            emailAddressEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            confirmPassword.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            userType.setVisibility(View.VISIBLE);

        }

    } // end of updateUI method



    /**
     * Method is called by registerButton event handler to validate all user inputs in EditTexts.
     * Will display appropriate and relevant message to user if there is an issue with any input.
     */
    private boolean passwordsMatch() {

        // if passwords do not match
        if (passwordEditText.getText().toString().equals(confirmPassword.getText().toString())) {
            return true;
        } else {
            Toast.makeText(RegisterActivity.this, "Please ensure passwords match", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Method checks all input after registerButton is pressed.
     *
     * @return boolean input is valid or not
     */
    private boolean validateInput() {

        boolean valid = true;

        if (userTypeSelection.equalsIgnoreCase("Select user type")) {
            Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (nameEditText.getText().toString().length() < 4) {
            nameEditText.setError("Please enter full name");
            valid = false;
        }

        if (numberEditText.getText().toString().length() < 8) {
            numberEditText.setError("Please enter full Staff/Student number");
            valid = false;
        }

        if ((TextUtils.isEmpty(passwordEditText.getText().toString())) || (TextUtils.isEmpty(confirmPassword.getText().toString()))) {
            Toast.makeText(this, "Yeo they're both empty, so they are.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }


}

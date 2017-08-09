package com.example.liamc.lecturetrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.HeartRateConsentListener;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // BandClient to access MicrosoftBand
    private BandClient client = null;
    // BluetoothAdapter object to manage bluetooth of device.
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 1;

    // constant Strings used by FirebaseDatabase ref to access email and studentNumber nodes
    private final String EMAIL_NODE = "emailAddress";
    private final String STUDENT_NUMBER_NODE = "studentNumber";
    // TAG string to be used in Log entries in event of errors and other events.
    private static final String TAG = "MainActivity";

    private Student currentUserStudent;

    // Button and TextView objects uss to reference views in activity_main.xml
    private Button startButton, stopButton, consentButton, logInButton, logOutButton;
    private TextView heartRateFigure, loggedInState, heartRateText;

    // FirebaseAuth to get current user
    FirebaseAuth mFireAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;


    // [[FIREBASE DATABASE REFERENCES]]
    // Set up Firebase DB reference. Called root reference as it returns ref to root of JSON reference tree
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    // Reference to student node in Firebase Database.
    private DatabaseReference mStudentRef = mRootRef.child("students");

    // Reference to key of currently signed in user.
    // Will be used to retrieve name and student number to send along with synced HR data.
    private DatabaseReference mCurrentUserRef;

    // reference used to insert student HR data in DB.
    private DatabaseReference mHeartRateRef = mRootRef.child("studentHeartRate");

    DatabaseReference mHeartRateEventRef;

    // [[END OF FIREBASE DATABASE REFERENCES]]

    // create WeakReference Object to be used by HeartRateConsentTask.
    final WeakReference<Activity> reference = new WeakReference<Activity>(this);

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent event) {
            if (event != null) {
                appendToUI(String.format("%d", event.getHeartRate()));

                // create new child reference in heartRateRef with UID of currentUser

                mHeartRateEventRef.child("heartRate").setValue(String.valueOf(event.getHeartRate()));

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate Buttons and TextViews
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        consentButton = (Button) findViewById(R.id.consent_button);
        logInButton = (Button) findViewById(R.id.log_in_button);
        logOutButton = (Button) findViewById(R.id.log_out_button);
        heartRateFigure = (TextView) findViewById(R.id.heart_rate_figure);
        loggedInState = (TextView) findViewById(R.id.logged_in_state_text);
        heartRateText = (TextView) findViewById(R.id.heart_rate_text_view);

        // check device has Bluetooth Enabled
        checkBluetooth();

    } // end of onCreate()

    /**
     * onStart method calls super onStart.
     * Uses FirebaseAuth instance to check which user is logged in.
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        // instantiate currently logged in user.
        currentUser = mFireAuth.getCurrentUser();

        updateUI(currentUser);

        // set onClickListeners for Button views, will ve handled by onClick method in this class.
        logInButton.setOnClickListener(this);
        logOutButton.setOnClickListener(this);
        consentButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);


        // Value event listener to update TextView to display HR from DB.
        // Only used for demo purposes in early stages of dev.

        /*
        // Set listener for HR reference in DB.
        mHeartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String heartRateFromDB = dataSnapshot.getValue(String.class);
                heartRateFigureFromDB.setText(heartRateFromDB);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        */

    }

    /**
     * TODO javadoc for onDestroy
     */
    @Override
    protected void onDestroy() {
        try {
            client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
        } catch (BandIOException ex) {
            appendToUI(ex.getMessage());
        } catch (Exception ex) {
            appendToUI(ex.getMessage());
        }

    }

    /**
     * TODO javadoc for onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkBluetooth();
    }

    /**
     * Method uses BluetoothAdapter to check if Bluetooth is enabled on device.
     * If disable, will launch an intent to prompt user app would like to turn it on.
     */
    public void checkBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    /**
     * AsyncTask that will enable Bluetooth Connection to Microsoft Band.
     * Uses WeakReference passed by onClick of consentButton
     */
    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {

            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean b) {
                            }
                        });

                    }
                } else {
                    appendToUI("Band is not connected. Ensure bluetooth is enabled and Band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage;

                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "BandService does not support your current SDK Version, please update.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health Band Service is not available. Please ensure Microsoft Band app is installed and you have enabled permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occurred: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);
            } catch (Exception e) {
                appendToUI(e.getMessage());
            }

            return null;

        } // end of doInBackground method
    } // end of HeartRateConsentTask

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (getConnectedBandClient()) {
                    // check that user has granted access to HR sensor on Band.
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        // if granted, register HeartRateEventListener which will handle event.
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                        appendToUI("Please allow this application to access the heart rate monitor on your Microsoft Band.");
                    }
                } else {
                    appendToUI("No band is connected.");
                }
            } catch (BandException e) {
                String exceptionMessage;
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "BandService does not support your current SDK Version, please update.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health Band Service is not available. Please ensure Microsoft Band app is installed and you have enabled permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occurred: " + e.getMessage() + "\n";
                        break;
                }
                // show message to user in Toast
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                // show error message to user in Toast
                appendToUI(e.getMessage());
            }
            return null;
        } // end of doInBackground.
    } // end of HeartRateSubscriptionTask

    /**
     * Method takes String and sets contents to populate heartRateFigure textView.
     * Utility method to update figure of HR and if exception/error has occurred.
     * Used runOnUiThread to update.
     * @param string String to be displayed to user
     */
    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heartRateFigure.setText(string);
            }
        });
    }

    /**
     * Method checks that there are Bands paired with device.
     * Notifies user if no paired Bands are found.
     * @return boolean band is connected or not
     * @throws InterruptedException
     * @throws BandException
     */
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
            if (pairedBands.length == 0) {
                appendToUI("No Microsoft Band is paired with your device.\n" +
                        "Ensure you have installed Microsoft Band app and paired Band with your phone/tablet.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), pairedBands[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    /**
     * Method that will be called by onHeartRateChanged method of HeartRateEventListener.
     * Will write new HR value to CSV along with timestamp.
     * @param heartRateFigure TextView that will be converted and stored in file
     */
    private void saveHeartRateToFile(TextView heartRateFigure) {


    }

    /**
     *
     * @param user FirebaseUser returned by FirebaseAuth
     */
    private void updateUI(FirebaseUser user) {

        // if a user is logged in, will remove logInButton and replace with logOutButton.
        if (user != null) {
            logInButton.setVisibility(View.GONE);
            // make logOutButton clickable and visible.
            logOutButton.setVisibility(View.VISIBLE);
            logOutButton.setClickable(true);
            // display current user logged in
            loggedInState.setText(user.getEmail());

            heartRateText.setVisibility(View.VISIBLE);

            // If there is a valid user signed in, nest to their relevant UID in db
            mCurrentUserRef = mStudentRef.child(currentUser.getUid());

            // Use this new ref to listen once for data. SingleValueEventListener will execute at least once, and on any data change.
            // In this instance, only executes once to instantiate currentUserStudent Student object with details
            // underneath matching UID to current FirebaseUser.
            mCurrentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Trying to create Student with values returned from DB.
                    currentUserStudent = dataSnapshot.getValue(Student.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else {
            // if no user logged in, make logInButton visible and clickable, remove logOutButton.
            logInButton.setVisibility(View.VISIBLE);
            // remove logOutButton and render un-clickable.
            logOutButton.setVisibility(View.GONE);
            logOutButton.setClickable(false);

            // display to user the logged out message instructing to log in in order to track HR.
            loggedInState.setText(R.string.logged_out);
            // remover HR text until user logs in as they are not able to track until then.
            heartRateText.setVisibility(View.GONE);


        }
    } // end of updateUI method.

    /**
     * Sign out method. Is called if Log Out button is clicked.
     * OnClick method checks if there is a user logged in and only calls if logged in.
     *
     * Log Out button will not be visible or clickable if there is no user logged in.
     *
     */
    private void signOut() {
        mFireAuth.signOut();
        updateUI(null);
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
    }


    /**
     * TODO javadoc for onclick method.
     * @param view View that was clicked
     */
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.consent_button:
                    if (currentUser != null) {
                        new HeartRateConsentTask().execute(reference);
                    } else {
                        Toast.makeText(this, "Please sign in to track HR.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.start_button:

                    if (currentUser != null) {
                        heartRateFigure.setText("");
                        // if user is not null, instantiate reference using currentUser UID.
                        mHeartRateEventRef = mHeartRateRef.child(currentUserStudent.getUID());
                        // under this UID reference, insert value of student number, only needs inserted once.
                        mHeartRateEventRef.child("studentNumber").setValue(currentUserStudent.getStudentNumber());
                        new HeartRateSubscriptionTask().execute();
                    } else {
                        Toast.makeText(this, "Please sign in to track HR.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.stop_button:
                    new HeartRateSubscriptionTask().cancel(true);
                    heartRateFigure.setText("");

                    try {
                        client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                        client.disconnect();
                    } catch (BandIOException e) {
                        Toast.makeText(this, "Unable to unregister HeartRateEventListener: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to unregister HeartRateEventListener: " + e.getMessage());
                    } catch (Exception e) {
                        Toast.makeText(this, "Unable to unregister HeartRateEventListener: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to unregister HeartRateEventListener: " + e.getMessage());
                    }
                    break;
                case R.id.log_in_button:
                    Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                    startActivity(logInIntent);
                    break;
                case R.id.log_out_button:
                    if (currentUser!=null) {
                        signOut();
                    }
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

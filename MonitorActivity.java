package com.example.liamc.lecturetrack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Activity to demo realtime DB sync of HR data from MainActivity - DB - this activity.
 */

public class MonitorActivity extends AppCompatActivity {

    TextView studentsHeartRate;

    // Set up Firebase DB reference. Called root reference as it returns ref to root of JSON reference tree
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    // Create value listener
    DatabaseReference mHeartRateRef = mRootRef.child("heartRate");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);


    }


    /*
    @Override
    protected void onStart() {
        super.onStart();


        mHeartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String heartRate = dataSnapshot.getValue(String.class);
                studentsHeartRate.setText(heartRate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    */

}

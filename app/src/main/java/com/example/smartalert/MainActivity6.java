package com.example.smartalert;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity6 extends AppCompatActivity {

    TextView fire, flood, earth, storm, hurricane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        earth = findViewById(R.id.earthInc);
        flood = findViewById(R.id.floodInc);
        fire = findViewById(R.id.fireInc);
        storm = findViewById(R.id.stormInc);
        hurricane = findViewById(R.id.hurricaneInc);

        // Call the method to count emergencies when the activity is created
        countEmergencies();
    }

    public void countEmergencies() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("sent_alerts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the snapshot contains data
                if (snapshot.exists()) {
                    // Retrieve counts from the snapshot and update TextViews
                    long earthquakeCount = snapshot.child("Earthquake").getValue(Long.class);
                    long floodCount = snapshot.child("Flood").getValue(Long.class);
                    long fireCount = snapshot.child("Fire").getValue(Long.class);
                    long stormCount = snapshot.child("Storm").getValue(Long.class);
                    long hurricaneCount = snapshot.child("Hurricane").getValue(Long.class);

                    // Update TextViews with the counts
                    earth.setText(String.valueOf(earthquakeCount));
                    flood.setText(String.valueOf(floodCount));
                    fire.setText(String.valueOf(fireCount));
                    storm.setText(String.valueOf(stormCount));
                    hurricane.setText(String.valueOf(hurricaneCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                Log.e("Firebase", "Error getting data", error.toException());
            }
        });
    }

    public void onBack(View view) {
        finish();
    }
}

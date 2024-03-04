package com.example.smartalert;

import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity6 extends AppCompatActivity {

    TextView fire,flood,earth,storm,hurricane;

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
        Query query = database.getReference("Emergencies").orderByChild("status").equalTo("accepted");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int floodCount = 0;
                int fireCount = 0;
                int earthquakeCount = 0;
                int stormCount = 0;
                int hurricaneCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String emergencyType = snapshot.child("emergency").getValue(String.class);
                    switch (emergencyType) {
                        case "Flood":
                            floodCount++;
                            break;
                        case "Fire":
                            fireCount++;
                            break;
                        case "Earthquake":
                            earthquakeCount++;
                            break;
                        case "Storm":
                            stormCount++;
                            break;
                        case "Hurricane":
                            hurricaneCount++;
                            break;
                        default:
                            // Handle unrecognized emergency type
                            break;
                    }
                }

                earth.setText(String.valueOf(earthquakeCount));
                flood.setText(String.valueOf(floodCount));
                fire.setText(String.valueOf(fireCount));
                storm.setText(String.valueOf(stormCount));

                hurricane.setText(String.valueOf(hurricaneCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    public void onBack(View view){
        finish();
    }

}
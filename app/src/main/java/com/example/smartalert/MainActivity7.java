package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity7 extends AppCompatActivity {
    TextView date;
    TextView danger;
    TextView location;
    TextView counter;
    EditText description;
    ImageView imageView;
    private FirebaseDatabase database;    // Firebase Database reference
    public static final double earthRadius = 6371.0;
    int hours = 0;
    int kilometers = 0;
    Emergency emergency;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        date = findViewById(R.id.date);
        danger = findViewById(R.id.danger);
        counter=findViewById(R.id.counterview);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.imageView);
        // Set click listeners for buttons
        Button acceptButton = findViewById(R.id.submit_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccept();
            }
        });

        Button declineButton = findViewById(R.id.decline_button);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDecline();
            }
        });
        database = FirebaseDatabase.getInstance();

        GatherData();

    }

    private void GatherData() {
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");
        dbEmergency.orderByChild("count").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve emergency data from the snapshot
                        emergency = alertSnapshot.getValue(Emergency.class);
                        if (emergency != null && emergency.getStatus().equals("pending")) {
                            // Add emergency to the list
                            date.setText(emergency.getTimestamp());
                            danger.setText(emergency.getEmergency());
                            location.setText(emergency.getLocation());
                            description.setText(emergency.getDescription());
                            counter.setText("Total Reports:"+emergency.getCount());
                            Picasso.get().load(emergency.getImageUrl()).into(imageView);
                        }
                    }
                } else {
                    // Handle the case where no data exists
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


    public void onAccept(){
        ChangeStatus("accepted");
    }
    public void onDecline(){
        ChangeStatus("denied");
    }


    private void ChangeStatus(String new_status){
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");
        DatabaseReference reference = database.getReference("Emergencies");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot alertSnapshot : task.getResult().getChildren()) {
                    String eventType = alertSnapshot.child("emergency").getValue(String.class);
                    if (eventType != null && eventType.equals(emergency.getEmergency())) {
                        hours = 0;
                        kilometers = 0;
                        switch (emergency.getEmergency()) {
                            case "Earthquake":
                                hours = 2;
                                kilometers = 150;
                                break;
                            case "Flood":
                                hours = 12;
                                kilometers = 100;
                                break;
                            case "Hurricane":
                                hours = 24;
                                kilometers = 80;
                                break;
                            case "Fire":
                                hours = 48;
                                kilometers = 200;
                                break;
                            case "Storm":
                                hours = 5;
                                kilometers = 50;
                                break;
                        }
                        if (isWithinHours(alertSnapshot.child("timestamp").getValue(String.class), emergency.getTimestamp(), hours) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class), emergency.getLocation(), kilometers)) {
                            // Update status and count
                            emergency.setStatus(new_status);
                            emergency.setCount(0); // Set count to 0
                            reference.child(alertSnapshot.getKey()).child("status").setValue(new_status);
                            reference.child(alertSnapshot.getKey()).child("count").setValue(0); // Set count to 0 in database
                        }
                        else if(!isWithinHours(alertSnapshot.child("timestamp").getValue(String.class), emergency.getTimestamp(), hours)){
                            emergency.setStatus("denied");
                            emergency.setCount(0); // Set count to 0
                            reference.child(alertSnapshot.getKey()).child("status").setValue("denied");
                            reference.child(alertSnapshot.getKey()).child("count").setValue(0); // Set count to 0 in database
                        }
                    }
                }
                GatherData();
            }
        });
    }

    public void onBack(View view){
        finish();
    }
    public boolean isWithinHours(String timestamp1, String timestamp2, int n) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            long diff = dateFormat.parse(timestamp1).getTime() - dateFormat.parse(timestamp2).getTime();
            return Math.abs(diff) <= (long) n * 60 * 60 * 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    //Calculates the distance between 2 points using Haversine Formula
    public static boolean isWithinKilometers(String location1, String location2, double n) {
        String[] latLong1 = location1.split(",");
        String[] latLong2 = location2.split(",");
        double lat1 = Double.parseDouble(latLong1[0]);
        double lon1 = Double.parseDouble(latLong1[1]);
        double lat2 = Double.parseDouble(latLong2[0]);
        double lon2 = Double.parseDouble(latLong2[1]);

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance <= n;
    }

}


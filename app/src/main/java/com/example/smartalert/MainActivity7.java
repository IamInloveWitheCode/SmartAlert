package com.example.smartalert;

import static java.lang.Double.parseDouble;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseMessaging firebaseMessaging;
    private DatabaseReference userRef;
    String emergencyId="";
    DatabaseReference allUsersReference, rejectReference, acceptReference, sentAlertsReference;
    String message = "";
    String targetToken = "";
    Resources resources;
    String eventENG,associatedString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        firebaseMessaging = FirebaseMessaging.getInstance();
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
        userRef = FirebaseDatabase.getInstance().getReference("User");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        resources = getResources();

        //allUsersReference = database.getReference("Emergencies");

        /*allUsersReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        if(alertSnapshot.child("id").getValue().equals(getIntent().getStringExtra("id"))) {
                            emergency = alertSnapshot.getValue(Emergency.class);
                            int stringResourceId = resources.getIdentifier(alertSnapshot.child("emergency").getValue(String.class), "string","com.example.smartalert");
                            String associatedString = resources.getString(stringResourceId);
                            eventENG = alertSnapshot.child("emergency").getValue(String.class);
                            danger.setText(associatedString);
                            String loc = alertSnapshot.child("location").getValue(String.class);
                            try {
                                String city = geocoder.getFromLocation(parseDouble(loc.substring(0,loc.indexOf(","))),parseDouble(loc.substring(loc.indexOf(",")+1,loc.length())),1).get(0).getAddressLine(0);
                                location.setText(city);
                                System.out.println(city);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            date.setText(emergency.getTimestamp());
                            description.setText(emergency.getDescription());
                            counter.setText("Total Reports:"+emergency.getCount());
                            Picasso.get().load(emergency.getImageUrl()).into(imageView);
                        }
                    }
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });*/


        GatherData();

    }

    private void GatherData() {
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        dbEmergency.orderByChild("count").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean pendingEmergencyFound = false;
                    for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve emergency data from the snapshot
                        emergency = alertSnapshot.getValue(Emergency.class);
                        if (emergency != null && emergency.getStatus().equals("pending")) {
                            pendingEmergencyFound = true;
                            // Add emergency to the list
                            date.setText(emergency.getTimestamp());
                            int stringResourceId = resources.getIdentifier(alertSnapshot.child("emergency").getValue(String.class), "string","com.example.smartalert");
                            associatedString = resources.getString(stringResourceId);
                            eventENG = alertSnapshot.child("emergency").getValue(String.class);
                            danger.setText(associatedString);
                            location.setText(emergency.getLocation());
                            String loc = alertSnapshot.child("location").getValue(String.class);
                            try {
                                String city = geocoder.getFromLocation(parseDouble(loc.substring(0,loc.indexOf(","))),parseDouble(loc.substring(loc.indexOf(",")+1,loc.length())),1).get(0).getAddressLine(0);
                                location.setText(city);
                                System.out.println(city);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            description.setText(emergency.getDescription());
                            counter.setText("Total Reports:"+emergency.getCount());
                            Picasso.get().load(emergency.getImageUrl()).into(imageView);
                            emergencyId=alertSnapshot.getKey();
                        }
                    }
                    if (!pendingEmergencyFound) {
                        showMessage("Warning", "No Pending Emergencies Found!");

                        // Wait for 3 seconds before redirecting to MainActivity2
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(MainActivity7.this, MainActivity5.class);
                            startActivity(intent);
                            finish(); // Optional, if you want to close the current activity
                        }, 3000); // 3000 milliseconds delay (adjust as needed)
                    }
                } else {
                    showMessage("Warning", "No Pending Emergencies Found!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


    public void onAccept(){
        //ChangeStatus("accepted");
        //database = FirebaseDatabase.getInstance();
        allUsersReference = database.getReference("Emergencies");
        acceptReference = database.getReference("accepted");
        sentAlertsReference = database.getReference("sent_alerts");
        allUsersReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot alertSnapshot : task.getResult().getChildren()) {
                    String eventType = alertSnapshot.child("emergency").getValue(String.class);
                    if (eventType != null && eventType.equals(emergency.getEmergency())) {
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
                                alertSnapshot.child("emergency").getValue(String.class).equals(emergency.getEmergency()) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class), emergency.getLocation(), kilometers) ){

                            emergency.setStatus("accepted");
                            allUsersReference.child(alertSnapshot.getKey()).child("status").setValue("accepted");

                            emergency.setUserID(alertSnapshot.child("userId").getValue(String.class));


                        }
                    }
                }

                acceptReference.child(emergency.getId()).setValue(emergency);

                allUsersReference.child(emergency.getId()).removeValue();
                acceptReference.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        int c = 0;
                        for (DataSnapshot alertSnapshot : task1.getResult().getChildren()) {
                            if (alertSnapshot.child("emergency").getValue(String.class).equals(emergency.getEmergency())) {
                                c++;
                            }
                        }
                        sentAlertsReference.child(emergency.getEmergency()).setValue(c);
                    }
                });

                sendNotification();
                GatherData();
                //onBackPressed();
            } else {
                Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
            }
        });
    }


    public void onDecline(){
        //ChangeStatus("denied");
        database = FirebaseDatabase.getInstance();
        allUsersReference = database.getReference("alerts");
        rejectReference = database.getReference("rejected");
        allUsersReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        switch (emergency.getEmergency()){
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
                        if(isWithinHours(alertSnapshot.child("timestamp").getValue(String.class),emergency.getTimestamp(),hours) &&
                                alertSnapshot.child("emergency").getValue(String.class).equals(emergency.getEmergency()) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class),emergency.getLocation(),kilometers)&&
                                !alertSnapshot.child("id").getValue(String.class).equals(emergency.getId())){
                            //uncomment if you want rejected tables' records to have count equal to 1
                            //alertClass.setCount(alertClass.getCount()-1);
                            DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies").child(emergency.getId());
                            dbEmergency.child("status").setValue("accepted");
                            allUsersReference.child(alertSnapshot.getKey()).child("count").setValue(alertSnapshot.child("count").getValue(Integer.class)-1);
                        }
                    }
                    System.out.println(emergency);
                    rejectReference.child(emergency.getId()).setValue(emergency);
                    allUsersReference.child(emergency.getId()).removeValue();
                    GatherData();
                    //onBackPressed();
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });
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
                if(new_status.equals("approved")){
                    //SendNotification();
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
    private void sendNotification(){
        allUsersReference = database.getReference("Users");
        allUsersReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                    if (alertSnapshot.child("role").getValue(String.class).equals("user")) {
                        switch (eventENG) {
                            case "Earthquake":
                                kilometers = 150;
                                message = getString(R.string.alertEarthquake);
                                break;
                            case "Flood":
                                kilometers = 100;
                                message = getString(R.string.alertFlood);
                                break;
                            case "Hurricane":
                                kilometers = 80;
                                message = getString(R.string.alertHurricane);
                                break;
                            case "Fire":
                                kilometers = 200;
                                message = getString(R.string.alertFire);
                                break;
                            case "Storm":
                                kilometers = 50;
                                message = getString(R.string.alertStorm);
                                break;
                        }
                        targetToken = alertSnapshot.child("token").getValue(String.class);
                        allUsersReference.child(alertSnapshot.child("userid").getValue(String.class)).child("Location").setValue(emergency.getLocation());
                        allUsersReference.child(alertSnapshot.child("userid").getValue(String.class)).child("emergency").setValue(eventENG);
                        allUsersReference.child(alertSnapshot.child("userid").getValue(String.class)).child("description").setValue(message);
                    }
                }
            }
            else {
                Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
            }
        });
    }
    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

}


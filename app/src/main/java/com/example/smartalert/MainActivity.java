package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    EditText email, password;

    FirebaseAuth auth;
    FirebaseUser user;
    String token;
    LocationManager locationManager;
    String devicelocation = "";
    private FirebaseDatabase database;    // Firebase Database reference
    public static final double earthRadius = 6371.0;
    int hours = 0;
    int kilometers = 0;
    Emergency emergency;
    String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        email = findViewById(R.id.editTextTextPersonName2);
        password = findViewById(R.id.editTextTextPersonName);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        //startup();
        getnotified();

    }

    public void getnotified() {
        getLocation();
        DatabaseReference reference = database.getReference("accepted");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot alertSnapshot : task.getResult().getChildren()) {
                    emergency = alertSnapshot.getValue(Emergency.class);
                    String eventType = alertSnapshot.child("emergency").getValue(String.class);
                    String timestamp = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
                    if (eventType != null && eventType.equals(emergency.getEmergency())) {
                        switch (emergency.getEmergency()) {
                            case "Earthquake":
                                hours = 2;
                                kilometers = 150;
                                message = getString(R.string.alertEarthquake);
                                break;
                            case "Flood":
                                hours = 12;
                                kilometers = 100;
                                message = getString(R.string.alertFlood);
                                break;
                            case "Hurricane":
                                kilometers = 80;
                                hours = 24;
                                message = getString(R.string.alertHurricane);
                                break;
                            case "Fire":
                                kilometers = 200;
                                hours = 48;
                                message = getString(R.string.alertFire);
                                break;
                            case "Storm":
                                hours = 5;
                                kilometers = 50;
                                message = getString(R.string.alertStorm);
                                break;
                        }
                        if (isWithinHours(alertSnapshot.child("timestamp").getValue(String.class), timestamp, hours) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class), devicelocation, kilometers)) {
                            Toast.makeText(this, "This is my Toast message!",
                                    Toast.LENGTH_LONG).show();
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelID")
                                    .setSmallIcon(R.drawable.notification_bell)
                                    .setContentTitle(emergency.getEmergency())
                                    .setContentText(message)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            // Show the notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            notificationManager.notify(1, builder.build());
                       }
                   }

               }
           }
        });



    }
    public void startup() {

        LocationManager locationManager1 = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager1 != null){
            boolean isGPSEnabled = locationManager1.isLocationEnabled();
            if(!isGPSEnabled){
                Toast.makeText(this, getString(R.string.toastLocOn), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return;
            }
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();
                if (!areNotificationsEnabled) {
                    Toast.makeText(this, getString(R.string.toastNotifOn), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent);
                } else {
                    findLocation();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.toastAllowATT), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    // Method to retrieve FCM token and store it in Firebase Realtime Database
    private void storeFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    if (user != null) {
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                        usersRef.child(user.getUid()).child("token").setValue(token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Token stored successfully
                                        Log.d("FCMToken", "Token stored successfully: " + token);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to store token
                                        Log.e("FCMToken", "Failed to store token: " + e.getMessage());
                                    }
                                });
                    }
                } else {
                    // Failed to get token
                    Log.e("FCMToken", "Failed to get token: " + task.getException().getMessage());
                }
            }
        });
    }

    public void signup(View view) {
        if (!email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty()) {
            auth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                updateUser(user);
                                showMessage("Success", "User profile created!");

                                // Store FCM token after user signup
                                storeFCMToken();

                                // Write user data to Firebase Realtime Database
                                if (user != null) {
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                                    // Create a new User object with the necessary data including role
                                    User newUser = new User(user.getUid(), email.getText().toString(), "User");

                                    usersRef.child(user.getUid()).setValue(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                // Handle success
                                                showMessage("Success", "User data saved successfully!");
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle failure
                                                showMessage("Error", "Failed to save user data!");
                                            });
                                }
                            } else {
                                showMessage("Error", task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please provide all Info!");
        }
    }

    private void updateUser(FirebaseUser user) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .build();

        user.updateProfile(request);
    }

    public void signin(View view) {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString();

        if (!userEmail.isEmpty() && !userPassword.isEmpty()) {
            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                                usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            User userData = snapshot.getValue(User.class);
                                            if (userData != null) {
                                                String role = userData.getRole();
                                                if (role.equals("User")) {
                                                    // Go to MainActivity2
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                                    intent.putExtra("userId", user.getUid());
                                                    startActivity(intent);
                                                } else if (role.equals("Admin")) {
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity5.class);
                                                    intent.putExtra("userId", user.getUid());
                                                    startActivity(intent);
                                                } else {
                                                    showMessage("Error", "Unknown role!");
                                                }
                                            } else {
                                                showMessage("Error", "User data is null!");
                                            }
                                        } else {
                                            showMessage("Error", "User data not found!");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        showMessage("Error", "Database error: " + error.getMessage());
                                    }
                                });

                                // Store FCM token after user signin
                                storeFCMToken(); // Update the token every time the user signs in
                            } else {
                                showMessage("Error", "Authentication failed: " + task.getException().getMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please provide email and password!");
        }
    }
    public void findLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Enabled!");
                builder.setMessage("Please enable Location Services.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            }
        }
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Enabled");
            builder.setMessage("Please enable Location Services");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        } else {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d("Location", "Location changed: " + location.getLatitude() + ", " + location.getLongitude());
                    // Save the location to a global variable or use it directly where needed
                    devicelocation = location.getLatitude() + "," + location.getLongitude();
                    // Once you get the location, you can proceed with further actions, such as uploading data to Firebase
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (loc != null) {
                devicelocation = loc.getLatitude() + "," + loc.getLongitude();
                Log.d("Location", "Last known location: " + devicelocation);
                // If you need to use the last known location immediately, you can handle it here
            }
        }
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

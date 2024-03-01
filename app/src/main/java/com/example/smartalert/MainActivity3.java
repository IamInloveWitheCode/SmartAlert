package com.example.smartalert;

import android.Manifest;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity3 extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {
    private ImageView imageView;
    private Button selectImageButton;
    private TextView myTextView;
    private TextView mydate;
    private TextView description;
    private String Userlocation = "";
    private String latitude;
    private String longitude;
    String location = "";
    private String TypeOfEmergency;
    private Spinner spinner;
    private String userId;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private LocationManager locationManager;
    private FirebaseDatabase database;    // Firebase Database reference
    public static final double earthRadius = 6371.0;
    int hours = 0;
    int kilometers = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        userId = getIntent().getStringExtra("userId");

        // Initialize views and set listeners
        imageView = findViewById(R.id.imageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        myTextView = findViewById(R.id.myTextView);
        mydate = findViewById(R.id.date);
        description = findViewById(R.id.description);

        // Set up spinner
        spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dangers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Initialize locationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Call getLocation method to get user's location
        getLocation();

        selectImageButton.setOnClickListener(v -> openImagePicker());

        // Set current date
        SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormatWithZone.format(new Date());
        mydate.setText(currentDate);

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
        TypeOfEmergency = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    // Upload the image to Firebase Storage
    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveEmergencyData(uri.toString()))
                            .addOnFailureListener(e -> showMessage("Error", "Failed to get image URL: " + e.getMessage())))
                    .addOnFailureListener(e -> showMessage("Error", "Failed to upload image: " + e.getMessage()));
        } else {
            showMessage("Error", "No image selected");
        }
    }

    // Modify the method to save emergency data with the image URL
    private void saveEmergencyData(String imageUrl) {
        String stringdesc = description.getText().toString();
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");
        String timestamp = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
        DatabaseReference reference = database.getReference("Emergencies");
        String emergencyId = reference.push().getKey();
        Emergency emergency = new Emergency(stringdesc, TypeOfEmergency, latitude, longitude, location, timestamp, userId, imageUrl);
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
                            emergency.setCount(emergency.getCount() + 1);
                            reference.child(alertSnapshot.getKey()).child("count").setValue(alertSnapshot.child("count").getValue(Integer.class) + 1);
                        }
                    }
                }
                dbEmergency.child(emergencyId).setValue(emergency)
                        .addOnSuccessListener(aVoid -> {
                            showMessage("Success", "Emergency data saved successfully!");

                            // Wait for 3 seconds before redirecting to MainActivity2
                            new android.os.Handler().postDelayed(() -> {
                                Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                                startActivity(intent);
                                finish(); // Optional, if you want to close the current activity
                            }, 3000); // 3000 milliseconds delay (adjust as needed)
                        })
                        .addOnFailureListener(e -> showMessage("Error", "Failed to save emergency data!"));


            } else {
                showMessage("Error", "Failed to retrieve data: " + task.getException().getMessage());
            }
        });

    }

    // Activity Result API launcher for image selection
    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imageView.setImageURI(selectedImageUri);
                }
            }
    );

    public void FindLocation(android.view.View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        Userlocation = latitude + "\n" + longitude + "\n";
        myTextView.setText(Userlocation);
    }
    public void getLocation() {
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
                    MainActivity3.this.location = location.getLatitude() + "," + location.getLongitude();
                    // Once you get the location, you can proceed with further actions, such as uploading data to Firebase
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (loc != null) {
                location = loc.getLatitude() + "," + loc.getLongitude();
                Log.d("Location", "Last known location: " + location);
                // If you need to use the last known location immediately, you can handle it here
            }
        }
    }

    public void submitForm(android.view.View view) {
        if (description.getText().toString().isEmpty()) {
            showMessage("Error!!", "You cannot leave the description empty");
        } else {
            if (Userlocation.isEmpty()) {
                showMessage("Error!!", "Please wait for the app to gather your location");
            } else {
                // Upload the image to Firebase Storage
                uploadImage(selectedImageUri);
            }
        }
    }


    //Calculates whether n hours have passed from timestamp1 to timestamp2
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
    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }


}


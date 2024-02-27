package com.example.smartalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private String TypeOfEmergency;
    private Spinner spinner;
    private String userId;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private LocationManager locationManager;
    public static final double earthRadius = 6371.0;
    int hours = 0;
    int kilometers = 0;


    // Firebase Database reference
    private DatabaseReference dbEmergency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        userId = getIntent().getStringExtra("userId");

        imageView = findViewById(R.id.imageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        myTextView = findViewById(R.id.myTextView);
        mydate = findViewById(R.id.date);
        description = findViewById(R.id.description);

        spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dangers, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        selectImageButton.setOnClickListener(v -> openImagePicker());

        SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormatWithZone.format(new Date());
        mydate.setText(currentDate);
        // Initialize Firebase Storage reference
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
            // Generate a random image name
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("images/" + imageName);

            // Upload the image
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
        String stringdate = mydate.getText().toString();
        Emergency emergency = new Emergency(stringdesc, TypeOfEmergency, latitude, longitude, Userlocation, stringdate, userId, imageUrl);

        // Get reference to the "Emergencies" node in the database
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");

        // Set hours and kilometers based on the type of emergency
        switch (TypeOfEmergency) {
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
            default:
                // Set default values or handle the case where the emergency type is not recognized
                break;
        }

        // Check for existing events within a certain time frame and distance
        // Update the count if an existing event is found
        dbEmergency.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                    Emergency existingEmergency = alertSnapshot.getValue(Emergency.class);
                    if (existingEmergency != null && isWithinHours(existingEmergency.getTimestamp(), stringdate, hours) && isWithinKilometers(existingEmergency.getLatitude() + "," + existingEmergency.getLongitude(), latitude + "," + longitude, kilometers) && existingEmergency.getEmergency().equals(TypeOfEmergency)) {
                        int updatedCount = existingEmergency.getCount() + 1;
                        alertSnapshot.getRef().child("count").setValue(updatedCount);
                        return;
                    }
                }
                // No existing event found, add a new entry
                addNewEmergency(emergency, dbEmergency);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showMessage("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Method to add a new emergency entry to the database
    private void addNewEmergency(Emergency emergency, DatabaseReference dbEmergency) {
        String emergencyId = dbEmergency.push().getKey();
        dbEmergency.child(emergencyId).setValue(emergency)
                .addOnSuccessListener(aVoid -> showMessage("Success", "Emergency data saved successfully!"))
                .addOnFailureListener(e -> showMessage("Error", "Failed to save emergency data: " + e.getMessage()));
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

    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
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

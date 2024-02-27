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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

        // Generate a new unique key for the emergency data
        String emergencyId = dbEmergency.push().getKey();

        // Use the generated key to set the value in the database
        dbEmergency.child(emergencyId).setValue(emergency)
                .addOnSuccessListener(aVoid -> showMessage("Success", "Emergency data saved successfully!"))
                .addOnFailureListener(e -> showMessage("Error", "Failed to save emergency data!"));
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

}

package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity implements LocationListener{


    Spinner spinner;
    EditText editText;
    TextView textView9;
    Intent intent;

    // Some persmission request codes
    final int ACCESS_FINE_LOCATION_REQUEST_CODE = 123;

    // Some flags
    boolean locationPermissionHasBeenGranted;
    boolean locationChangedFirstTime = true;

    LocationManager locationManager;

    // Get Timestamp
    Calendar calendar = Calendar.getInstance();

    String currentDate = "";
    String locationOfUser = "";
    String LatitudeOfUser = "";
    String LongtitudeOfUser = "";
    String TypeOfEmergency = "";
    String DescriptionOfEmergency = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        editText = findViewById(R.id.editTextTextPersonName5);
        textView9 = findViewById(R.id.textView9);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity3.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.dangers));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);

        // About location of user
        locationPermissionHasBeenGranted = true;

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // If the user hasn't given the permissions that are needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_REQUEST_CODE);
            return;
        }

        // Ask for user's location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public void submitDangerousSituation(View view)
    {
        if(editText.getText().toString().equals(""))
        {
            showMessage("Error!!","You cannot leave the description Empty");
        }
        else
        {
            if(locationOfUser.equals(""))
            {
                showMessage("Error!!","Please wait for the app to gather your location");
            }
            else
            {
                //Setting Timestamp
                //currentDate = DateFormat.getDateInstance().format(calendar.getTime());

                Date date = new Date();
                SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss", Locale.getDefault());
                currentDate = dateFormatWithZone.format(date);

                //Setting the type of Emergency
                if(spinner.getSelectedItem().equals("Fire"))
                {
                    TypeOfEmergency = "Fire";
                }
                else if(spinner.getSelectedItem().equals("Flood"))
                {
                    TypeOfEmergency = "Flood";
                }
                else if(spinner.getSelectedItem().equals("Earthquake"))
                {
                    TypeOfEmergency = "Earthquake";
                }
                else if(spinner.getSelectedItem().equals("Tsunami"))
                {
                    TypeOfEmergency = "Tsunami";
                }

                //Setting the description
                DescriptionOfEmergency = editText.getText().toString();

//                Map<String,String> emergency = new HashMap<String,String>();
//                emergency.put("Emergency",TypeOfEmergency);
//                emergency.put("Location",locationOfUser);
//                emergency.put("Latitude",LatitudeOfUser);
//                emergency.put("Longtitude",LongtitudeOfUser);
//                emergency.put("Timestamp",currentDate);
//                emergency.put("Description",DescriptionOfEmergency);
                Emergency emergency = new Emergency(DescriptionOfEmergency,TypeOfEmergency,LatitudeOfUser,LongtitudeOfUser,locationOfUser,currentDate);

                // Δημιουργεί την πρώτη φορά collection με όνομα document, κάποιο τυχαίο
                db.collection("Emergencies").document().set(emergency)
                        .addOnCompleteListener((task) ->
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(MainActivity3.this,"Emergency has been submited successfully",Toast.LENGTH_LONG).show();
                                intent = new Intent(this, MainActivity2.class);
                                startActivity(intent);
                            }
                            else
                            {
                                showMessage("Error", task.getException().getLocalizedMessage());
                            }
                        });


//                showMessage("Here is the Details I gathered:",locationOfUser +"\n"+ currentDate +"\n"+ TypeOfEmergency +"\n"+ DescriptionOfEmergency);
            }
        }
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        // Getting the location of the user only 1 time
        if (locationChangedFirstTime)
        {
            locationChangedFirstTime = false;
            locationOfUser = location.getLatitude()+","+location.getLongitude();
            LatitudeOfUser = ""+location.getLatitude();
            LongtitudeOfUser = ""+location.getLongitude();

            // For the user (and me) to see that the location has been acquired
//            textView5.setText("Location has been taken, click here to save changes");
//            saveButton.setVisibility(Button.VISIBLE);

            textView9.setText("Location: " + locationOfUser);
        }
    }
}
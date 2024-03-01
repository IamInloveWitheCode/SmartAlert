package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity7 extends AppCompatActivity {
    TextView date;
    TextView danger;
    TextView location;
    EditText description;
    ImageView imageView;
    private FirebaseDatabase database;    // Firebase Database reference



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        date = findViewById(R.id.date);
        danger = findViewById(R.id.danger);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.imageView);
        database = FirebaseDatabase.getInstance();

        GatherData();

    }

    private void GatherData() {
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");
        DatabaseReference reference = database.getReference("Emergencies");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot alertSnapshot : task.getResult().getChildren()) {
                    String eventType = alertSnapshot.child("emergency").getValue(String.class);

                }

            }
            else {

            }
        });
    }



}





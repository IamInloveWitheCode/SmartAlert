package com.example.smartalert;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity7 extends AppCompatActivity {
    TextView date;
    TextView danger;
    TextView location ;
    EditText description;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        date = findViewById(R.id.date);
        danger = findViewById(R.id.danger);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.imageView);

    }

}

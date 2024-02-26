package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity5 extends AppCompatActivity {
    String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        userId = getIntent().getStringExtra("userId");


    }
    public void submit(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity7.class);
        startActivity(intent);

    }
    public void read(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
        startActivity(intent);
    }


}
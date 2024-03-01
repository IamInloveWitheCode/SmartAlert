package com.example.smartalert;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {

    RecyclerView recyclerView;
    EmergencyAdapter emergencyAdapter;
    List<Emergency> emergencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adding item decoration to display dividers between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        emergencyList = new ArrayList<>();
        emergencyAdapter = new EmergencyAdapter(emergencyList);
        recyclerView.setAdapter(emergencyAdapter);

        // Get reference to the "Emergencies" node in the database
        DatabaseReference dbEmergency = FirebaseDatabase.getInstance().getReference("Emergencies");

        dbEmergency.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve emergency data from the snapshot
                    Emergency emergency = snapshot.getValue(Emergency.class);
                    if (emergency != null) {
                        // Add emergency to the list
                        emergencyList.add(emergency);
                    }
                }
                // Notify the adapter that data set has changed
                emergencyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}

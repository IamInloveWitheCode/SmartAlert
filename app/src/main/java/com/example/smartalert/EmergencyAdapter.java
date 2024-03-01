package com.example.smartalert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {

    private List<Emergency> emergencyList;

    public EmergencyAdapter(List<Emergency> emergencyList) {
        this.emergencyList = emergencyList;
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        Emergency emergency = emergencyList.get(position);
        holder.bind(emergency);
    }

    @Override
    public int getItemCount() {
        return emergencyList.size();
    }

    static class EmergencyViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView, emergencyTextView, locationTextView, timestampTextView;


        EmergencyViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            emergencyTextView = itemView.findViewById(R.id.emergencyTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);

        }

        void bind(Emergency emergency) {
            descriptionTextView.setText("Description: " + emergency.getDescription());
            emergencyTextView.setText("Type: " + emergency.getEmergency());
            locationTextView.setText("Location: " + emergency.getLocation());
            timestampTextView.setText("Timestamp: " + emergency.getTimestamp());

        }
    }
}
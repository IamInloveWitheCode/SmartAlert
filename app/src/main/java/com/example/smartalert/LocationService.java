package com.example.smartalert;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

public class LocationService extends Service implements LocationListener {
    private LocationManager locationManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }
    @Override
    public void onLocationChanged(Location location) {
        // Handle location updates here
        // You can access the device's location from the 'location' object
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates when the service is destroyed
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}

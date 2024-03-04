package com.example.smartalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    DatabaseReference reference;
    String uid;
    String targetToken;
    String title;
    String message;
    String eventLocation;
    final String API_KEY = "AAAAc2IfUkI:APA91bFW6Yyo09OnPlHRpVG_F-RbTxW-Ifv1GcTIfnIXaWWO2X7iB-daHWKXbHMsk3Esf-oZDFvoMBfzSVexEvclcHUGpRKKeG-9gaqx29A6QMDbZhfBoCrVqR0VN1YfNHiWJAYlDpQb";
    String url = "https://fcm.googleapis.com/v1/projects/SmartAlert1/messages:send";
    public static final double earthRadius = 6371.0;
    int kilometers = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this);
                System.out.println(location);
                String loc = location.getLatitude()+ "," + location.getLongitude();
                reference = FirebaseDatabase.getInstance().getReference("all_users").child(uid);

                switch (title){
                    case "Earthquake":
                        kilometers = 150;
                        break;
                    case "Flood":
                        kilometers = 100;
                        break;
                    case "Hurricane":
                        kilometers = 80;
                        break;
                    case "Fire":
                        kilometers = 200;
                        break;
                    case "Storm":
                        kilometers = 50;
                        break;
                }
                reference.child("location").setValue(loc);
                reference.child("startTracking").setValue(false);
                if(isWithinKilometers(loc,eventLocation,kilometers)){
                    fetchOAuth2TokenAndSendFCMMessage();
                }
                stopSelf();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uid = intent.getStringExtra("userid");
        title = intent.getStringExtra("title");
        eventLocation = intent.getStringExtra("eventLocation");
        targetToken = intent.getStringExtra("token");
        message = intent.getStringExtra("message");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

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

    private void fetchOAuth2TokenAndSendFCMMessage() {
        String encodedAPIKey = Base64.getEncoder().encodeToString((API_KEY + ":").getBytes());
        String tokenFetchUrl = "https://accounts.google.com/o/oauth2/token";

        JsonObjectRequest tokenRequest = new JsonObjectRequest(Request.Method.POST, tokenFetchUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String accessToken = response.getString("access_token");
                            sendFCMMessage(accessToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + encodedAPIKey);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer".getBytes();
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(LocationService.this);
        requestQueue.add(tokenRequest);
    }

    private void sendFCMMessage(String accessToken) {
        JSONObject messageBody = new JSONObject();
        try {
            // Construct the message content
            JSONObject notification = new JSONObject();
            notification.put("title", "Watch out! " + title + "!");
            notification.put("content", message);

            // Set the recipient token
            messageBody.put("token", targetToken);

            // Set the message payload
            messageBody.put("message", notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, messageBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken); // Use OAuth2 access token
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(LocationService.this);
        requestQueue.add(request);
    }
}


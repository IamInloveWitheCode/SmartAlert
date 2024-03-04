package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    EditText email, password;

    FirebaseAuth auth;
    FirebaseUser user;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.editTextTextPersonName2);
        password = findViewById(R.id.editTextTextPersonName);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    // Method to retrieve FCM token and store it in Firebase Realtime Database
    private void storeFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    if (user != null) {
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                        usersRef.child(user.getUid()).child("token").setValue(token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Token stored successfully
                                        Log.d("FCMToken", "Token stored successfully: " + token);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to store token
                                        Log.e("FCMToken", "Failed to store token: " + e.getMessage());
                                    }
                                });
                    }
                } else {
                    // Failed to get token
                    Log.e("FCMToken", "Failed to get token: " + task.getException().getMessage());
                }
            }
        });
    }

    public void signup(View view) {
        if (!email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty()) {
            auth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                updateUser(user);
                                showMessage("Success", "User profile created!");

                                // Store FCM token after user signup
                                storeFCMToken();

                                // Write user data to Firebase Realtime Database
                                if (user != null) {
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                                    // Create a new User object with the necessary data including role
                                    User newUser = new User(user.getUid(), email.getText().toString(), "User");

                                    usersRef.child(user.getUid()).setValue(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                // Handle success
                                                showMessage("Success", "User data saved successfully!");
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle failure
                                                showMessage("Error", "Failed to save user data!");
                                            });
                                }
                            } else {
                                showMessage("Error", task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please provide all Info!");
        }
    }

    private void updateUser(FirebaseUser user) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .build();

        user.updateProfile(request);
    }

    public void signin(View view) {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString();

        if (!userEmail.isEmpty() && !userPassword.isEmpty()) {
            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
                                usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            User userData = snapshot.getValue(User.class);
                                            if (userData != null) {
                                                String role = userData.getRole();
                                                if (role.equals("User")) {
                                                    // Go to MainActivity2
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                                    intent.putExtra("userId", user.getUid());
                                                    startActivity(intent);
                                                } else if (role.equals("Admin")) {
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity5.class);
                                                    intent.putExtra("userId", user.getUid());
                                                    startActivity(intent);
                                                } else {
                                                    showMessage("Error", "Unknown role!");
                                                }
                                            } else {
                                                showMessage("Error", "User data is null!");
                                            }
                                        } else {
                                            showMessage("Error", "User data not found!");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        showMessage("Error", "Database error: " + error.getMessage());
                                    }
                                });

                                // Store FCM token after user signin
                                storeFCMToken(); // Update the token every time the user signs in
                            } else {
                                showMessage("Error", "Authentication failed: " + task.getException().getMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please provide email and password!");
        }
    }

}

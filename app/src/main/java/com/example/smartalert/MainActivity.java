package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    //Each intent represents the Activities that I will open
    Intent intent,intent2,intent3;

    EditText email,password;

    FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Subscribe to all notifications
        FirebaseMessaging.getInstance().subscribeToTopic("Emergencies")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Done";
                        if (!task.isSuccessful()) {
                            msg = "failed";
                        }
//                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        // Αρχικοποιώ το απο που θα παίρνω δεδομένα για το email και password που εισάγει ο χρήστης
        email= findViewById(R.id.editTextTextPersonName2);
        password=findViewById(R.id.editTextTextPersonName);

        // Αρχικοποιώ το Authentication object της Firebase με Singleton Design Pattern
        mAuth = FirebaseAuth.getInstance();

    }

    public void signin(View view)
    {
        if(email.getText().toString().equals("") || password.getText().toString().equals(""))
        {
            showMessage("Error!","You cannot leave Email or Password blank");
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    // Using a Lambda expression
                    .addOnCompleteListener((task) -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,"Welcome to the smart Alert app!!",Toast.LENGTH_LONG).show();

//                            String userRole, userId;
//                            DocumentSnapshot documentSnapshot = collectionReference.document(mAuth.getUid()).get();
                            collectionReference.document(mAuth.getUid()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists())
                                            {
                                                String userRole = documentSnapshot.getString("Role");

                                                if(userRole.equals("User"))
                                                {
                                                    intent = new Intent(MainActivity.this, MainActivity2.class);
                                                    startActivity(intent);
                                                }
                                                else
                                                {
                                                    intent3 = new Intent(MainActivity.this, MainActivity4.class);
                                                    startActivity(intent3);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showMessage("Error", task.getException().getLocalizedMessage());
                                        }
                                    });

                        } else {
                            showMessage("Error", task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    public void signup(View view)
    {
        // Δήλωση του Broadcast receiver:
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_POWER_CONNECTED);
//        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        registerReceiver(new MyReceiver(),filter);

        if(email.getText().toString().equals("") || password.getText().toString().equals(""))
        {
            showMessage("Error!","You cannot leave Email or Password blank");
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            // Έχω 1 μόνο Listener και επιλέγω τα αποτελέσματα που μου δίνει με τη βοήθεια του task
                            // ( Δηλαδή της παραμέτρου της συνάρτησης onComplete )
                            if (task.isSuccessful())
                            {
                                showMessage("Success!", "User Authentication was successfull");
                                // Μπορώ απο εκεί και ύστερα να έχω το όνομα του χρήστη από την βάση.
                                // Η Firebase χρησιμοποιεί token σε AppLevel

                                // Πως μπορώ να δείξω το Id του χρήστη που έκανε signup ή signin?
                                //showMessage("User Id:",mAuth.getUid());
                                intent2 = new Intent(MainActivity.this, MainActivity3.class);
                                Log.w("mAuth.getUid()",mAuth.getUid());
                                intent2.putExtra("userID",mAuth.getUid());
                                startActivity(intent2);
                            }
                            else
                            {
                                // Αυτό το else πετάει το σωστό μήνυμα που πρέπει να δει ο χρήστης
                                // Χρησιμοποιώντας το ".getException().getLocalizedMessage()" :
                                // πιάνω όλα τα πιθανά errors, και όχι μόνο αυτό, δείχνω μάλιστα
                                // και το κατάλληλο μήνυμα στον χρήστη
                                showMessage("Error!", task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }
}
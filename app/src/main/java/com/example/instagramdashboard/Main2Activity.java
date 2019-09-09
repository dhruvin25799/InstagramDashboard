package com.example.instagramdashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Main2Activity extends AppCompatActivity {
    MaterialButton b1;
    FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        b1 = findViewById(R.id.login_login);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    private void login() {
        TextInputEditText ed_email, ed_password;
        ed_email = findViewById(R.id.login_email);
        ed_password = findViewById(R.id.login_password);
        String email = ed_email.getText().toString();
        String password = ed_password.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            update();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    public void update() {
        DocumentReference docRef = db.collection("Users").document(Objects.requireNonNull(mAuth.getUid()));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("Username",document.get("Username").toString());
                        edit.putFloat("Rank",Float.parseFloat(document.get("Rank").toString()));
                        edit.putLong("TimeStamp",0);
                        edit.putInt("Followers", 0);
                        edit.putInt("Following", 0);
                        edit.putInt("Posts", 0);
                        edit.putInt("FollowersDiff", 0);
                        edit.putInt("FollowingDiff", 0);
                        edit.putString("DP","");
                        edit.commit();
                        Log.d("Pref",pref.getAll().toString());
                        startActivity(new Intent(getApplicationContext(), Main5Activity.class));
                        finish();
                    } else {
                        Log.d("TAG", "No such document");
                    }

                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}

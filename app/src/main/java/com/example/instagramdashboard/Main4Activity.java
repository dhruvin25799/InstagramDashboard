package com.example.instagramdashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main4Activity extends AppCompatActivity {
    MaterialButton b1;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        b1 = findViewById(R.id.iinfo_next);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterinitialinfo();
            }
        });
    }

    private void enterinitialinfo() {
        final TextInputEditText ed_fname, ed_lname, ed_username;
        ed_fname = findViewById(R.id.iinfo_fname);
        ed_lname = findViewById(R.id.iinfo_lname);
        ed_username = findViewById(R.id.iinfo_username);
        Map<String, String> inital_info = new HashMap<>();
        inital_info.put("First_Name", ed_fname.getText().toString());
        inital_info.put("Last_Name", ed_lname.getText().toString());
        inital_info.put("Username", ed_username.getText().toString());
        inital_info.put("Rank", "0");
        db.collection("Users").document(Objects.requireNonNull(mAuth.getUid()))
                .set(inital_info)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("Username",ed_username.getText().toString());
                        edit.putLong("TimeStamp",0);
                        edit.putInt("Followers", 0);
                        edit.putInt("Following", 0);
                        edit.putInt("Posts", 0);
                        edit.putInt("FollowersDiff", 0);
                        edit.putInt("FollowingDiff", 0);
                        edit.putFloat("Rank", 0);
                        edit.apply();
                        startActivity(new Intent(getApplicationContext(), Main5Activity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error writing document", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(getApplicationContext(),"Please enter the information", Toast.LENGTH_SHORT).show();
    }
}

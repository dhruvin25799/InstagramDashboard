package com.example.instagramdashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.view.MenuItem;
import android.view.View;

public class Main5Activity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_slide_right_enter,R.anim.fragment_slide_right_exit,R.anim.fragment_slide_left_enter,R.anim.fragment_slide_left_exit).replace(R.id.frag_container, new DashboardFragment()).commit();
                    return true;
                case R.id.navigation_leaderboard:
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_enter,
                            R.anim.fragment_slide_left_exit,
                            R.anim.fragment_slide_right_enter,
                            R.anim.fragment_slide_right_exit).replace(R.id.frag_container, new LeaderboardFragment()).commit();
                    return true;
                case R.id.navigation_logout:
                    Snackbar.make(findViewById(R.id.frag_container),"Confirm Logout ?",Snackbar.LENGTH_SHORT).setAction("Logout", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dashboard_logout();
                        }
                    }).show();
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mAuth = FirebaseAuth.getInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.frag_container, new DashboardFragment()).commit();
    }

    private void dashboard_logout() {
        mAuth.signOut();
        SharedPreferences pref = StaticContext.getAppContext().getSharedPreferences("MyPrefs", 0);
        SharedPreferences.Editor edit = pref.edit();
        edit.clear();
        edit.apply();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        //super.onBackPressed();
    }
}


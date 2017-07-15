package com.roadioapp.roadioapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private Activity activity;

    private final int SPLASH_DISPLAY_LENGTH = 4000;

    private FirebaseAuth mAuth;
    private SessionManager checkSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        activity = this;
        mAuth = FirebaseAuth.getInstance();
        checkSession = new SessionManager(activity);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                switchActCond();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void switchActCond(){
        if(checkSession.isValidate()){
            switchAct(BasicInfo.class);
        }else if(mAuth.getCurrentUser() != null){
            switchAct(MapActivity.class);
        }else{
            switchAct(MainActivity.class);
        }
    }

    private void switchAct(Class classname){
        startActivity(new Intent(activity, classname));
        finish();
    }
}
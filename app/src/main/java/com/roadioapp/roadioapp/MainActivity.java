package com.roadioapp.roadioapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    LinearLayout mLoginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        LinearLayout registerBtn = (LinearLayout) findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mLoginBtn = (LinearLayout) findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        }else{
            SessionManager checkSession = new SessionManager(MainActivity.this);
            HashMap userSess = checkSession.getRegPref();
            if(checkSession.isValidate()){
                finish();
                startActivity(new Intent(MainActivity.this, BasicInfo.class));
            }else{
                if(userSess.get("phone_num") != null){
                    finish();
                    startActivity(new Intent(MainActivity.this, ConfirmCode.class));
                }
            }
        }
    }
}

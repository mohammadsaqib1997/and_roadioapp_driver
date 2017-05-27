package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.roadioapp.roadioapp.LoginActivity;

public class AuthObject {

    private FirebaseAuth mAuth;
    public String authUid;

    private Activity activity;

    public AuthObject(Activity act){
        this.activity = act;
        mAuth = FirebaseAuth.getInstance();
        authUid = mAuth.getCurrentUser().getUid();
    }

    public AuthObject(){
        mAuth = FirebaseAuth.getInstance();
        authUid = mAuth.getCurrentUser().getUid();
    }

    public void signOut(){
        if(isLoginUser()){
            mAuth.signOut();
            updateUser();
        }
    }

    private void updateUser(){
        if(!isLoginUser()){
            activity.finish();
            Intent moveLogAct = new Intent(activity, LoginActivity.class);
            moveLogAct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            moveLogAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(moveLogAct);
        }
    }

    public boolean isLoginUser(){
        return mAuth.getCurrentUser() != null;
    }

}

package com.roadioapp.roadioapp.mModels;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;

public class User {

    public String email, first_name, last_name, mob_no, vehicle;

    private Activity activity;
    private DatabaseReference userCol;

    public User(){

    }

    public User(Activity activity){
        userCol = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public String getName(){
        return first_name+" "+last_name;
    }

    public void getVehicleByUID(String UID, final DBCallbacks.CompleteListener callback){
        userCol.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User data = dataSnapshot.getValue(User.class);
                callback.onSuccess(true, data.vehicle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage());
            }
        });
    }

    public void getMobNoByUID(String UID, final DBCallbacks.CompleteListener callback){
        userCol.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User data = dataSnapshot.getValue(User.class);
                callback.onSuccess(true, data.mob_no);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage());
            }
        });
    }



}

package com.roadioapp.roadioapp.mModels;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mObjects.AuthObject;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    public String first_name, last_name, vehicle, mob_no;

    //here program variables
    private DatabaseReference userCollection;
    private AuthObject authObj;

    public UserInfo(){

    }

    public UserInfo(Activity activity){
        userCollection = FirebaseDatabase.getInstance().getReference().child("users");
        authObj = new AuthObject(activity);
    }

    public void getUserInfo(String uid, final UserCallback callback){
        userCollection.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    initValue(dataSnapshot.getValue(UserInfo.class));
                    callback.onSuccess(dataSnapshot, null);
                }else{
                    callback.onSuccess(null, "No Data Found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(null, databaseError.getMessage());
            }
        });
    }

    public void getMyInfo(final DBCallbacks.CompleteListener callback){
        userCollection.child(authObj.authUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initValue(dataSnapshot.getValue(UserInfo.class));
                callback.onSuccess(true, "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage());
            }
        });
    }

    private void initValue(UserInfo userInfo){
        first_name = userInfo.first_name;
        last_name = userInfo.last_name;
        vehicle = userInfo.vehicle;
        mob_no = userInfo.mob_no;
    }

    public String getFirstName(){
        return first_name;
    }

    public String getLastName(){
        return last_name;
    }

    public void setMyName(String first_name, String last_name, final DBCallbacks.CompleteListener callback){
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("first_name", first_name);
        dataMap.put("last_name", last_name);
        userCollection.child(authObj.authUid).updateChildren(dataMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    callback.onSuccess(false, databaseError.getMessage());
                }else{
                    callback.onSuccess(true, "");
                }
            }
        });
    }

    public String fullName(){
        return first_name+" "+last_name;
    }

    public String getVehicle(){
        return vehicle;
    }

    public String getMobNo(){
        return mob_no;
    }

    public interface UserCallback{
        void onSuccess(DataSnapshot dataSnapshot, String errMsg);
    }
}
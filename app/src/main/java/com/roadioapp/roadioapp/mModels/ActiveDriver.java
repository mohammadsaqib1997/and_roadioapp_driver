package com.roadioapp.roadioapp.mModels;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;

import java.util.HashMap;
import java.util.Map;

public class ActiveDriver {

    public double lat, lng;
    public float azimuth;
    public String vehicle;

    private DatabaseReference activeDriverCol;

    public ActiveDriver(){

    }

    public ActiveDriver(Activity activity){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        activeDriverCol = mDatabase.child("active_driver");
    }

    public void setLocationChanged(String DUID, double lat, double lng, float azimuth){
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("lat", lat);
        dataMap.put("lng", lng);
        dataMap.put("direction", azimuth);
        activeDriverCol.child(DUID).setValue(dataMap);
    }

    public void delActiveDriver(String DUID, final DBCallbacks.CompleteListener callback){
        activeDriverCol.child(DUID).removeValue(new DatabaseReference.CompletionListener() {
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

    /*public void trackDriver(String DUID, final DBCallbacks.CompleteDSListener callback){
        activeDriverCol.child(DUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(true, "", dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage(), null);
            }
        });
    }*/



}

package com.roadioapp.roadioapp.mModels;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;

public class UserRequest {

    public String desLat, desLng, desText, disText, durText, id, orgLat, orgLng, orgText, parcelThmb, parcelUri, vecType;

    public long createdAt;

    private DatabaseReference userRequestCol;

    public UserRequest(){
        
    }

    public UserRequest(Activity activity){
        userRequestCol = FirebaseDatabase.getInstance().getReference().child("user_requests");
    }

    public void getReq(String child_url, final DBCallbacks.CompleteDSListener callback){
        userRequestCol.child(child_url).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(true, "", dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage(), null);
            }
        });
    }
}

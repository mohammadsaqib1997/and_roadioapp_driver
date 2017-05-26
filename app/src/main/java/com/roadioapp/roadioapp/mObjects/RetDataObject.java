package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RetDataObject {

    private DatabaseReference databaseReference;
    private Activity activity;

    public RetDataObject (Activity act, DatabaseReference dbRef) {
        this.activity = act;
        this.databaseReference = dbRef;
    }

    public static String getDataSnapshot(DataSnapshot dataSnapshot, String fieldName){
        String get = "";
        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
            DataSnapshot checkRef = dataSnapshot1.child(fieldName);
            if(checkRef.exists()){
                get = checkRef.getValue().toString();
                break;
            }
        }
        return get;
    }

    public void getReqSnapshotListener(String grabObj, final RetDataSnapCallback callback){
        databaseReference.child(grabObj).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "User Request DB Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface RetDataSnapCallback{
        void onSuccess(DataSnapshot dataSnapshot);
    }

}

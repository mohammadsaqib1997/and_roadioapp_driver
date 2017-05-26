package com.roadioapp.roadioapp.mModels;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserActiveRequest {

    public String driver_uid, req_id, status;
    public long active_time, complete_time;

    /*private DatabaseReference mDatabase, userActiveRequestCol, userLiveRequestCol;
    private AuthObj mAuthObj;
    private String[] statusArr;*/

    public UserActiveRequest(){

    }

    /*public UserActiveRequest(Activity act){
        mAuthObj = new AuthObj();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userActiveRequestCol = mDatabase.child("user_active_requests");
        userLiveRequestCol = mDatabase.child("user_live_requests");

        statusArr = act.getResources().getStringArray(R.array.req_status);
    }

    public void userReqAct(final String req_id, final String driver_uid, final ObjectInterfaces.SimpleCallback callback){
        if(mAuthObj.isLoginUser()){
            userLiveRequestCol.child(mAuthObj.authUid).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        callback.onSuccess(false, "Request remove error!");
                    }else{
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("driver_uid",driver_uid);
                        data.put("req_id",req_id);
                        data.put("status", statusArr[0]);
                        data.put("active_time", 0);
                        data.put("complete_time", 0);
                        userActiveRequestCol.child(mAuthObj.authUid).setValue(data, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    callback.onSuccess(false, "Add active request error!");
                                }else{
                                    callback.onSuccess(true, null);
                                }
                            }
                        });
                    }
                }
            });
        }else{
            callback.onSuccess(false, "Auth Not Found!");
        }
    }*/

    /*public void userActReqStatusCall(final ObjectInterfaces.UserActReqStatusCallback callback){
        if(mAuthObj.isLoginUser()){
            userActiveRequestCol.child(mAuthObj.authUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    callback.onSuccess(true, "", dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onSuccess(false, databaseError.getMessage(), null);
                }
            });
        }else{
            callback.onSuccess(false, "Auth Not Found!", null);
        }
    }*/

    /*public void completeJob(final int stars, final ObjectInterfaces.SimpleCallback callback){
        if(mAuthObj.isLoginUser()){
            userActiveRequestCol.child(mAuthObj.authUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserActiveRequest getData = dataSnapshot.getValue(UserActiveRequest.class);

                    Map<String, Object> setData = new HashMap<String, Object>();
                    setData.put("driver_uid", getData.driver_uid);
                    setData.put("client_uid", mAuthObj.authUid);
                    setData.put("status", getData.status);
                    setData.put("active_time", getData.active_time);
                    setData.put("complete_time", getData.complete_time);
                    setData.put("rating", stars);

                    String reqID = getData.req_id;

                    UserCompleteRequest setCompleteReqData = new UserCompleteRequest();
                    setCompleteReqData.activeToCloneData(reqID, setData, new UserCompleteRequest.CloneDataCallback() {
                        @Override
                        public void onSuccess(boolean status, String err) {
                            if(status){
                                userActiveRequestCol.child(mAuthObj.authUid).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError != null){
                                            callback.onSuccess(false, databaseError.getMessage());
                                        }else{
                                            callback.onSuccess(true, "");
                                        }
                                    }
                                });
                            }else{
                                callback.onSuccess(false, err);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onSuccess(false, databaseError.getMessage());
                }
            });
        }else{
            callback.onSuccess(false, "Auth Not Found!");
        }
    }*/
}

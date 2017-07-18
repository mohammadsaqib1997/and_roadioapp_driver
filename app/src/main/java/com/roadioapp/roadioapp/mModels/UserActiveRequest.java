package com.roadioapp.roadioapp.mModels;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.R;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mObjects.AuthObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserActiveRequest {

    public String driver_uid, req_id, status;
    public long active_time, complete_time;

    private DatabaseReference userActiveRequestCol;
    private Query userActReqColQuery;
    private ValueEventListener driverCheckListener, resDataByReqIDListener;

    public UserActiveRequest(){

    }

    public UserActiveRequest(Activity act){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        userActiveRequestCol = mDatabase.child("user_active_requests");
    }

    public void checkDriverReqActive(String UID, final DBCallbacks.CompleteListener callback){
        driverCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.e("CheckData", dataSnapshot+"");
                String sts = "invalid";
                if(dataSnapshot.exists()){
                    for(DataSnapshot children: dataSnapshot.getChildren()){
                        String status = children.child("status").getValue().toString();
                        if(!status.equals("req.complete")){
                            sts = "exist";
                            break;
                        }
                    }
                    callback.onSuccess(true, sts);
                }else{
                    callback.onSuccess(true, sts);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage());
            }
        };
        userActReqColQuery = userActiveRequestCol.orderByChild("driver_uid").equalTo(UID);
        userActReqColQuery.addValueEventListener(driverCheckListener);
    }

    public void removeDriverCheckListener(){
        if(driverCheckListener != null){
            userActReqColQuery.removeEventListener(driverCheckListener);
        }
    }

    public void getResDataByDUID(final String UID, final DBCallbacks.CompleteDSListener callback){
        userActiveRequestCol.orderByChild("driver_uid").equalTo(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DataSnapshot selectSnap = null;
                    for(DataSnapshot children: dataSnapshot.getChildren()){
                        String status = children.child("status").getValue().toString();
                        if(!status.equals("req.complete")){
                            selectSnap = children;
                            break;
                        }
                    }
                    if(selectSnap != null){
                        callback.onSuccess(true, "", selectSnap);
                    }else {
                        callback.onSuccess(false, "Data Not Found!", null);
                    }
                }else {
                    callback.onSuccess(false, "Data Not Found!", null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage(), null);
            }
        });
    }

    public void getResDataByReqID(final String req_id, final DBCallbacks.CompleteDSListener callback){
        resDataByReqIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    callback.onSuccess(true, "", dataSnapshot.getChildren().iterator().next());
                }else {
                    callback.onSuccess(false, "Data Not Found!", null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage(), null);
            }
        };
        userActiveRequestCol.orderByChild("req_id").equalTo(req_id).addValueEventListener(resDataByReqIDListener);
    }

    public void removeResDataByReqID(){
        if(resDataByReqIDListener != null){
            userActiveRequestCol.removeEventListener(resDataByReqIDListener);
        }
    }

    public void setStatus(final String req_id, final String status, final DBCallbacks.CompleteListener callback){
        userActiveRequestCol.orderByChild("req_id").equalTo(req_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Key = dataSnapshot.getChildren().iterator().next().getKey();

                if(status.equals("req.active")){
                    userActiveRequestCol.child(Key).child("active_time").setValue(ServerValue.TIMESTAMP);
                }else if(status.equals("req.complete")){
                    userActiveRequestCol.child(Key).child("complete_time").setValue(ServerValue.TIMESTAMP);
                }

                userActiveRequestCol.child(Key).child("status").setValue(status);
                callback.onSuccess(true, "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSuccess(false, databaseError.getMessage());
            }
        });
    }

    /*public void userReqAct(final String req_id, final String driver_uid, final DBCallbacks.CompleteListener callback){
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

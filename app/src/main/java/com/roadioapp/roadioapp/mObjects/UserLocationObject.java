package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roadioapp.roadioapp.RequestActiveActivity;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.ActiveDriver;
import com.roadioapp.roadioapp.mModels.User;
import com.roadioapp.roadioapp.mModels.UserActiveRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserLocationObject {

    private AuthObject authObj;
    private PermissionCheckObj permissionCheckObj;
    private MapObject mapObj;
    private Activity activity;
    private DatabaseReference onlineDrivers;

    private Timer timer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();
    private ProgressBarObject progressBarObj;

    private User userModel;
    private UserActiveRequest userActiveRequestModel;
    private ActiveDriver activeDriverModel;

    public UserLocationObject(AuthObject authObj, PermissionCheckObj permissionCheckObj, MapObject mapObj, Activity activity){
        this.activity = activity;
        this.permissionCheckObj = permissionCheckObj;
        this.mapObj = mapObj;
        this.authObj = authObj;
        userModel = new User(activity);
        progressBarObj = new ProgressBarObject(activity);
    }

    public void setOnlineDriversCol(){
        onlineDrivers = FirebaseDatabase.getInstance().getReference().child("online_drivers");
        userActiveRequestModel = new UserActiveRequest(activity);
    }

    public void setActiveDriverModel(){
        activeDriverModel = new ActiveDriver(activity);
    }

    public void startTimer() {
        if(authObj.isLoginUser()){
            progressBarObj.showProgressDialog();

            userActiveRequestModel.checkDriverReqActive(authObj.authUid, new DBCallbacks.CompleteListener() {
                @Override
                public void onSuccess(boolean status, String msg) {
                    if(status){
                        if(msg.equals("exist")){
                            progressBarObj.hideProgressDialog();
                            activity.finishAffinity();
                            activity.startActivity(new Intent(activity, RequestActiveActivity.class));
                        }else{
                            if(timer == null){
                                setOnlineUserCall();
                            }
                        }
                    }else{
                        progressBarObj.hideProgressDialog();
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void startTimer_AD() {
        if(authObj.isLoginUser()){
            userModel.getVehicleByUID(authObj.authUid, new DBCallbacks.CompleteListener() {
                @Override
                public void onSuccess(boolean status, String msg) {
                    if(status){
                        mapObj.driverVehicle = msg;
                        timer = new Timer();
                        initializeTask_AD();
                        timer.schedule(timerTask, 2000, 2000);
                    }else{
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setOnlineUserCall(){
        userModel.getVehicleByUID(authObj.authUid, new DBCallbacks.CompleteListener() {
            @Override
            public void onSuccess(boolean status, String msg) {
                progressBarObj.hideProgressDialog();
                if(status){
                    timer = new Timer();
                    initializeTask(msg);
                    timer.schedule(timerTask, 2000, 2000);
                }else{
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
        onlineDrivers.child(authObj.authUid).onDisconnect().removeValue();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTask(final String userVehicle) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mapObj.uCurrLL != null && !userVehicle.equals("")) {
                            Map<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("lat", mapObj.uCurrLL.latitude);
                            dataMap.put("lng", mapObj.uCurrLL.longitude);
                            dataMap.put("direction", mapObj.azimuth);
                            dataMap.put("uid", authObj.authUid);
                            dataMap.put("vehicle", userVehicle);
                            onlineDrivers.child(authObj.authUid).setValue(dataMap);
                        }
                    }
                });
            }
        };
    }

    private void initializeTask_AD() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mapObj.uCurrLL != null) {
                            mapObj.setDriverMarker(authObj.authUid);
                            activeDriverModel.setLocationChanged(authObj.authUid, mapObj.uCurrLL.latitude, mapObj.uCurrLL.longitude, mapObj.azimuth);
                        }
                    }
                });
            }
        };
    }

    public void startLocationUpdates() {
        if (permissionCheckObj.permissionCheck()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mapObj.mGoogleApiClient, mapObj.mLocationRequest, (LocationListener) activity);
            mapObj.mRequestingLocationUpdates = true;
        }

    }

    public void stopLocationUpdates() {
        if (mapObj.mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mapObj.mGoogleApiClient, (LocationListener) activity);
            mapObj.mRequestingLocationUpdates = false;
        }
    }

}

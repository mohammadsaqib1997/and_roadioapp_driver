package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.os.Handler;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserLocationObject {

    private AuthObject authObj;
    private PermissionCheckObj permissionCheckObj;
    private MapObject mapObj;
    private Activity activity;

    private Timer timer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();

    public UserLocationObject(AuthObject authObj, PermissionCheckObj permissionCheckObj, MapObject mapObj, Activity activity){
        this.activity = activity;
        this.permissionCheckObj = permissionCheckObj;
        this.mapObj = mapObj;
        this.authObj = authObj;
    }


    public void startTimer() {
        if(authObj.isLoginUser()){
            timer = new Timer();
            initializeTask();
            timer.schedule(timerTask, 2000, 2000);
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        /*if (curLocLL != null && !userVehicle.equals("")) {
                            DatabaseReference userOnline = onlineDrivers.child(authObj.authUid);
                            Map<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("lat", curLocLL.latitude);
                            dataMap.put("lng", curLocLL.longitude);
                            dataMap.put("direction", (float) azimuth);
                            dataMap.put("uid", authObj.authUid);
                            dataMap.put("vehicle", userVehicle);
                            userOnline.setValue(dataMap);
                        }*/
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

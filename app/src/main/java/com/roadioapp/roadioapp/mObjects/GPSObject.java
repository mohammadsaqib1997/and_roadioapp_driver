package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import static android.content.Context.LOCATION_SERVICE;

public class GPSObject {

    private Activity activity;

    private LocationManager locationManager;

    public GPSObject(Activity act){
        this.activity = act;
    }

    public boolean isGPSEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void enableGPS() {
        Intent sett_i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        sett_i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(sett_i);
    }

}

package com.roadioapp.roadioapp.mObjects;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.roadioapp.roadioapp.ActivityConstants.ProConstants;

import static android.os.Build.VERSION_CODES.M;

public class PermissionCheckObj {

    private Activity activity;
    private ProConstants proConstants;

    public PermissionCheckObj(Activity act){
        this.activity = act;
        this.proConstants = new ProConstants();
    }

    public boolean permissionCheck() {
        if (verCheck()) {
            int hasFineLocationPermission = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean callPermissionCheck(){
        if (verCheck()) {
            int hasCallPermission = activity.checkSelfPermission(Manifest.permission.CALL_PHONE);
            if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void setCallPermission(){
        if (verCheck()) {
            activity.requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE},
                    proConstants.PERM_REQUEST_CALL);
        }
    }

    public void setPermission() {
        if (verCheck()) {
            activity.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    proConstants.PERM_REQUEST_LOCATION);
        }
    }

    public boolean storagePermissionCheck(){
        if (verCheck()) {
            int hasCallPermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void setStoragePermission(){
        if (verCheck()) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    proConstants.PERM_REQUEST_STORAGE);
        }
    }

    public void showPermissionErr() {
        Toast.makeText(activity, "Your mobile not allowed this Permission!", Toast.LENGTH_LONG).show();
    }

    private boolean verCheck() {
        return android.os.Build.VERSION.SDK_INT >= M;
    }

}
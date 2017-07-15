package com.roadioapp.roadioapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.roadioapp.roadioapp.ActivityConstants.ProConstants;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.UserInfo;
import com.roadioapp.roadioapp.mObjects.AuthObject;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.GPSObject;
import com.roadioapp.roadioapp.mObjects.MapObject;
import com.roadioapp.roadioapp.mObjects.PermissionCheckObj;
import com.roadioapp.roadioapp.mObjects.UserLocationObject;
import com.squareup.picasso.Picasso;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Activity activity;
    private GoogleMap mMap;

    private MapFragment mapFragment;
    boolean drawerState = false;


    // properties variable
    DrawerLayout drawer_layout;

    ImageView navMenuIcon, logOutBtn, settingBtn, userProfileImg;
    TextView usernameSBar;
    LinearLayout smListCont, myEarningsBtn;
    RelativeLayout mainActCon;
    LinearLayout curLocCont, setCurLocBtn, bottomBtnCon, requestBtn;
    //Objects here
    PermissionCheckObj permissionCheckObj;

    ButtonEffects buttonEffectsObj;
    GPSObject gpsObj;
    ProConstants proConstants;

    MapObject mapObj;
    AuthObject authObj;
    UserLocationObject userLocationObj;
    private UserInfo userInfoModel;
    private StorageReference mProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        setProperties();

        navMenuIcon.setOnClickListener(this);
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerState = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerState = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        requestBtn.setOnClickListener(this);
        logOutBtn.setOnClickListener(this);
        settingBtn.setOnClickListener(this);

        setCurLocBtn.setOnClickListener(this);

        mapObj.buildGoogleApiClient();
        mapObj.createLocationRequest();
        mapObj.buildLocationSettingsRequest();

        mapObj.mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        userLocationObj.stopLocationUpdates();
        userLocationObj.stopTimer();
        mapObj.mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapObj.setMap(mMap);
        mapObj.setDefaultMapListner();

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MapTag", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapTag", "Can't find style. Error: ", e);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapObj.karachi, 10));

        mapObj.getDeviceLocation(false, false, null, true);
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        switch (getId) {
            case R.id.navMenuIcon:
                if (drawerState) {
                    drawer_layout.closeDrawer(Gravity.START);
                } else {
                    drawer_layout.openDrawer(Gravity.START);
                }
                break;
            case R.id.setCurLocBtn:
                mapObj.getDeviceLocation(true, true, null, true);
                break;
            case R.id.logoutBtn:
                confirmDialog();
                break;
            case R.id.settingBtn:
                startActivity(new Intent(activity, SettingActivity.class));
                break;
            case R.id.requestBtn:
                startActivity(new Intent(activity, RequestRecyleView.class));
                break;
            default:
                break;
        }
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure! You want to Logout!");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                authObj.signOut();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == proConstants.PERM_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mapObj.mGoogleApiClient.connect();
                Toast.makeText(MapActivity.this, "Location Permission Access", Toast.LENGTH_SHORT)
                        .show();
            } else {
                permissionCheckObj.setPermission();
                Toast.makeText(MapActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (!mapObj.mRequestingLocationUpdates) {
            userLocationObj.startLocationUpdates();
            userLocationObj.startTimer();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mapObj.getDeviceLocation(false, false, location, false);
        mapObj.azimuth = location.getBearing();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    private void setProperties() {
        buttonEffectsObj = new ButtonEffects(this);
        gpsObj = new GPSObject(this);
        proConstants = new ProConstants();
        mapObj = new MapObject(this);
        authObj = new AuthObject(this);
        userInfoModel = new UserInfo(this);
        mProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        permissionCheckObj = new PermissionCheckObj(this);
        userLocationObj = new UserLocationObject(authObj, permissionCheckObj, mapObj, this);
        userLocationObj.setOnlineDriversCol();

        navMenuIcon = (ImageView) findViewById(R.id.navMenuIcon);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) drawer_layout.findViewById(R.id.nav_view);
        logOutBtn = (ImageView) navView.findViewById(R.id.logoutBtn);
        settingBtn = (ImageView) navView.findViewById(R.id.settingBtn);
        myEarningsBtn = (LinearLayout) navView.findViewById(R.id.myEarningsBtn);
        smListCont = (LinearLayout) navView.findViewById(R.id.smListCont);
        usernameSBar = (TextView) navView.findViewById(R.id.usernameSBar);
        userProfileImg = (ImageView) navView.findViewById(R.id.userProfileImg);

        for(int i = 0; i < smListCont.getChildCount(); i++){
            LinearLayout ListItem = (LinearLayout) smListCont.getChildAt(i);
            ListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.getId() == R.id.myEarningsBtn){
                        startActivity(new Intent(activity, MyEarningsActivity.class));
                    }else{
                        Toast.makeText(activity, "Coming Soon!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        userInfoModel.getMyInfo(new DBCallbacks.CompleteListener() {
            @Override
            public void onSuccess(boolean status, String msg) {
                if(status){
                    usernameSBar.setText(userInfoModel.first_name+" "+userInfoModel.last_name);
                }else{
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mProfileImageRef.child(authObj.authUid+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.with(activity).load(task.getResult()).placeholder(R.drawable.circle_img).transform(new CircleTransform()).into(userProfileImg);
                }
            }
        });

        mainActCon = (RelativeLayout) findViewById(R.id.mainActCon);

        requestBtn = (LinearLayout) findViewById(R.id.requestBtn);
        buttonEffectsObj.btnEventEff(requestBtn);

        curLocCont = (LinearLayout) findViewById(R.id.curLocCont);
        setCurLocBtn = (LinearLayout) findViewById(R.id.setCurLocBtn);

        bottomBtnCon = (LinearLayout) findViewById(R.id.bottomBtnCon);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

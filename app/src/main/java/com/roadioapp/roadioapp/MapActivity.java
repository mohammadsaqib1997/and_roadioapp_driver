package com.roadioapp.roadioapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.ActivityConstants.ProConstants;
import com.roadioapp.roadioapp.mObjects.AuthObject;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.GPSObject;
import com.roadioapp.roadioapp.mObjects.MapObject;
import com.roadioapp.roadioapp.mObjects.PermissionCheckObj;
import com.roadioapp.roadioapp.mObjects.UserLocationObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //private GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private LatLng karachi;
    private Location mLastKnownLocation;
    LocationManager locationManager;


    boolean drawerState = false, userCamMove = true, bothLocation = false, pickLocation = false, googleClientConn = false;

    // properties variable
    DrawerLayout drawer_layout;
    ImageView navMenuIcon, logOutBtn;
    RelativeLayout mainActCon;
    LinearLayout curLocCont, setCurLocBtn, bottomBtnCon, requestBtn;
    /*Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();*/

    //Storage Variables
    //LatLng curLocLL;
    boolean firstCamMov = true;
    //double azimuth = 0f;

    /*//Firebase Variables
    private FirebaseAuth mAuth;
    private String authUid = "", userVehicle = "";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase, onlineDrivers, userInfo;*/

    private static boolean appCheckTer = false;

    //Objects here
    PermissionCheckObj permissionCheckObj;
    ButtonEffects buttonEffectsObj;
    GPSObject gpsObj;

    ProConstants proConstants;
    MapObject mapObj;
    AuthObject authObj;
    UserLocationObject userLocationObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        setCurLocBtn.setOnClickListener(this);

        mapObj.buildGoogleApiClient();
        mapObj.createLocationRequest();
        mapObj.buildLocationSettingsRequest();

        mapObj.mGoogleApiClient.connect();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (!permissionCheckObj.permissionCheck()) {
            permissionCheckObj.setPermission();
        } else {
            if (mapObj.isConnected()) {
                userLocationObj.startLocationUpdates();
                userLocationObj.startTimer();
            }
        }
    }*/

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

        karachi = new LatLng(24.861462, 67.009939);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(karachi, 10));

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                userCamMove = true;
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mMap.getUiSettings().setAllGesturesEnabled(true);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        getDeviceLocation(false, false, null, true);
    }

    private void getDeviceLocation(boolean anim, final boolean defLatLng, final Location curLocation, boolean move) {

        if (permissionCheckObj.permissionCheck()) {
            if (gpsObj.isGPSEnabled()) {
                mLastKnownLocation = (curLocation != null) ? curLocation : LocationServices.FusedLocationApi.getLastLocation(mapObj.mGoogleApiClient);
                if (mLastKnownLocation != null) {
                    mapObj.uCurrLL = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    if (!mMap.isMyLocationEnabled()) {
                        mMap.setMyLocationEnabled(true);
                    }
                    if (firstCamMov && curLocation != null) {
                        move = true;
                        anim = true;
                        firstCamMov = false;
                    }
                    if (move) {
                        if (anim) {
                            mapMoveCam(mapObj.uCurrLL, null, true, true);
                        } else {
                            mapMoveCam(mapObj.uCurrLL, null, true, false);
                        }
                    }
                } else {
                    if (defLatLng) {
                        mapMoveCam(karachi, null, move, anim);
                    }
                }
            } else {
                gpsObj.enableGPS();
            }
        } else {
            permissionCheckObj.showPermissionErr();
        }
    }

    private void mapMoveCam(LatLng latLng, LatLngBounds latLngBounds, boolean uMoveCam, boolean anim) {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        if (latLngBounds != null) {
            if (anim) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, getHeightWidth("w"), 500, 100));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, getHeightWidth("w"), 500, 100));
            }
        } else {
            if (anim) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            }
        }
        userCamMove = uMoveCam;
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
                getDeviceLocation(true, true, null, true);
                break;
            case R.id.logoutBtn:
                confirmDialog();
                break;
            case R.id.requestBtn:
                startActivity(new Intent(MapActivity.this, RequestRecyleView.class));
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

    private int getHeightWidth(String arg) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (arg.equals("h")) {
            return displayMetrics.heightPixels;
        } else if (arg.equals("w")) {
            return displayMetrics.widthPixels;
        }
        return 0;
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
        mCurrentLocation = location;
        getDeviceLocation(false, false, mCurrentLocation, false);
        mapObj.azimuth = location.getBearing();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void setProperties() {
        buttonEffectsObj = new ButtonEffects(this);
        gpsObj = new GPSObject(this);
        proConstants = new ProConstants();
        mapObj = new MapObject(this);
        authObj = new AuthObject(this);

        permissionCheckObj = new PermissionCheckObj(this);
        userLocationObj = new UserLocationObject(authObj, permissionCheckObj, mapObj, this);

        navMenuIcon = (ImageView) findViewById(R.id.navMenuIcon);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) drawer_layout.findViewById(R.id.nav_view);
        logOutBtn = (ImageView) navView.findViewById(R.id.logoutBtn);

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

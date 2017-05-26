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
        OnMapReadyCallback, View.OnClickListener, View.OnTouchListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;

    LocationRequest mLocationRequest;
    LocationSettingsRequest mLocationSettingsRequest;
    Location mCurrentLocation;
    Boolean mRequestingLocationUpdates;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private LatLng karachi;
    final private int PERM_REQUEST_LOCATION = 100;
    private Location mLastKnownLocation;
    LocationManager locationManager;


    boolean drawerState = false, userCamMove = true, bothLocation = false, pickLocation = false, googleClientConn = false;

    // properties variable
    DrawerLayout drawer_layout;
    ImageView navMenuIcon, logOutBtn;
    RelativeLayout mainActCon;
    LinearLayout curLocCont, setCurLocBtn, bottomBtnCon, requestBtn;
    TextView requestBtnText;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    RequestQueue geocodingAPIReq, directionAPIReq;

    private ProgressDialog progressDialog;

    //Storage Variables
    LatLng curLocLL;
    boolean firstCamMov = true;
    double azimuth = 0f;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private String authUid = "", userVehicle = "";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase, onlineDrivers, userInfo;

    private static boolean appCheckTer = false;

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
        requestBtn.setOnTouchListener(this);
        logOutBtn.setOnClickListener(this);

        setCurLocBtn.setOnClickListener(this);

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
    }

    private void signOut() {
        mAuth.signOut();
        onlineDrivers.child(authUid).removeValue();
        finish();
        startActivity(new Intent(MapActivity.this, MainActivity.class));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionCheck()) {
            setPermission();
        } else {
            if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
                startLocationUpdates();
                startTimer();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("AppStatus", "App Terminated");
        mAuth.removeAuthStateListener(mAuthListener);
        stopLocationUpdates();
        stopTimer();
        mGoogleApiClient.disconnect();
    }

    public void startTimer() {
        timer = new Timer();
        initializeTask();
        timer.schedule(timerTask, 2000, 2000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (curLocLL != null && !userVehicle.equals("")) {
                            DatabaseReference userOnline = onlineDrivers.child(authUid);
                            Map<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("lat", curLocLL.latitude);
                            dataMap.put("lng", curLocLL.longitude);
                            dataMap.put("direction", (float) azimuth);
                            dataMap.put("uid", authUid);
                            dataMap.put("vehicle", userVehicle);
                            userOnline.setValue(dataMap);
                        }
                    }
                });
            }
        };
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

        if (permissionCheck()) {
            if (isGPSEnabled()) {
                mLastKnownLocation = (curLocation != null) ? curLocation : LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastKnownLocation != null) {
                    curLocLL = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
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
                            mapMoveCam(curLocLL, null, true, true);
                        } else {
                            mapMoveCam(curLocLL, null, true, false);
                        }
                    }
                } else {
                    if (defLatLng) {
                        mapMoveCam(karachi, null, true, false);
                    }
                }
            } else {
                enableGPS();
            }
        } else {
            showPermissionErr();
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

    private boolean isGPSEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void enableGPS() {
        Intent sett_i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        sett_i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sett_i);
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
                signOut();
                dialog.dismiss();
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

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
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
    public boolean onTouch(View v, MotionEvent event) {
        int getId = v.getId();
        switch (getId) {
            case R.id.requestBtn:
                btnEventEff(event, requestBtn, requestBtnText);
                break;
            default:
                break;
        }
        return false;
    }

    public void btnEventEff(MotionEvent event, View v, TextView tv) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.colorPrimary);
            tv.setTextColor(Color.parseColor("#ffffff"));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setBackgroundResource(R.color.colorAccent);
            tv.setTextColor(Color.parseColor("#333333"));
        }
    }

    public void btnEventEffRounded(MotionEvent event, View v, TextView tv) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.drawable.bg_acc_rounded_inverse);
            tv.setTextColor(Color.parseColor("#ffffff"));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setBackgroundResource(R.drawable.bg_acc_rounded);
            tv.setTextColor(Color.parseColor("#333333"));
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private boolean verCheck() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            return true;
        return false;
    }

    public boolean permissionCheck() {
        if (verCheck()) {
            int hasFineLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERM_REQUEST_LOCATION);
        }
    }

    private void showPermissionErr() {
        Toast.makeText(this, "Your mobile not allowed this Permission!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                    Toast.makeText(MapActivity.this, "Location Permission Access", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    setPermission();
                    Toast.makeText(MapActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
            startTimer();
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void startLocationUpdates() {
        if (permissionCheck()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mRequestingLocationUpdates = true;
        }

    }

    protected void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mRequestingLocationUpdates = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        getDeviceLocation(false, false, mCurrentLocation, false);
        azimuth = location.getBearing();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showProgressDialog() {
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    private void setProperties() {
        navMenuIcon = (ImageView) findViewById(R.id.navMenuIcon);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) drawer_layout.findViewById(R.id.nav_view);
        logOutBtn = (ImageView) navView.findViewById(R.id.logoutBtn);

        mainActCon = (RelativeLayout) findViewById(R.id.mainActCon);

        requestBtn = (LinearLayout) findViewById(R.id.requestBtn);
        requestBtnText = (TextView) requestBtn.getChildAt(0);

        curLocCont = (LinearLayout) findViewById(R.id.curLocCont);
        setCurLocBtn = (LinearLayout) findViewById(R.id.setCurLocBtn);

        bottomBtnCon = (LinearLayout) findViewById(R.id.bottomBtnCon);

        // here instances assign
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /*if (firebaseAuth.getCurrentUser() != null) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    authUid = user.getUid();

                }*/
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userInfo = mDatabase.child("users");
        onlineDrivers = mDatabase.child("online_drivers");

        if (mAuth.getCurrentUser() != null) {
            authUid = mAuth.getCurrentUser().getUid();
            userInfo.child(authUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    userVehicle = user.vehicle;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        onlineDrivers.child(authUid).onDisconnect().removeValue();


        mRequestingLocationUpdates = false;
        progressDialog = new ProgressDialog(this);

        geocodingAPIReq = Volley.newRequestQueue(this);
        directionAPIReq = Volley.newRequestQueue(this);
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

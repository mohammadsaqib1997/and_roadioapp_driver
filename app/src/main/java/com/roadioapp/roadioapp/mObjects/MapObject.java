package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapObject {

    private Activity activity;
    private GoogleMap mMap;
    private LocationSettingsRequest mLocationSettingsRequest;

    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;

    public boolean mRequestingLocationUpdates = false;
    public LatLng uCurrLL;
    public float azimuth = 0f;
    public Location mLastKnownLocation;
    public LatLng karachi;
    public String driverVehicle = "";

    private Marker orgMarker, desMarker;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private boolean firstCamMov;
    private PermissionCheckObj permissionCheckObj;
    private GPSObject gpsObj;

    private Map<String, Marker> ADMarker;
    private JSONObject MarkerIcons;
    private boolean userCurrLocIcon = true;

    public MapObject(Activity act){
        this.activity = act;
        permissionCheckObj = new PermissionCheckObj(act);
        gpsObj = new GPSObject(act);
        karachi = new LatLng(24.861462, 67.009939);
        firstCamMov = true;
    }

    public void setMap(GoogleMap map){
        this.mMap = map;
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                /*.addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)*/
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) activity)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) activity)
                .build();
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void setLocationIcon(boolean userIcon){
        userCurrLocIcon = userIcon;
        mMap.setMyLocationEnabled(userIcon);
    }

    public void getDeviceLocation(boolean anim, final boolean defLatLng, final Location curLocation, boolean move) {

        if (permissionCheckObj.permissionCheck()) {
            if (gpsObj.isGPSEnabled()) {
                mLastKnownLocation = (curLocation != null) ? curLocation : LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastKnownLocation != null) {
                    uCurrLL = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    if (!mMap.isMyLocationEnabled() && userCurrLocIcon) {
                        mMap.setMyLocationEnabled(true);
                    }
                    if (firstCamMov && curLocation != null) {
                        move = true;
                        anim = true;
                        firstCamMov = false;
                    }
                    if (move) {
                        if (anim) {
                            mapMoveCam(uCurrLL, null, anim);
                        } else {
                            mapMoveCam(uCurrLL, null, anim);
                        }
                    }
                } else {
                    if (defLatLng) {
                        mapMoveCam(karachi, null, anim);
                    }
                }
            } else {
                gpsObj.enableGPS();
            }
        } else {
            permissionCheckObj.showPermissionErr();
        }
    }

    public void mapMoveCam(LatLng latLng, LatLngBounds latLngBounds, boolean anim) {
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
    }

    public void setDefaultMapListner(){
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
    }

    private int getHeightWidth(String arg) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (arg.equals("h")) {
            return displayMetrics.heightPixels;
        } else if (arg.equals("w")) {
            return displayMetrics.widthPixels;
        }
        return 0;
    }

    public void setTrackingContent(){
        ADMarker = new HashMap<String, Marker>();
        MarkerIcons = new JSONObject();
        try {
            MarkerIcons.put("Bike", activity.getResources().getIdentifier("top_bike", "drawable", "com.roadioapp.roadioapp"));
            MarkerIcons.put("Car", activity.getResources().getIdentifier("top_car", "drawable", "com.roadioapp.roadioapp"));
            MarkerIcons.put("Pickup", activity.getResources().getIdentifier("top_van", "drawable", "com.roadioapp.roadioapp"));
            MarkerIcons.put("Truck", activity.getResources().getIdentifier("top_truck", "drawable", "com.roadioapp.roadioapp"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDriverMarker(String UID){
        if(uCurrLL != null){
            Marker marker;
            if(ADMarker.containsKey(UID)){
                marker = ADMarker.get(UID);
                marker.setPosition(uCurrLL);
                marker.setRotation(azimuth);
            }else{
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(uCurrLL);
                markerOptions.flat(true).anchor(0.5f, 0.5f).rotation(azimuth);
                try {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource((int) MarkerIcons.get(driverVehicle)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                marker = mMap.addMarker(markerOptions);
            }
            ADMarker.put(UID, marker);
        }
    }

    public void removeDriverMarker(String UID){
        Marker marker = ADMarker.get(UID);
        marker.remove();
        ADMarker.remove(UID);
    }

    public void setOrgMarker(LatLng latLng){
        if (orgMarker!=null) {
            orgMarker.setPosition(latLng);
        }else{
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(activity.getResources().getIdentifier("ic_location_pin", "drawable", "com.roadioapp.roadioapp")));
            orgMarker = mMap.addMarker(markerOptions);
        }
    }

    public void setDesMarker(LatLng latLng){
        if (desMarker!=null) {
            desMarker.setPosition(latLng);
        }else{
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(activity.getResources().getIdentifier("ic_cur_loc_dark", "drawable", "com.roadioapp.roadioapp")));
            desMarker = mMap.addMarker(markerOptions);
        }
    }

    /*public void animateMarker(final Marker marker, final LatLng toPosition,
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
    }*/


}

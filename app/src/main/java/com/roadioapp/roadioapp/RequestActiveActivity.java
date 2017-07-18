package com.roadioapp.roadioapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.roadioapp.roadioapp.ActivityConstants.ProConstants;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.ActiveDriver;
import com.roadioapp.roadioapp.mModels.User;
import com.roadioapp.roadioapp.mModels.UserActiveRequest;
import com.roadioapp.roadioapp.mModels.UserRequest;
import com.roadioapp.roadioapp.mObjects.AuthObject;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.MapObject;
import com.roadioapp.roadioapp.mObjects.PermissionCheckObj;
import com.roadioapp.roadioapp.mObjects.PopupObject;
import com.roadioapp.roadioapp.mObjects.ProgressBarObject;
import com.roadioapp.roadioapp.mObjects.UserLocationObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestActiveActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    MapFragment mapFragment;
    GoogleMap mMap;
    ButtonEffects btnEffects;
    ProgressBarObject progressBarObj;
    DateFormat formatter;

    LinearLayout
            stsPendingCon,
            stsActiveCon,
            stsCompleteCon,
            contactDBtn,
            requestBtn,
            setCurLocBtn;
    TextView complete_time_TV, active_time_TV, requestBtnInnerTV;
    ImageView navMenuIcon;

    String[] statusArr, parcelTextTypes;

    long active_time = 0, complete_time = 0;
    String reqClientMob = "", reqID = "", nextStatus = "";
    LatLngBounds latLngBounds;
    LatLng orgLL, desLL;

    UserActiveRequest userActiveRequestModel;
    User userModel;
    ActiveDriver activeDriverModel;
    UserRequest userRequestModel;

    PermissionCheckObj permissionCheckObj;
    ProConstants proConstants;

    AuthObject authObj;
    PopupObject popupObj;
    MapObject mapObj;
    UserLocationObject userLocationObj;

    Dialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_active);

        setProperties();

    }

    @Override
    protected void onDestroy() {
        disconnectObjects();
        super.onDestroy();
    }

    private void disconnectObjects(){
        userActiveRequestModel.removeResDataByReqID();
        userLocationObj.stopLocationUpdates();
        userLocationObj.stopTimer();
        mapObj.mGoogleApiClient.disconnect();
    }

    private void setProperties() {
        formatter = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");

        statusArr = getResources().getStringArray(R.array.req_status);
        parcelTextTypes = getResources().getStringArray(R.array.parcel_text_types);

        permissionCheckObj = new PermissionCheckObj(this);
        progressBarObj = new ProgressBarObject(this);
        proConstants = new ProConstants();
        authObj = new AuthObject();
        popupObj = new PopupObject(this);
        mapObj = new MapObject(this);
        userLocationObj = new UserLocationObject(authObj, permissionCheckObj, mapObj, this);
        userLocationObj.setActiveDriverModel();

        confirmDialog = popupObj.confirmPopup();

        userModel = new User(this);
        userActiveRequestModel = new UserActiveRequest(this);
        activeDriverModel = new ActiveDriver(this);
        userRequestModel = new UserRequest(this);

        stsPendingCon = (LinearLayout) findViewById(R.id.sts_pending_con);
        stsActiveCon = (LinearLayout) findViewById(R.id.sts_active_con);
        stsCompleteCon = (LinearLayout) findViewById(R.id.sts_complete_con);
        setCurLocBtn = (LinearLayout) findViewById(R.id.setCurLocBtn);
        setCurLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LLBoundSet();
                if(latLngBounds != null){
                    mapObj.mapMoveCam(null, latLngBounds, true);
                }else{
                    mapObj.getDeviceLocation(true, true, null, true);
                }

            }
        });

        navMenuIcon = (ImageView) findViewById(R.id.navMenuIcon);
        navMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        complete_time_TV = (TextView) findViewById(R.id.complete_time_TV);
        active_time_TV = (TextView) findViewById(R.id.active_time_TV);

        contactDBtn = (LinearLayout) findViewById(R.id.contactDBtn);
        contactDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialog();
            }
        });

        requestBtn = (LinearLayout) findViewById(R.id.requestBtn);
        requestBtnInnerTV = (TextView) requestBtn.getChildAt(0);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.show();
                popupObj.setCancelBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                    }
                });
                popupObj.setYesBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                        userActiveRequestModel.setStatus(reqID, nextStatus, new DBCallbacks.CompleteListener() {
                            @Override
                            public void onSuccess(boolean status, String msg) {
                                if (!status) {
                                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        progressBarObj.showProgressDialog();
        userActiveRequestModel.getResDataByDUID(authObj.authUid, new DBCallbacks.CompleteDSListener() {
            @Override
            public void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot) {
                /*progressBarObj.hideProgressDialog();*/
                if (status) {
                    String Key = dataSnapshot.getKey();
                    UserActiveRequest userActiveRequestDataSnap = dataSnapshot.getValue(UserActiveRequest.class);
                    reqID = userActiveRequestDataSnap.req_id;
                    lookupReqStatus(reqID);
                    userRequestModel.getReq(Key+"/"+reqID, new DBCallbacks.CompleteDSListener() {
                        @Override
                        public void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot) {
                            progressBarObj.hideProgressDialog();
                            UserRequest userRequestData = dataSnapshot.getValue(UserRequest.class);
                            orgLL = new LatLng(Double.parseDouble(userRequestData.orgLat),Double.parseDouble(userRequestData.orgLng));
                            desLL = new LatLng(Double.parseDouble(userRequestData.desLat),Double.parseDouble(userRequestData.desLng));
                            mapObj.setOrgMarker(orgLL);
                            mapObj.setDesMarker(desLL);
                            //LLBoundSet();
                        }
                    });
                    userModel.getMobNoByUID(Key, new DBCallbacks.CompleteListener() {
                        @Override
                        public void onSuccess(boolean status, String msg) {
                            if (status) {
                                reqClientMob = msg;
                            }
                        }
                    });
                } else {
                    progressBarObj.hideProgressDialog();
                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEffects = new ButtonEffects(this);

        btnEffects.btnEventEffRounded(contactDBtn);
        btnEffects.btnEventEffRounded(requestBtn);

        mapObj.buildGoogleApiClient();
        mapObj.createLocationRequest();
        mapObj.buildLocationSettingsRequest();

        mapObj.mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapObj.setMap(mMap);
        mapObj.setDefaultMapListner();
        mapObj.setLocationIcon(false);

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

    private void lookupReqStatus(String reqID) {
        userActiveRequestModel.getResDataByReqID(reqID, new DBCallbacks.CompleteDSListener() {
            @Override
            public void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot) {
                if (status) {
                    UserActiveRequest userActiveRequestDataSnap = dataSnapshot.getValue(UserActiveRequest.class);
                    active_time = userActiveRequestDataSnap.active_time;
                    complete_time = userActiveRequestDataSnap.complete_time;
                    statusChangeUI(userActiveRequestDataSnap.status);
                } else {
                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void statusChangeUI(String reqSts) {
        if (reqSts != null && reqSts.equals(statusArr[0])) {
            nextStatus = statusArr[1];
            stsUI_inAct(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[0]);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        } else if (reqSts != null && reqSts.equals(statusArr[1])) {
            nextStatus = statusArr[2];
            stsUI_act(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[1]);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        } else if (reqSts != null && reqSts.equals(statusArr[2])) {
            nextStatus = statusArr[3];
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[2]);

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, "00:00");
        } else if (reqSts != null && reqSts.equals(statusArr[3])) {
            nextStatus = "";
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_act(stsCompleteCon);

            disconnectObjects();
            activeDriverModel.delActiveDriver(authObj.authUid, new DBCallbacks.CompleteListener() {
                @Override
                public void onSuccess(boolean status, String msg) {
                    if (status) {
                        finish();
                        startActivity(new Intent(RequestActiveActivity.this, MapActivity.class));
                    }else{
                        Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, convertDate(complete_time));
        } else {
            Log.e("CheckCallBacks", "Req No Status Found!");
        }
    }

    private void LLBoundSet(){
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        if(mapObj.uCurrLL != null){
            if(nextStatus.equals(statusArr[1]) || nextStatus.equals(statusArr[2])){
                latLngBuilder.include(orgLL).include(mapObj.uCurrLL);
                latLngBounds = latLngBuilder.build();
            }else if (nextStatus.equals(statusArr[3])){
                latLngBuilder.include(desLL).include(mapObj.uCurrLL);
                latLngBounds = latLngBuilder.build();
            }else{
                latLngBounds = null;
            }
        }else{
            latLngBuilder.include(orgLL).include(desLL);
            latLngBounds = latLngBuilder.build();
        }
    }

    private void stsUI_act(LinearLayout layout) {
        layout.setAlpha(1);
        ImageView stsPenImgView = (ImageView) layout.getChildAt(0);
        stsPenImgView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_success, null));
    }

    private void stsUI_inAct(LinearLayout layout) {
        layout.setAlpha(0.5f);
        ImageView stsImgView = (ImageView) layout.getChildAt(0);
        stsImgView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_proccess, null));
    }

    private void showContactDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contact_dialog);
        LinearLayout callBtn = (LinearLayout) dialog.findViewById(R.id.callBtn);
        LinearLayout smsBtn = (LinearLayout) dialog.findViewById(R.id.smsBtn);

        btnEffects.btnEventEffRounded(callBtn);
        btnEffects.btnEventEffRounded(smsBtn);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionCheckObj.callPermissionCheck()) {
                    callDriverIntent();
                } else {
                    permissionCheckObj.setCallPermission();
                }
            }
        });
        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsDriverIntent();
            }
        });
        dialog.show();
    }

    public void callDriverIntent() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:+" + reqClientMob));
        startActivity(callIntent);
    }

    private void smsDriverIntent() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:+" + reqClientMob)));
    }

    private void setTime(TextView tv, String text) {
        tv.setText(text);
    }

    private String convertDate(long timestamp) {
        if (timestamp == 0) {
            return "00:00";
        } else {
            Date date = new Date(timestamp);
            return formatter.format(date);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == proConstants.PERM_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callDriverIntent();
            } else {
                Toast.makeText(RequestActiveActivity.this, "Call Permission Denied", Toast.LENGTH_SHORT).show();
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
            userLocationObj.startTimer_AD();
            mapObj.setTrackingContent();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mapObj.getDeviceLocation(false, false, location, false);
        mapObj.azimuth = location.getBearing();
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
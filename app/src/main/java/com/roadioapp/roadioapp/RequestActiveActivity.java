package com.roadioapp.roadioapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.roadioapp.roadioapp.ActivityConstants.ProConstants;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.User;
import com.roadioapp.roadioapp.mModels.UserActiveRequest;
import com.roadioapp.roadioapp.mObjects.AuthObject;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.PermissionCheckObj;
import com.roadioapp.roadioapp.mObjects.PopupObject;
import com.roadioapp.roadioapp.mObjects.ProgressBarObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestActiveActivity extends AppCompatActivity implements OnMapReadyCallback {

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
            requestBtn;
    TextView complete_time_TV, active_time_TV, requestBtnInnerTV;
    ImageView navMenuIcon;

    String[] statusArr, parcelTextTypes;

    long active_time = 0, complete_time = 0;
    String reqClientMob = "", reqID = "", nextStatus="";
    //boolean firstRes = true;

    UserActiveRequest userActiveRequestModel;
    User userModel;

    PermissionCheckObj permissionCheckObj;
    ProConstants proConstants;

    AuthObject authObj;
    PopupObject popupObj;

    Dialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_active);

        setProperties();

    }

    private void setProperties(){
        formatter = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");

        statusArr = getResources().getStringArray(R.array.req_status);
        parcelTextTypes = getResources().getStringArray(R.array.parcel_text_types);
        permissionCheckObj = new PermissionCheckObj(this);
        progressBarObj = new ProgressBarObject(this);
        proConstants = new ProConstants();
        authObj = new AuthObject();
        popupObj = new PopupObject(this);

        confirmDialog = popupObj.confirmPopup();

        userModel = new User(this);
        userActiveRequestModel = new UserActiveRequest(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        stsPendingCon = (LinearLayout) findViewById(R.id.sts_pending_con);
        stsActiveCon = (LinearLayout) findViewById(R.id.sts_active_con);
        stsCompleteCon = (LinearLayout) findViewById(R.id.sts_complete_con);

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
                                if(!status){
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
                progressBarObj.hideProgressDialog();
                if(status){
                    String Key = dataSnapshot.getKey();
                    UserActiveRequest userActiveRequestDataSnap = dataSnapshot.getValue(UserActiveRequest.class);
                    /*active_time = userActiveRequestDataSnap.active_time;
                    complete_time = userActiveRequestDataSnap.complete_time;*/
                    reqID = userActiveRequestDataSnap.req_id;
                    lookupReqData(reqID);
                    //statusChangeUI(userActiveRequestDataSnap.status);
                    //if(firstRes){
                        userModel.getMobNoByUID(Key, new DBCallbacks.CompleteListener() {
                            @Override
                            public void onSuccess(boolean status, String msg) {
                                if(status){
                                    reqClientMob = msg;
                                }
                            }
                        });
                    //    firstRes = false;
                    //}
                }else{
                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEffects = new ButtonEffects(this);

        btnEffects.btnEventEffRounded(contactDBtn);
        btnEffects.btnEventEffRounded(requestBtn);
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

        LatLng karachi = new LatLng(24.861462, 67.009939);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(karachi, 10));
    }

    private void lookupReqData(String reqID){
        userActiveRequestModel.getResDataByReqID(reqID, new DBCallbacks.CompleteDSListener() {
            @Override
            public void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot) {
                if(status){
                    UserActiveRequest userActiveRequestDataSnap = dataSnapshot.getValue(UserActiveRequest.class);
                    active_time = userActiveRequestDataSnap.active_time;
                    complete_time = userActiveRequestDataSnap.complete_time;
                    statusChangeUI(userActiveRequestDataSnap.status);
                }else{
                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void statusChangeUI(String reqSts){
        if(reqSts != null && reqSts.equals(statusArr[0])){
            nextStatus = statusArr[1];
            stsUI_inAct(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[0]);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[1])){
            nextStatus = statusArr[2];
            stsUI_act(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[1]);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[2])){
            nextStatus = statusArr[3];
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            requestBtnInnerTV.setText(parcelTextTypes[2]);

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[3])){
            nextStatus = "";
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_act(stsCompleteCon);

            finish();
            startActivity(new Intent(this, MapActivity.class));

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, convertDate(complete_time));
        }else{
            Log.e("CheckCallBacks", "Req No Status Found!");
        }
    }

    private void stsUI_act(LinearLayout layout){
        layout.setAlpha(1);
        ImageView stsPenImgView = (ImageView) layout.getChildAt(0);
        stsPenImgView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_success, null));
    }

    private void stsUI_inAct(LinearLayout layout){
        layout.setAlpha(0.5f);
        ImageView stsImgView = (ImageView) layout.getChildAt(0);
        stsImgView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_proccess, null));
    }

    private void showContactDialog(){
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
                if(permissionCheckObj.callPermissionCheck()){
                    callDriverIntent();
                }else{
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

    public void callDriverIntent(){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:+"+reqClientMob));
        startActivity(callIntent);
    }

    private void smsDriverIntent(){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:+"+reqClientMob)));
    }

    private void setTime(TextView tv, String text){
        tv.setText(text);
    }

    private String convertDate(long timestamp){
        if(timestamp == 0){
            return "00:00";
        }else{
            Date date = new Date(timestamp);
            return formatter.format(date);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == proConstants.PERM_REQUEST_CALL){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callDriverIntent();
            } else {
                Toast.makeText(RequestActiveActivity.this, "Call Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
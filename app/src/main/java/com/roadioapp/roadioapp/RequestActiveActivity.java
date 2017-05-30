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
            requestBtn,
            ratingCon,
            star_rating_con;
    TextView complete_time_TV, active_time_TV;
    ImageView navMenuIcon;

    String[] statusArr;

    long active_time = 0, complete_time = 0;
    String reqDriverMob = "", reqID = "";

    UserActiveRequest userActiveRequestModel;
    User userModel;

    PermissionCheckObj permissionCheckObj;
    ProConstants proConstants;

    AuthObject authObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_active);

        setProperties();

    }

    private void setProperties(){
        formatter = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");

        statusArr = getResources().getStringArray(R.array.req_status);
        permissionCheckObj = new PermissionCheckObj(this);
        progressBarObj = new ProgressBarObject(this);
        proConstants = new ProConstants();
        authObj = new AuthObject();

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

        //ratingCon = (LinearLayout) findViewById(R.id.ratingCon);
        contactDBtn = (LinearLayout) findViewById(R.id.contactDBtn);
        contactDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialog();
            }
        });

        requestBtn = (LinearLayout) findViewById(R.id.requestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView innerText = (TextView) requestBtn.getChildAt(0);
                innerText.setText(R.string.par_pickup);
                //Log.e("checkId", innerText.getText().toString());
            }
        });
        /*subRatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//*if(saveStars > 0){
                    progressBarObj.showProgressDialog();
                    userActiveRequestModel.completeJob(saveStars, new ObjectInterfaces.SimpleCallback() {
                        @Override
                        public void onSuccess(boolean status, String err) {
                            progressBarObj.hideProgressDialog();
                            if(status){
                                finishAffinity();
                                startActivity(new Intent(RequestActiveActivity.this, MapActivity.class));
                            }else{
                                Toast.makeText(RequestActiveActivity.this, err, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }else{
                    Toast.makeText(RequestActiveActivity.this, "Please rate your Rider!", Toast.LENGTH_SHORT).show();
                }*//*

            }
        });*/

        /*star_rating_con = (LinearLayout) findViewById(R.id.star_rating_con);
        for(int i=0; i<star_rating_con.getChildCount(); i++){
            final int ind = i+1;
            ImageView star = (ImageView) star_rating_con.getChildAt(i);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveStars = ind;
                    act_stars(ind);
                }
            });
        }*/

        progressBarObj.showProgressDialog();
        userActiveRequestModel.getRequestData(authObj.authUid, new DBCallbacks.CompleteDSListener() {
            @Override
            public void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot) {
                progressBarObj.hideProgressDialog();
                if(status){
                    UserActiveRequest userActiveRequestDataSnap = dataSnapshot.getChildren().iterator().next().getValue(UserActiveRequest.class);
                    active_time = userActiveRequestDataSnap.active_time;
                    complete_time = userActiveRequestDataSnap.complete_time;
                    reqID = userActiveRequestDataSnap.req_id;
                    statusChangeUI(userActiveRequestDataSnap.status);
                }else{
                    Toast.makeText(RequestActiveActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*userActiveRequestModel.userActReqStatusCall(new ObjectInterfaces.UserActReqStatusCallback() {
            @Override
            public void onSuccess(boolean status, String err, DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    reset_stars();
                    saveStars = 0;
                    reqDriverUID = dataSnapshot.child("driver_uid").getValue().toString();
                    String reqStatus = dataSnapshot.child("status").getValue().toString();
                    active_time = (Long) dataSnapshot.child("active_time").getValue();
                    complete_time = (Long) dataSnapshot.child("complete_time").getValue();
                    statusChangeUI(reqStatus);
                    if(firstRes){
                        userInfoModel.getUserInfo(reqDriverUID, new UserInfo.UserCallback() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot, String errMsg) {
                                progressBarObj.hideProgressDialog();
                                if(errMsg != null){
                                    Toast.makeText(RequestActiveActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                                }else{
                                    reqDriverMob = userInfoModel.getMobNo();
                                }
                            }
                        });
                        firstRes = false;
                    }
                }
            }
        });*/

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

    private void statusChangeUI(String reqSts){
        if(reqSts != null && reqSts.equals(statusArr[0])){
            stsUI_inAct(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[1])){
            stsUI_act(stsPendingCon);
            stsUI_inAct(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            setTime(active_time_TV, "00:00");
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[2])){
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_inAct(stsCompleteCon);

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, "00:00");
        }else if(reqSts != null && reqSts.equals(statusArr[3])){
            stsUI_act(stsPendingCon);
            stsUI_act(stsActiveCon);
            stsUI_act(stsCompleteCon);

            setTime(active_time_TV, convertDate(active_time));
            setTime(complete_time_TV, convertDate(complete_time));
        }else{
            Log.e("CheckCallBacks", "Req No Status Found!");
        }

        /*if(reqSts != null && reqSts.equals(statusArr[3])){
            setRatingCon_act();
        }else{
            setRatingCon_inAct();
        }*/
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

    private void setRatingCon_act(){
        ratingCon.setVisibility(View.VISIBLE);
        contactDBtn.setVisibility(View.GONE);
    }

    private void setRatingCon_inAct(){
        ratingCon.setVisibility(View.GONE);
        contactDBtn.setVisibility(View.VISIBLE);
    }

    /*private void act_stars(final int stars){
        reset_stars();
        for(int i=0; i<stars; i++){
            ImageView star = (ImageView) star_rating_con.getChildAt(i);
            star.setBackground(getResources().getDrawable(R.drawable.star_active, null));
        }
    }
    private void reset_stars(){
        for(int i=0; i<star_rating_con.getChildCount(); i++){
            ImageView star = (ImageView) star_rating_con.getChildAt(i);
            star.setBackground(getResources().getDrawable(R.drawable.star_inactive, null));
        }
    }*/

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
        callIntent.setData(Uri.parse("tel:+"+reqDriverMob));
        startActivity(callIntent);
    }

    private void smsDriverIntent(){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:+"+reqDriverMob)));
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
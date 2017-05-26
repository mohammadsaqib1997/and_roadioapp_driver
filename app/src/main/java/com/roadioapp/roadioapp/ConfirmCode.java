package com.roadioapp.roadioapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ConfirmCode extends Activity {

    EditText mobVerCode;
    LinearLayout sendCodebtn;
    TextView resendCodeTV;

    String mobNumberStr, passStr, tokenStr = "", DOMAIN;

    BroadcastReceiver broadcastReceiver=null;
    HashMap userData, tokenData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);

        DOMAIN = getResources().getString(R.string.app_api_domain);

        mobVerCode = (EditText) findViewById(R.id.mobVerCode);
        sendCodebtn = (LinearLayout) findViewById(R.id.sendCodebtn);
        resendCodeTV = (TextView) findViewById(R.id.resendCodeTV);

        SessionManager saveUserInfo = new SessionManager(ConfirmCode.this);
        tokenData = saveUserInfo.getRegPref();
        userData = saveUserInfo.getUserTamp();
        tokenStr = tokenData.get("token").toString();

        sendCodebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobVerCodeStr = mobVerCode.getText().toString();
                String err = "";
                if(mobVerCodeStr.isEmpty()){
                    err = "Code is Required!";
                }

                if(!err.isEmpty()){
                    Toast.makeText(ConfirmCode.this, err, Toast.LENGTH_SHORT).show();
                }else{
                    View view = ConfirmCode.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    tokenCheck(mobVerCodeStr);
                }
            }
        });

        resendCodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobNumberStr = userData.get("phone_num").toString();
                passStr = userData.get("password").toString();
                reqSend(mobNumberStr, passStr);
            }
        });

        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg1, Intent intent) {
                Toast.makeText(arg1, "SMS Received", Toast.LENGTH_LONG).show();
                processReceivedSMS(intent);
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SessionManager checkSession = new SessionManager(ConfirmCode.this);
        if(checkSession.isValidate()){
            finish();
            startActivity(new Intent(ConfirmCode.this, BasicInfo.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver.isOrderedBroadcast()){
            unregisterReceiver(broadcastReceiver);
        }
    }

    public void processReceivedSMS(Intent intent){
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage;
        boolean strMessage = false;
        String code = "";

        for(int i=0; i<pdus.length;i++){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                String format = bundle.getString("format");
                smsMessage = SmsMessage.createFromPdu((byte[])pdus[i], format);
            }else{
                smsMessage = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
//            String sendNumber = smsMessage.getOriginatingAddress();
//            if(sendNumber.equals("+1978034496") || sendNumber.equals("978034496")){
                String receivedMsg = smsMessage.getMessageBody();
                try {
                    String[] msgSplit = receivedMsg.split("\"");
                    code = msgSplit[2];
                    if (code.length() == 6) {
                        strMessage = true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
//            }

        }
        if(strMessage){
            tokenCheck(code);
        }

    }

    private void tokenCheck(String pincode){
        SessionManager saveUserInfo = new SessionManager(ConfirmCode.this);
        HashMap tokenObj = saveUserInfo.getRegPref();
        String token = tokenObj.get("token").toString();

        if(pincode.equals(token)){
            saveUserInfo.setLogIn(true);
            finish();
            startActivity(new Intent(ConfirmCode.this, BasicInfo.class));
        }else{
            Toast.makeText(ConfirmCode.this, "Code is not valid!", Toast.LENGTH_SHORT).show();
        }

    }

    private void reqSend(final String mobNumber, final String pass){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String reqURL = DOMAIN+"/validate";

        JSONObject params = new JSONObject();
        try {
            params.put("phone_num", mobNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqURL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    if(response.getString("status").equals("ok")){
                        String token = response.getString("token");
                        SessionManager saveUserInfo = new SessionManager(ConfirmCode.this);
                        saveUserInfo.setRegPref(token, mobNumber);
                        saveUserInfo.saveUserTamp(pass, mobNumber);
                        Toast.makeText(ConfirmCode.this, "Successfully Code Send.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ConfirmCode.this, "Bad Request Params", Toast.LENGTH_SHORT).show();
                        Log.e("Response", response.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("ErrorResponse", error.toString());
                Toast.makeText(ConfirmCode.this, "Bad Request", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

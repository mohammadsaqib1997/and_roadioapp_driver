package com.roadioapp.roadioapp;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class SignUp extends AppCompatActivity {

    EditText mobNumber, passET, rePassET;
    String mobNumberStr, passETStr, rePassETStr;
    String DOMAIN;
    ArrayList<String> numbers;
    TextView mloginBtn;
    ImageView mBack;

    final private int PERM_REQUEST_CODE_DRAW_OVERLAYS = 123;
    final private int REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS = 124;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        numbers = new ArrayList<String>();

        DOMAIN = getResources().getString(R.string.app_api_domain);

        mobNumber = (EditText) findViewById(R.id.mobNumber);
        passET = (EditText) findViewById(R.id.passET);
        rePassET = (EditText) findViewById(R.id.rePassET);

        mloginBtn = (TextView) findViewById(R.id.loginBtn);

        mloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(SignUp.this, LoginActivity.class));
            }
        });

        mBack = (ImageView) findViewById(R.id.back_to_signin);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(SignUp.this, MainActivity.class));
            }
        });

        LinearLayout sendCodebtn = (LinearLayout) findViewById(R.id.sendConCodeBtn);

        sendCodebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobNumberStr = "92"+mobNumber.getText().toString();
                passETStr = passET.getText().toString();
                rePassETStr = rePassET.getText().toString();

                String err = "";
                if(mobNumberStr.length() < 10){
                    err = "Invalid Mobile Number!";
                }else if(passETStr.isEmpty()){
                    err = "Required Password";
                }else if(passETStr.length() < 5){
                    err = "Password is too short";
                }else if(rePassETStr.isEmpty()){
                    err = "Required Confirm Password";
                }else if(!passETStr.equals(rePassETStr)){
                    err = "Confirm Password is not match!";
                }

                if(!err.isEmpty()){
                    Toast.makeText(SignUp.this, err, Toast.LENGTH_SHORT).show();
                }else{
                    View view = SignUp.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    reqSend(mobNumberStr, passETStr);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        receivedMsgPer();
    }

    private boolean receivedMsgPer(){
        if (android.os.Build.VERSION.SDK_INT >= 23){
            if (!Settings.canDrawOverlays(SignUp.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
                return false;
            }
            int hasInternetPermission = checkSelfPermission(Manifest.permission.RECEIVE_SMS);
            if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.RECEIVE_SMS},
                        REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS);
                return false;
            }else{
                return true;
            }
        }
        return true;
    }

    private void reqSend(final String mobNumber, final String pass){
        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(SignUp.this);
        String reqURL = DOMAIN+"/validate";

        JSONObject params = new JSONObject();
        try {
            params.put("phone_num", mobNumber);
            params.put("type", "driver");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqURL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                //Log.e("Response", response.toString());
                try {
                    if(response.getString("status").equals("ok")){
                        String token = response.getString("token");
                        SessionManager saveUserInfo = new SessionManager(SignUp.this);
                        saveUserInfo.setRegPref(token, mobNumber);
                        saveUserInfo.saveUserTamp(pass, mobNumber);
                        Intent moveInboxIntent = new Intent(SignUp.this, ConfirmCode.class);
                        startActivity(moveInboxIntent);
                    }else{
                        Toast.makeText(SignUp.this, response.getString("message"), Toast.LENGTH_LONG).show();
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
                Toast.makeText(SignUp.this, "Bad Request", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS:
                if(grantResults.length > 0){
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
                        Toast.makeText(SignUp.this, "Received SMS Permission Access", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // Permission Denied
                        Toast.makeText(SignUp.this, "Received SMS Permission Denied", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(SignUp.this, "Overlay Permission Access", Toast.LENGTH_SHORT)
                            .show();
                    receivedMsgPer();
                }
                else {
                    Toast.makeText(SignUp.this, "Overlay Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.roadioapp.roadioapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicInfo extends AppCompatActivity {

    private Spinner mVehicle;
    private TextView mFirstName, mLastName, mEmail, loginBtn;
    private LinearLayout mCompleteSignUp;
    private String DOMAIN;
    private FirebaseAuth mAuth;
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);


        setProperties();
        LinearLayout mCompleteSignUp = (LinearLayout) findViewById(R.id.completeSignUp);

        mCompleteSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                completeForm();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager setSession = new SessionManager(BasicInfo.this);
                setSession.clearAllSess();
                finish();
                startActivity(new Intent(BasicInfo.this, LoginActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!sessionManager.isValidate()){
            finish();
            startActivity(new Intent(BasicInfo.this, ConfirmCode.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void completeForm() {

        String firstName = mFirstName.getText().toString();
        String lastName = mLastName.getText().toString();
        String email = mEmail.getText().toString();
        String vehicle = String.valueOf(mVehicle.getSelectedItem());

        if(TextUtils.isEmpty(firstName)) {
            Toast.makeText(BasicInfo.this, "First Name is required", Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(lastName)) {
            Toast.makeText(BasicInfo.this, "Last Name is required", Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(email)) {
            Toast.makeText(BasicInfo.this, "Email is required", Toast.LENGTH_LONG).show();
        } else if(!isValidEmail(email)){
            Toast.makeText(BasicInfo.this, "Email is invalid", Toast.LENGTH_LONG).show();
        }else if(vehicle.equals("Select Vehicle") ){
            Toast.makeText(BasicInfo.this, "Vehicle is required", Toast.LENGTH_LONG).show();
        } else {

            HashMap userTamp = sessionManager.getUserTamp();

            JSONObject params = new JSONObject();
            try{
                params.put("first_name", firstName);
                params.put("last_name", lastName);
                params.put("email", email);
                params.put("vehicle", vehicle);
                params.put("password", userTamp.get("password"));
                params.put("mob_no", userTamp.get("phone_num"));
                params.put("type", "driver");
            } catch (JSONException e){
                e.printStackTrace();
            }

            sendFormData(params);


        }

    }

    private void sendFormData(JSONObject params) {

        final ProgressDialog progressDialog = new ProgressDialog(BasicInfo.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(BasicInfo.this);

        String reqUrl = DOMAIN + "/register";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqUrl, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getString("status").equals("ok")){
                        //Log.e("message", response + "");
                        mAuth.signInWithCustomToken(response.getString("token")).addOnCompleteListener(BasicInfo.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                SessionManager setSess = new SessionManager(BasicInfo.this);
                                setSess.clearAllSess();
                                progressDialog.dismiss();
                                finish();
                                startActivity(new Intent(BasicInfo.this, MapActivity.class));
                            }
                        });
                    } else if(response.getString("status").equals("failed")) {
                        progressDialog.dismiss();
                        Toast.makeText(BasicInfo.this, response.getString("message"), Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("ErrorResponse", error.toString());
                Toast.makeText(BasicInfo.this, "Bad Request", Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }

    public boolean isValidEmail(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private void setProperties() {
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        DOMAIN = getString(R.string.app_api_domain);
        mVehicle = (Spinner) findViewById(R.id.vehicle);
        mFirstName = (TextView) findViewById(R.id.firstName);
        mLastName = (TextView) findViewById(R.id.lastName);
        mEmail = (TextView) findViewById(R.id.emailAddress);
        loginBtn = (TextView) findViewById(R.id.loginBtn);
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

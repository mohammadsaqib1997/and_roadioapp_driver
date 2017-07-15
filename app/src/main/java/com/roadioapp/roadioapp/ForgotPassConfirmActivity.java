package com.roadioapp.roadioapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.roadioapp.roadioapp.LoginActivity;
import com.roadioapp.roadioapp.mObjects.ButtonEffects;
import com.roadioapp.roadioapp.mObjects.SaveLocalMemory;
import com.roadioapp.roadioapp.mObjects.ProgressBarObject;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPassConfirmActivity extends AppCompatActivity {

    private Activity activity;

    private ButtonEffects buttonEffects;
    private ProgressBarObject progressBarObj;
    private String DOMAIN;
    private SaveLocalMemory saveLocalMemory;

    private LinearLayout subBtn;
    private EditText token, password, retypePassword;
    private String tokenStr, passwordStr, retypePasswordStr;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pass_new);
        activity = this;

        DOMAIN = getString(R.string.app_api_domain);
        progressBarObj = new ProgressBarObject(activity);
        buttonEffects = new ButtonEffects(activity);
        saveLocalMemory = new SaveLocalMemory(activity).selectPref("forgotPass");

        token = (EditText) findViewById(R.id.token);
        password = (EditText) findViewById(R.id.password);
        retypePassword = (EditText) findViewById(R.id.retypePassword);

        subBtn = (LinearLayout) findViewById(R.id.subBtn);
        buttonEffects.btnEventEffRounded(subBtn);

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tokenStr = token.getText().toString();
                passwordStr = password.getText().toString();
                retypePasswordStr = retypePassword.getText().toString();

                String err = "";
                if(tokenStr.isEmpty()){
                    err = "Token is required!";
                }else if(tokenStr.length() < 6 && tokenStr.length() > 6){
                    err = "Token is invalid!";
                }else if(passwordStr.isEmpty()){
                    err = "Password is required!";
                }else if(passwordStr.length() < 6){
                    err = "Password is too short!";
                }else if(passwordStr.length() > 30){
                    err = "Password is too long!";
                }else if(!passwordStr.equals(retypePasswordStr)){
                    err = "Re-Type Password not match!";
                }

                if(!err.isEmpty()){
                    Toast.makeText(activity, err, Toast.LENGTH_SHORT).show();
                }else{
                    newPassReqSend();
                }
            }
        });
    }

    private void newPassReqSend() {
        progressBarObj.showProgressDialog();
        JSONObject params = new JSONObject();
        try{
            params.put("mob_no", saveLocalMemory.getVal("mob_no"));
            params.put("token", tokenStr);
            params.put("password", passwordStr);
            params.put("type", "driver");
        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        String reqUrl = DOMAIN + "/new_password";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqUrl, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getString("status").equals("ok")){
                        progressBarObj.hideProgressDialog();
                        saveLocalMemory.editPref().clearPref().commitPref();
                        switchAct(LoginActivity.class);
                        Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        progressBarObj.hideProgressDialog();
                        Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    progressBarObj.hideProgressDialog();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBarObj.hideProgressDialog();
                Log.e("ErrorResponse", error.toString());
                Toast.makeText(activity, "Bad Request", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(300000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(saveLocalMemory.getVal("mob_no").isEmpty()){
            switchAct(LoginActivity.class);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switchAct(LoginActivity.class);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchAct(Class classname) {
        finish();
        startActivity(new Intent(activity, classname));
    }
}
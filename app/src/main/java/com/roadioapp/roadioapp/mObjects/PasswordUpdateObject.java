package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.google.firebase.auth.FirebaseAuth;
import com.roadioapp.roadioapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PasswordUpdateObject {

    private Activity activity;
    private ButtonEffects btnEffects;
    private ProgressBarObject progressBarObj;
    private String DOMAIN;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    public PasswordUpdateObject(Activity activity) {
        this.activity = activity;
        btnEffects = new ButtonEffects(activity);
        progressBarObj = new ProgressBarObject(activity);
        DOMAIN = activity.getString(R.string.app_api_domain);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(activity);
    }

    private String passValid(EditText curr_pass, EditText pass1, EditText pass2){
        String curr_passStr = curr_pass.getText().toString();
        String pass1Str = pass1.getText().toString();
        String pass2Str = pass2.getText().toString();
        if(curr_passStr.isEmpty()){
            return "Current Password Empty!";
        }else if(pass1Str.equals(curr_passStr)){
            return "Current Password and New Password is match!";
        }else if(pass1Str.isEmpty()){
            return "New Password Empty!";
        }else if(pass2Str.isEmpty()){
            return "Re-Type Password Empty!";
        }else if(!pass1Str.equals(pass2Str)){
            return "Password Not Match!";
        }else{
            return "";
        }
    }

    public void showUpdatePassDialog(){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upd_pass_dialog);
        LinearLayout subBtn = (LinearLayout) dialog.findViewById(R.id.subBtn);
        btnEffects.btnEventEffRounded(subBtn);

        final EditText curr_pass = (EditText) dialog.findViewById(R.id.curr_pass);
        final EditText pass1 = (EditText) dialog.findViewById(R.id.new_pass);
        final EditText pass2 = (EditText) dialog.findViewById(R.id.re_new_pass);

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPass = passValid(curr_pass, pass1, pass2);
                if(checkPass.isEmpty()){
                    passUpdateReq(pass1.getText().toString(), curr_pass.getText().toString(), dialog);
                }else{
                    Toast.makeText(activity, checkPass, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void passUpdateReq(String newPass, String oldPass, final Dialog dialog){
        if(mAuth.getCurrentUser() != null){
            progressBarObj.showProgressDialog();
            JSONObject params = new JSONObject();
            try{
                params.put("uid", mAuth.getCurrentUser().getUid());
                params.put("new_pass", newPass);
                params.put("old_pass", oldPass);
                params.put("type", "driver");

            } catch (JSONException e){
                e.printStackTrace();
            }
            String reqUrl = DOMAIN + "/update_password";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressBarObj.hideProgressDialog();
                    try{
                        if(response.getString("status").equals("ok")){
                            Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else if(response.getString("status").equals("failed")) {
                            Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                            Log.e("message", response + "");
                        }
                    } catch (JSONException e){
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

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        }else{
            Toast.makeText(activity, "Auth not found!", Toast.LENGTH_SHORT).show();
        }
    }

}
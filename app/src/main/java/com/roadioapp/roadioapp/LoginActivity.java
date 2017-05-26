package com.roadioapp.roadioapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private Button mLoginButton;
    private EditText mEmail;
    private EditText mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mSignup;
    private ImageView mBack;
    String DOMAIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };

        mSignup = (TextView) findViewById(R.id.signUp);

        mLoginButton = (Button) findViewById(R.id.loginBtn);

        DOMAIN = getString(R.string.app_api_domain);

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LoginActivity.this, SignUp.class));
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });

        mBack = (ImageView) findViewById(R.id.back_to_signin);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
        }else{
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            finish();
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
        }
    }

    private void startSignIn() {

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(LoginActivity.this, "Email & password is required.", Toast.LENGTH_LONG).show();
        } else {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            JSONObject params = new JSONObject();
            try{

                params.put("email", email);
                params.put("password", password);
                params.put("type", "driver");

            } catch (JSONException e){
                e.printStackTrace();
            }
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            String reqUrl = DOMAIN + "/login";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reqUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if(response.getString("status").equals("ok")){
                            //Log.e("message", response + "");
                            mAuth.signInWithCustomToken(response.getString("token")).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage()+"",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else if(response.getString("status").equals("failed")) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, "Bad Request", Toast.LENGTH_SHORT).show();
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

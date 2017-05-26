package com.roadioapp.roadioapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RequestDetailsPopup extends AppCompatActivity {

    ImageView parcel_img;
    TextView from_loc, to_loc, distance, time;
    LinearLayout place_bid_btn;
    EditText bid_field;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase, driverBidsCollection;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details_popup);

        setProperties();

        Intent intentGrab = getIntent();
        if(!intentGrab.hasExtra("data")){
            finish();
        }
        UserRequests intentData = intentGrab.getParcelableExtra("data");
        Picasso.with(this).load(intentData.parcelUri).into(parcel_img);
        from_loc.setText(intentData.orgText);
        to_loc.setText(intentData.desText);
        distance.setText("Distance: "+intentData.disText);
        time.setText("Time: "+intentData.durText);

        final String req_id = intentData.id;

        place_bid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setParcelBid(req_id);

            }
        });

        driverBidsCollection.child(req_id).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("readDataTags", dataSnapshot.getValue()+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setParcelBid(String req_id){

        final String bidFieldStr = bid_field.getText().toString();

        String errMsg = "";

        if(bidFieldStr.isEmpty()){
            errMsg = "Enter Bid Amount!";
        }else if(uid == null){
            errMsg = "User not recognize!";
        }
        if(!errMsg.isEmpty()){
            Toast.makeText(RequestDetailsPopup.this, errMsg, Toast.LENGTH_SHORT).show();
        }else{
            Map<String, Object> dataSend = new HashMap<String, Object>();
            dataSend.put("amount", bidFieldStr);

            showProgressBar();
            driverBidsCollection.child(req_id).child(uid).setValue(dataSend, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    hideProgressBar();
                    if(databaseError != null){
                        Toast.makeText(RequestDetailsPopup.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RequestDetailsPopup.this, "Your Bid is send", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    hideProgressBar();
                }
            });
        }

    }

    private void showProgressBar(){
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressBar(){
        progressDialog.dismiss();
    }

    private void setProperties(){
        progressDialog = new ProgressDialog(this);

        parcel_img = (ImageView) findViewById(R.id.parcel_img);
        from_loc = (TextView) findViewById(R.id.from_loc);
        to_loc = (TextView) findViewById(R.id.to_loc);
        distance = (TextView) findViewById(R.id.distance);
        time = (TextView) findViewById(R.id.time);
        place_bid_btn = (LinearLayout) findViewById(R.id.place_bid_btn);
        bid_field = (EditText) findViewById(R.id.bid_field);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        driverBidsCollection = mDatabase.child("driver_bids");
    }
}

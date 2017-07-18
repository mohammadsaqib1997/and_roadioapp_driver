package com.roadioapp.roadioapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mModels.UserRequest;
import com.roadioapp.roadioapp.mObjects.AuthObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class MyEarningsActivity extends AppCompatActivity {

    private Activity activity;

    private ImageView close_act_btn;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ViewReqItemAdapter viewReqItemAdapter;

    private LinkedHashMap<String, LinkedHashMap> dataLoad;
    private LinkedHashMap<String, String> itemData;

    private ProgressBar progressBar;

    private AuthObject mAuthObj;
    private UserRequest userRequestsModel;
    private List<String> loadDataKeys;
    private JSONObject loadItems;
    private int loadDataInc;
    //Firebase variables
    DatabaseReference userRequests, completeRequests, driverBids, userLiveRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_earnings);

        activity = this;

        close_act_btn = (ImageView) findViewById(R.id.close_act_btn);
        close_act_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.listRequestItems);

        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        dataLoad = new LinkedHashMap<String, LinkedHashMap>();
        viewReqItemAdapter = new ViewReqItemAdapter(activity, dataLoad);
        mRecyclerView.setAdapter(viewReqItemAdapter);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        userRequests = mDatabase.child("user_requests");
        completeRequests = mDatabase.child("complete_requests");
        driverBids = mDatabase.child("driver_bids");
        userLiveRequests = mDatabase.child("user_live_requests");

        mAuthObj = new AuthObject(activity);

        getRequestList();
    }

    private void getRequestList(){
        if(mAuthObj.isLoginUser()){
            completeRequests.orderByChild("driver_uid").equalTo(mAuthObj.authUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        loadItems = new JSONObject();
                        loadDataKeys = new ArrayList<>();
                        final long childLength = dataSnapshot.getChildrenCount();
                        loadDataInc = 0;
                        for (final DataSnapshot child: dataSnapshot.getChildren()){
                            loadDataKeys.add(child.getKey());
                            userRequests.child(child.child("client_uid").getValue()+"").child(child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot reqSnap) {
                                    driverBids.child(child.getKey()).child(mAuthObj.authUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot bidSnap) {
                                            loadItems = setValueInArray(loadItems, child.getKey(), reqSnap, bidSnap.child("amount").getValue()+"", "Complete");
                                            loadDataInc++;
                                            finalLoadData(childLength, loadDataInc, loadDataKeys, loadItems);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            progressBar.setVisibility(View.GONE);
                                            Log.e("DBError", databaseError.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("DBError", databaseError.getMessage());
                                }
                            });
                        }
                    }else{
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("DBError", databaseError.getMessage());
                }
            });
        }else{
            Toast.makeText(activity, "Auth not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void reqItemUpdate(final DataSnapshot snapshot, String amount, String status){
        userRequestsModel = snapshot.getValue(UserRequest.class);
        itemData = new LinkedHashMap<String, String>();
        itemData.put("id", snapshot.getKey());
        itemData.put("origin", userRequestsModel.orgText);
        itemData.put("destination", userRequestsModel.desText);
        itemData.put("time", userRequestsModel.getFormatDate());
        itemData.put("parcel_thumb", userRequestsModel.parcelThmb);
        itemData.put("amount", amount);
        itemData.put("status", status);
        dataLoad.put(snapshot.getKey(), itemData);
        viewReqItemAdapter.notifyDataSetChanged();
        if(mRecyclerView.getVisibility() != View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void finalLoadData(long childLength, int inc, List<String> dataKeys, JSONObject loadedData){
        if(childLength == inc){
            Collections.sort(dataKeys);
            Collections.reverse(dataKeys);
            JSONObject item;
            for(int i=0; i<dataKeys.size(); i++){
                try {
                    String key = dataKeys.get(i);
                    item = (JSONObject) loadedData.get(key);
                    reqItemUpdate((DataSnapshot) item.get("reqSnap"), (String) item.get("amount"), (String) item.get("reqStatus"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private JSONObject setValueInArray(JSONObject loadArr, String key, DataSnapshot reqSnap, String amount, String reqStatus){
        try {
            loadArr.put(key, new JSONObject().put("reqSnap", reqSnap).put("amount", amount).put("reqStatus", reqStatus));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loadArr;
    }
}
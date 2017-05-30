package com.roadioapp.roadioapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roadioapp.roadioapp.mObjects.RetDataObject;

import java.util.ArrayList;
import java.util.List;

public class RequestRecyleView extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerAdapter mAdapter;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mfirebaseDatabase, mcollectionUserRequests;
    List<UserRequests> mList;
    ValueEventListener postListener;

    private RetDataObject retDataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_recyle_view);

        ImageView closeBtn = (ImageView) findViewById(R.id.close_act_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.requestsRecyler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mList = new ArrayList<>();
        mAdapter = new RecyclerAdapter(mList, RequestRecyleView.this);
        recyclerView.setAdapter(mAdapter);

        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();

        retDataObj = new RetDataObject(this, mfirebaseDatabase);

        mcollectionUserRequests = mfirebaseDatabase.child("user_live_requests");

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //UserRequests requests;
                mList.clear();

                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                for(DataSnapshot request: dataSnapshot.getChildren()){
                    String reqUid = request.getKey();
                    String reqId = request.child("reqId").getValue()+"";

                    retDataObj.getReqSnapshotListener("user_requests/"+reqUid+"/"+reqId, new RetDataObject.RetDataSnapCallback() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            UserRequests requests = dataSnapshot.getValue(UserRequests.class);
                            mList.add(requests);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w("Canceled", "loadPost:onCancelled", databaseError.toException());
            }
        };

        mcollectionUserRequests.orderByKey().limitToLast(15).addValueEventListener(postListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(RequestRecyleView.this, MainActivity.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(postListener != null){
            mcollectionUserRequests.removeEventListener(postListener);
        }
    }
}

package com.roadioapp.roadioapp;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.roadioapp.roadioapp.transforms.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ViewReqItemAdapter extends RecyclerView.Adapter<ViewReqItemAdapter.ViewHolder> {

    private LinkedHashMap<String, LinkedHashMap> saveData;
    private Activity activity;

    private String reqId;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView orgTV, desTV, timeTV, amountTV, reqStatusTV;
        public ImageView parcelImgThb;

        public ViewHolder(View v) {
            super(v);
            orgTV = (TextView) v.findViewById(R.id.orgTV);
            desTV = (TextView) v.findViewById(R.id.desTV);
            timeTV = (TextView) v.findViewById(R.id.timeTV);
            amountTV = (TextView) v.findViewById(R.id.amountTV);
            reqStatusTV = (TextView) v.findViewById(R.id.reqStatusTV);
            parcelImgThb = (ImageView) v.findViewById(R.id.parcelImgThb);
        }
    }

    public ViewReqItemAdapter(Activity mAct, LinkedHashMap<String, LinkedHashMap> dataSet) {
        this.saveData = dataSet;
        this.activity = mAct;
    }

    @Override
    public ViewReqItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item_view_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewReqItemAdapter.ViewHolder holder, final int position) {
        final LinkedHashMap data = (new ArrayList<LinkedHashMap>(saveData.values())).get(position);
        holder.orgTV.setText(data.get("origin")+"");
        holder.desTV.setText(data.get("destination")+"");
        holder.timeTV.setText(data.get("time")+"");
        holder.amountTV.setText("PKR "+data.get("amount"));
        holder.reqStatusTV.setText(""+data.get("status"));

        Picasso.with(activity).load(data.get("parcel_thumb")+"").placeholder(R.drawable.circle_img).transform(new CircleTransform()).into(holder.parcelImgThb);
    }

    @Override
    public int getItemCount() {
        return saveData.size();
    }


    public void setReqId(String reqId){
        this.reqId = reqId;
    }
}
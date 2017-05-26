package com.roadioapp.roadioapp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<UserRequests> mDataSet;
    private Context context;
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_child, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        holder.mTextView.setText(mDataSet[position]);

        Date date = new Date(mDataSet.get(position).createdAt);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Picasso.with(context).load(mDataSet.get(position).parcelThmb)
                .transform(new CircleTransform())
                .into(holder.parcelImg);
        holder.from_loc.setText("From: " + mDataSet.get(position).orgText);
        holder.to_loc.setText("To: " + mDataSet.get(position).desText);
        holder.created_at.setText(dateFormat.format(date));
        holder.distance.setText("Distance: " + mDataSet.get(position).disText);
        holder.list_item_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RequestDetailsPopup.class);
                intent.putExtra("data", (Parcelable) mDataSet.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    RecyclerAdapter(List<UserRequests> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView parcelImg;
        TextView from_loc, to_loc, created_at, distance;
        LinearLayout list_item_con;

        ViewHolder(View itemView) {
            super(itemView);
            list_item_con = (LinearLayout) itemView.findViewById(R.id.list_item_con);
            parcelImg = (ImageView) itemView.findViewById(R.id.parcel_img);
            from_loc = (TextView) itemView.findViewById(R.id.from_loc);
            to_loc = (TextView) itemView.findViewById(R.id.to_loc);
            created_at = (TextView) itemView.findViewById(R.id.created_at);
            distance = (TextView) itemView.findViewById(R.id.distance);

        }
    }
}

package com.roadioapp.roadioapp;

import android.os.Parcel;
import android.os.Parcelable;

public class UserRequests implements Parcelable {

    //public double orgLat, orgLng, desLat, desLng;
    public String id, desText, orgText, parcelUri, parcelThmb, vecType, orgLat, orgLng, desLat, desLng, durText, disText;
    public long createdAt;

    public UserRequests() {

    }

    protected UserRequests(Parcel in) {
        id = in.readString();
        desText = in.readString();
        orgText = in.readString();
        parcelUri = in.readString();
        parcelThmb = in.readString();
        vecType = in.readString();
        orgLat = in.readString();
        orgLng = in.readString();
        desLat = in.readString();
        desLng = in.readString();
        durText = in.readString();
        disText = in.readString();
        createdAt = in.readLong();
    }

    public static final Creator<UserRequests> CREATOR = new Creator<UserRequests>() {
        @Override
        public UserRequests createFromParcel(Parcel in) {
            return new UserRequests(in);
        }

        @Override
        public UserRequests[] newArray(int size) {
            return new UserRequests[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(desText);
        dest.writeString(orgText);
        dest.writeString(parcelUri);
        dest.writeString(parcelThmb);
        dest.writeString(vecType);
        dest.writeString(orgLat);
        dest.writeString(orgLng);
        dest.writeString(desLat);
        dest.writeString(desLng);
        dest.writeString(durText);
        dest.writeString(disText);
        dest.writeLong(createdAt);
    }
}

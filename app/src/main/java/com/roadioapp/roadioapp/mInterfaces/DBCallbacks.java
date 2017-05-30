package com.roadioapp.roadioapp.mInterfaces;

import com.google.firebase.database.DataSnapshot;

public class DBCallbacks {
    public interface CompleteListener {
        void onSuccess(boolean status, String msg);
    }

    public interface CompleteDSListener {
        void onSuccess(boolean status, String msg, DataSnapshot dataSnapshot);
    }
}

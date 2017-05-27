package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.app.ProgressDialog;

public class ProgressBarObject {

    private ProgressDialog progressDialog;

    public ProgressBarObject(Activity act){
        progressDialog = new ProgressDialog(act);
    }

    public void showProgressDialog() {
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        progressDialog.dismiss();
    }

}

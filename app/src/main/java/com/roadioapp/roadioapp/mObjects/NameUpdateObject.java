package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.roadioapp.roadioapp.R;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.UserInfo;

public class NameUpdateObject {

    private Activity activity;
    private ButtonEffects btnEffects;
    private ProgressBarObject progressBarObj;
    private UserInfo userInfoModel;

    private String first_name_str, last_name_str;

    public NameUpdateObject(Activity activity) {
        this.activity = activity;
        btnEffects = new ButtonEffects(activity);
        progressBarObj = new ProgressBarObject(activity);
        userInfoModel = new UserInfo(activity);
    }

    private boolean fieldValid(String first_name, String last_name){
        String err;
        first_name_str = first_name.trim();
        last_name_str = last_name.trim();
        if(first_name_str.isEmpty()){
            err = "First Name is empty!";
        }else if(first_name_str.length() > 20 || first_name_str.length() < 3){
            err = "First Name must be between 3 and 20 chars long!";
        }else if(last_name_str.isEmpty()){
            err = "Last Name is empty!";
        }else if(last_name_str.length() > 20 || last_name_str.length() < 3){
            err = "Last Name must be between 3 and 20 chars long!";
        }else{
            err = "";
        }
        if(err.isEmpty()){
            return true;
        }else{
            Toast.makeText(activity, err, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void showUpdateNameDialog(){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upd_name_dialog);
        LinearLayout subBtn = (LinearLayout) dialog.findViewById(R.id.subBtn);
        btnEffects.btnEventEffRounded(subBtn);

        final EditText first_name = (EditText) dialog.findViewById(R.id.first_name);
        final EditText last_name = (EditText) dialog.findViewById(R.id.last_name);

        userInfoModel.getMyInfo(new DBCallbacks.CompleteListener() {
            @Override
            public void onSuccess(boolean status, String msg) {
                progressBarObj.hideProgressDialog();
                if(status){
                    first_name.setText(userInfoModel.getFirstName());
                    last_name.setText(userInfoModel.getLastName());
                }else{
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fieldValid(first_name.getText().toString(), last_name.getText().toString())){
                    progressBarObj.showProgressDialog();
                    userInfoModel.setMyName(first_name.getText().toString(), last_name.getText().toString(), new DBCallbacks.CompleteListener() {
                        @Override
                        public void onSuccess(boolean status, String msg) {
                            progressBarObj.hideProgressDialog();
                            if(status){
                                dialog.dismiss();
                                Toast.makeText(activity, "Name has been changed!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    first_name.setText(first_name_str);
                    last_name.setText(last_name_str);
                }
            }
        });
        dialog.show();
        progressBarObj.showProgressDialog();
    }
}
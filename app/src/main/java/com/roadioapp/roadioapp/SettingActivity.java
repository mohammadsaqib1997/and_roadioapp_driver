package com.roadioapp.roadioapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.roadioapp.roadioapp.mObjects.NameUpdateObject;
import com.roadioapp.roadioapp.mObjects.PasswordUpdateObject;
import com.roadioapp.roadioapp.mObjects.ProfileImageUpdateObject;

public class SettingActivity extends AppCompatActivity {

    private Activity activity;

    ImageView close_act_btn;
    LinearLayout dialog_btn_passUpd, dialog_btn_nameUpd, dialog_btn_profileImgUpd;

    //Objects
    PasswordUpdateObject passwordUpdateObj;
    NameUpdateObject nameUpdateObj;
    ProfileImageUpdateObject profileImageUpdateObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        activity = this;

        passwordUpdateObj = new PasswordUpdateObject(activity);
        nameUpdateObj = new NameUpdateObject(activity);
        profileImageUpdateObj = new ProfileImageUpdateObject(activity);

        close_act_btn = (ImageView) findViewById(R.id.close_act_btn);
        close_act_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialog_btn_passUpd = (LinearLayout) findViewById(R.id.dialog_btn_passUpd);
        dialog_btn_passUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordUpdateObj.showUpdatePassDialog();
            }
        });

        dialog_btn_nameUpd = (LinearLayout) findViewById(R.id.dialog_btn_nameUpd);
        dialog_btn_nameUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameUpdateObj.showUpdateNameDialog();
            }
        });

        dialog_btn_profileImgUpd = (LinearLayout) findViewById(R.id.dialog_btn_profileImgUpd);
        dialog_btn_profileImgUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageUpdateObj.showUpdateProfileImgDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == profileImageUpdateObj.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            profileImageUpdateObj.imageToBytes();
        } else if (requestCode == profileImageUpdateObj.REQUEST_IMAGE_PICK && resultCode == RESULT_OK){
            profileImageUpdateObj.selImageToBytes(data.getData());
        }
    }

}
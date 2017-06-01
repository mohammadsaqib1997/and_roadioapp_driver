package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roadioapp.roadioapp.R;

public class PopupObject {

    private Activity activity;

    private ButtonEffects buttonEffectsObj;
    private LinearLayout cancelBtn, yesBtn;

    public PopupObject(Activity activity){
        this.activity = activity;
        buttonEffectsObj = new ButtonEffects(activity);
    }

    public Dialog confirmPopup(){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_dialog);
        dialog.setCancelable(false);
        TextView msg = (TextView) dialog.findViewById(R.id.msgCon);
        msg.setText(R.string.confirm_dialog_str);
        cancelBtn = (LinearLayout) dialog.findViewById(R.id.cancelBtn);
        yesBtn = (LinearLayout) dialog.findViewById(R.id.yesBtn);
        buttonEffectsObj.btnEventEffRounded(cancelBtn);
        buttonEffectsObj.btnEventEffRounded(yesBtn);
        return dialog;
    }

    public void setCancelBtn(View.OnClickListener listener){
        cancelBtn.setOnClickListener(listener);
    }

    public void setYesBtn(View.OnClickListener listener){
        yesBtn.setOnClickListener(listener);
    }

}

package com.roadioapp.roadioapp.ActivityConstants;

import android.app.Activity;

public class MapActivityConstants {

    private Activity activity;

    public int PERM_REQUEST_LOCATION = 100;
    public int PERM_REQUEST_CALL = 200;
    public int PERM_REQUEST_STORAGE = 300;

    public MapActivityConstants(Activity act){
        this.activity = act;
    }

    public float pxFromDp(final float dp) {
        return dp * activity.getResources().getDisplayMetrics().density;
    }

}

package com.roadioapp.roadioapp.mModels;

import android.app.Activity;

public class User {

    public String email, first_name, last_name, mob_no, vehicle;

    private Activity activity;

    public User(){

    }

    public User(Activity activity){

    }

    public String getName(){
        return first_name+" "+last_name;
    }

    public String getVehicleByUID(String UID){



        return vehicle;
    }

}

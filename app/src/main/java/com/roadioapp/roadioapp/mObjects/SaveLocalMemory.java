package com.roadioapp.roadioapp.mObjects;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveLocalMemory {

    private Context context;

    private String PREF_NAME = "";
    private int PRIVATE_MODE = 0;
    private SharedPreferences selShPref;
    private SharedPreferences.Editor selShPrefEditor;

    public SaveLocalMemory(Context context){
        this.context = context;
    }

    public SaveLocalMemory selectPref(String prefName){
        this.PREF_NAME = prefName;
        selShPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return this;
    }

    public SaveLocalMemory editPref(){
        selShPrefEditor = selShPref.edit();
        return this;
    }

    public SaveLocalMemory putVal(String key, String val){
        selShPrefEditor.putString(key, val);
        return this;
    }

    public SaveLocalMemory removeVal(String key){
        selShPrefEditor.remove(key);
        return this;
    }

    public SaveLocalMemory clearPref(){
        selShPrefEditor.clear();
        return this;
    }

    public SaveLocalMemory commitPref(){
        selShPrefEditor.commit();
        return this;
    }

    public String getVal(String key){
        return selShPref.getString(key, "");
    }

    /*public String getPrefName(){
        return PREF_NAME;
    }*/

}
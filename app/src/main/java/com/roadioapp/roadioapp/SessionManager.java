package com.roadioapp.roadioapp;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences regPref, savePref;
    SharedPreferences.Editor regPrefEditor;
    SharedPreferences.Editor savePrefEditor;
    Context _context;

    private int PRIVATE_MODE = 0;
    private static final String REG_PREF_NAME = "RegisterSession";
    private static final String REG_PREF_NAME_2 = "RegisterSessionUser";

    private static  final String KEY_TOKEN = "token";
    private static  final String KEY_NUMBER = "phone_num";
    private static  final String KEY_PASSWORD = "password";
    private static  final String KEY_LOGIN = "login";

    SessionManager(Context context){
        _context = context;
        regPref = _context.getSharedPreferences(REG_PREF_NAME, PRIVATE_MODE);
        savePref = _context.getSharedPreferences(REG_PREF_NAME_2, PRIVATE_MODE);
        regPrefEditor = regPref.edit();
        savePrefEditor = savePref.edit();
    }

    void setRegPref(String token, String phone_number){
        regPrefEditor.putString(KEY_TOKEN, token);
        regPrefEditor.putString(KEY_NUMBER, phone_number);
        regPrefEditor.commit();
    }

    void saveUserTamp(String pass, String number){
        savePrefEditor.putString(KEY_PASSWORD, pass);
        savePrefEditor.putString(KEY_NUMBER, number);
        savePrefEditor.commit();
    }

    HashMap getUserTamp(){
        HashMap<String, String> params = new HashMap<>();
        params.put(KEY_PASSWORD, savePref.getString(KEY_PASSWORD, null));
        params.put(KEY_NUMBER, savePref.getString(KEY_NUMBER, null));
        return params;
    }



    void setLogIn(boolean bool){
        regPrefEditor.putBoolean(KEY_LOGIN, bool);
        regPrefEditor.commit();
    }

    boolean isValidate(){
        return regPref.getBoolean(KEY_LOGIN, false);
    }

    HashMap getRegPref(){
        HashMap<String, String> params = new HashMap<>();
        params.put(KEY_TOKEN, regPref.getString(KEY_TOKEN, null));
        params.put(KEY_NUMBER, regPref.getString(KEY_NUMBER, null));
        return params;
    }

    void clearAllSess(){
        regPrefEditor.clear();
        regPrefEditor.commit();
        savePrefEditor.clear();
        savePrefEditor.commit();
    }


}

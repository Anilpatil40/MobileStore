package com.swayam.mobilestore;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class MainUser {
    private static String username;
    private static SharedPreferences sharedPreferences;
    private static Context mContext;

    public static void initAccount(Context context){
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        username = sharedPreferences.getString("MainUser",null);
    }

    public static void login(String username) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPreferences.edit().remove("MainUser").putString("MainUser",username).apply();
        MainUser.username = username;
    }

    public static String getCurrentUser() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        username = sharedPreferences.getString("MainUser",null);
        return username;
    }

    public static void logout(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPreferences.edit().remove("MainUser").apply();
        MainUser.username = null;
    }
}

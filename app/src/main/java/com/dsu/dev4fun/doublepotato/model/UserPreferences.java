package com.dsu.dev4fun.doublepotato.model;


import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class UserPreferences {
    private static UserPreferences instance = new UserPreferences();

    private static final String PREFERENCES_KEY = "POTATO_PREFS";

    private static final String MEM_VAL = "MEM_VAL";
    private static final String CHANEL_VAL = "CHANEL_VAL";
    private static final String APP_VAL = "APP_VAL";
    private static final String SHAKE_VAL = "SHAKE_ON";
    private static final String SHAKE_SENSITIVITY_VAL = "SHAKE_SENSITIVITY_VAL";


    private SharedPreferences preferences;

    private int memoryAllocation;
    private String channelId;
    private String appId;
    private boolean volControlsEnabled;


    public static UserPreferences getInstance(){
        return instance;
    }

    private UserPreferences(){

    }
    public void initialize (Activity mainActivity) {
        preferences = mainActivity.getSharedPreferences(PREFERENCES_KEY, 0);
        memoryAllocation = preferences.getInt(MEM_VAL, 4960);
        channelId = preferences.getString(CHANEL_VAL, "UCHphW02wxlMKQGPJBkIei0w");
        appId = preferences.getString(APP_VAL, "AIzaSyB_TSE4ZG_9WFYCilHjWGAP1xYn9KwygA0");
        volControlsEnabled = preferences.getBoolean(SHAKE_VAL, true);
    }

    public void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(MEM_VAL, memoryAllocation);
        editor.putString(CHANEL_VAL, channelId);
        editor.putString(APP_VAL, appId);
        editor.putBoolean(SHAKE_VAL, volControlsEnabled);
        editor.commit();
    }


    public int getMemoryAllocation() {
        return memoryAllocation;
    }

    public void setMemoryAllocation(int memoryAllocation) {
        this.memoryAllocation = memoryAllocation;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isVolControlsEnabled() {
        return volControlsEnabled;
    }

    public void setVolControlsEnabled(boolean volControlsEnabled) {
        this.volControlsEnabled = volControlsEnabled;
    }
}

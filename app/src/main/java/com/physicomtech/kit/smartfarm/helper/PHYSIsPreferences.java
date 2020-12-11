package com.physicomtech.kit.smartfarm.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.physicomtech.kit.smartfarm.R;

public class PHYSIsPreferences {

    private static final String PREFERENCES_NAME = "PHYSIs_rebuild_preference";
    private static final String PHYSIs_SERIAL_NUM = "PHYSIs_SERIAL_NUM";
    private SharedPreferences pref;

    public PHYSIsPreferences(Context context){
        pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setPhysisSerialNumber(String data){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PHYSIs_SERIAL_NUM, data);
        editor.apply();
    }

    public String getPhysisSerialNumber(){
        return pref.getString(PHYSIs_SERIAL_NUM, null);
    }
}

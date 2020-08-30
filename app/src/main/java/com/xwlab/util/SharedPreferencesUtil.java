package com.xwlab.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private SharedPreferences shp;

    public SharedPreferencesUtil(Context context) {
        shp = context.getSharedPreferences("attendance", Context.MODE_PRIVATE);
    }

    public void saveString(String key, String name) {
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(key, name);
        editor.apply();
    }

    public void saveBoolean(String key, boolean name) {
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(key, name);
        editor.apply();
    }

    public void clearAll() {
        SharedPreferences.Editor editor = shp.edit();
        editor.clear();
        editor.apply();
    }

    public String loadString(String key,String def) {
        return shp.getString(key, def);
    }

    public String loadString(String key) {
        return shp.getString(key, null);
    }

    public boolean loadBoolean(String key,boolean def) {
        return shp.getBoolean(key, def);
    }
}

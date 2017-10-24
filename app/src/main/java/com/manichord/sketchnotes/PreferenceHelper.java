package com.manichord.sketchnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Clean, central access to getting/setting user preferences
 */

public class PreferenceHelper {

    private static final int DEFAULT_INT = 0;
    private static final boolean DEFAULT_BOOLEAN = false;

    private static final String PREF_KEY_PEN_SIZE = "penSizePref";
    private static final String PREF_KEY_PEN_COLOUR_IDX = "penColourIndex";

    private final Context mContext;

    public PreferenceHelper(Context context) {
        mContext = context.getApplicationContext();
    }


    protected SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getPenSizePref() {
        return getString(PREF_KEY_PEN_SIZE, "medium");
    }

    public int getPenIndex() {
        return getInt(PREF_KEY_PEN_COLOUR_IDX);
    }

    public void setPenIndex(int penIndex) {
        edit(PREF_KEY_PEN_COLOUR_IDX, penIndex);
    }

    private void edit(String name, String value) {
        SharedPreferences.Editor editor = getSharedPrefs().edit();
        editor.putString(name, value);
        editor.apply();
    }

    private void edit(String name, int value) {
        SharedPreferences.Editor editor =  getSharedPrefs().edit();
        editor.putInt(name, value);
        editor.apply();
    }

    private void edit(String name, boolean value) {
        SharedPreferences.Editor editor =  getSharedPrefs().edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private String getString(String name, String def) {
        return getSharedPrefs().getString(name, def);
    }

    private int getInt(String name) {
        return  getSharedPrefs().getInt(name, DEFAULT_INT);
    }

    private boolean getBoolean(String name) {
        return  getSharedPrefs().getBoolean(name, DEFAULT_BOOLEAN);
    }
}

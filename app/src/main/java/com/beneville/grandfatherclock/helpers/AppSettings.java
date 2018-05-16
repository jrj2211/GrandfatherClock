package com.beneville.grandfatherclock.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by joeja on 3/27/2018.
 */

public class AppSettings {

    private static AppSettings mInstance;
    final private String KEY_DOWNLOADED = "SONGS_DOWNLOADED";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mContext;

    private AppSettings(Context context) {
        // Load the preferences file
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        mContext = context;
    }

    public static AppSettings getInstance(Context context) {
        if (mInstance == null || mInstance.mContext != context) {
            mInstance = new AppSettings(context);
        }

        return mInstance;
    }

    public boolean getSongsDownloaded() {
        return preferences.getBoolean(KEY_DOWNLOADED, false);
    }

    public void setSongsDownloaded(boolean downloaded) {
        editor.putBoolean(KEY_DOWNLOADED, downloaded);
        editor.commit();
    }

    public void reset() {
        editor.clear().commit();
    }
}

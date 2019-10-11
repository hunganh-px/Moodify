package com.px.moodify.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by prince on 26/12/2016.
 */

public class PreferenceHelper {

    private SharedPreferences mSharedPreferences;
    private static PreferenceHelper preferenceHelper;
//    private Context mContext;

    public PreferenceHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("com.px.moodify", Context.MODE_PRIVATE);
    }

    public static PreferenceHelper getInstance(Context context) {
        if (preferenceHelper == null) {
            preferenceHelper = new PreferenceHelper(context);
        }
        return preferenceHelper;
    }


    public void setLastSongOpened(int position) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_CURR_SONG, position);
        editor.commit();
    }

    public int getLastSongOpened() {
        return mSharedPreferences.getInt(PREF_CURR_SONG, 0);
    }

    public void setLastSongPausedPosition(int position) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_CURR_POS, position);
        editor.commit();
    }

    public int getLastSongPosition() {
        return mSharedPreferences.getInt(PREF_CURR_POS, 0);
    }


    static final String PREF_CURR_SONG = "com.px.moodify.CURR_SONG";
    static final String PREF_CURR_POS = "com.px.moodify.CURR_POS";
}

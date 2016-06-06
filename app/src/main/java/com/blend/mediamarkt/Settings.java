package com.blend.mediamarkt;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by nimbus on 06/06/16.
 */
public final class Settings {

    private static final String settings_filename = "settings";
    private static SharedPreferences prefs;



    private static SharedPreferences getPrefs(Activity activity) {

        if (prefs == null) {

        }
        return prefs;
    }

}
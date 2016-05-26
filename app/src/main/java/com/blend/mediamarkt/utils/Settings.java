package com.blend.mediamarkt.utils;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by nimbus on 26/05/16.
 */
public final class Settings {


    private static final String settings_filename = "settings";
    private static SharedPreferences prefs;

    private static SharedPreferences getPrefs(Activity activity) {
        // Vullen
        if (prefs == null) {
            
        }
        return prefs;
    }
}

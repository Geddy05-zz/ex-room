package com.blend.mediamarkt.activities;

import android.os.Bundle;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.utils.AudioPlayer;

/**
 * Created by geddy on 06/06/16.
 */

public class ForestActivity extends VuforiaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sceneName = "Forest";

        Sounds sound = Sounds.forest;
        audio = new AudioPlayer(app,sound);
    }

}

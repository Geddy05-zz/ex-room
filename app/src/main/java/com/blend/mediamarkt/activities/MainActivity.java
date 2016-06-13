package com.blend.mediamarkt.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.R;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.utils.AudioPlayer;

public class MainActivity extends VuforiaActivity {

    public static boolean musicEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sceneName = "Western";

        Sounds sound = Sounds.the_good_the_bad_the_ugly;
        audio = new AudioPlayer(app,sound);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_toggle_sound) {

            musicEnabled = !musicEnabled;
            item.setIcon(musicEnabled ? R.drawable.sound_on : R.drawable.sound_off);

            if (musicEnabled) {
                audio.startAudio();
            } else {
                audio.destroyAudio();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}

package com.blend.mediamarkt.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.blend.mediamarkt.ApiHandler;
import com.blend.mediamarkt.App;
import com.blend.mediamarkt.R;
import com.blend.mediamarkt.vuforia.VuforiaController;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.enumerations.audioOptions;

import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.vuforia.ExRoomException;
import com.blend.mediamarkt.vuforia.vuforiaActivity;

public class MainActivity extends vuforiaActivity {

    private static final String LOGTAG = "MainActivity";
    private App app;
    public static boolean musicEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();
        sceneName = "Main";

        // create audio Options for this scene
        Sounds sound = Sounds.the_good_the_bad_the_ugly;
        audio = new AudioPlayer(app,sound);

        startLoadingAnimation( (RelativeLayout) View.inflate(this, R.layout.camera_overlay, null));

        vuforiaController = new VuforiaController(this);
    }

    // only used for testing
    public void setAudioOptions(Context context){
        Sounds sound = Sounds.the_good_the_bad_the_ugly;
        audio = new AudioPlayer(context,sound);
    }

    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        audio.resumeAudio();

        try {
            app.vuforiaSession.resumeAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (vuforiaController.mGlView != null) {
            vuforiaController.mGlView.setVisibility(View.VISIBLE);
            vuforiaController.mGlView.onResume();
        }
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        app.vuforiaSession.onConfigurationChanged();
    }

    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        audio.pauseAudio();
//
        if (vuforiaController.mGlView != null) {
            vuforiaController.mGlView.setVisibility(View.INVISIBLE);
            vuforiaController.mGlView.onPause();
        }

        // Turn off the flash
        try {
            app.vuforiaSession.pauseAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        new ApiHandler(this, audioOptions.Stop).execute();

//        audio.destroyAudio();

        try {
            app.vuforiaSession.stopAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }
        System.gc();
    }
}

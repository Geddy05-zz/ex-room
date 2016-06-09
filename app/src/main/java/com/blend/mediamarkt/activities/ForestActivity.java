package com.blend.mediamarkt.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.R;
import com.blend.mediamarkt.apiHandlers.AudioApiHandler;
import com.blend.mediamarkt.vuforia.ExRoomException;
import com.blend.mediamarkt.vuforia.VuforiaController;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.enumerations.AudioOptions;
import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.vuforia.VuforiaActivity;

/**
 * Created by geddy on 06/06/16.
 */

public class ForestActivity extends VuforiaActivity {

    private VuforiaController mVuforiaController;
    private App app;
    private static final String LOGTAG = "ForestActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();

        // create audio Options for this scene
        Sounds sound = Sounds.forest;
        audio = new AudioPlayer(app,sound);
        startLoadingAnimation((RelativeLayout) View.inflate(this, R.layout.camera_overlay, null));

        mVuforiaController = new VuforiaController(this);
        new AudioApiHandler(this, AudioOptions.Play,Sounds.forest).execute();
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
        if (mVuforiaController.mGlView != null) {
            mVuforiaController.mGlView.setVisibility(View.VISIBLE);
            mVuforiaController.mGlView.onResume();
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
        if (mVuforiaController.mGlView != null) {
            mVuforiaController.mGlView.setVisibility(View.INVISIBLE);
            mVuforiaController.mGlView.onPause();
        }

        // Turn off the flash
        try {
            app.vuforiaSession.pauseAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        audio.destroyAudio();

        try {
            app.vuforiaSession.stopAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }

        System.gc();
    }
}

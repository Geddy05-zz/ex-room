package com.blend.mediamarkt.activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.R;
import com.blend.mediamarkt.apiHandlers.AudioApiHandler;
import com.blend.mediamarkt.enumerations.AudioOptions;
import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.utils.LoadingDialogHandler;
import com.blend.mediamarkt.vuforia.ExRoomException;
import com.blend.mediamarkt.vuforia.VuforiaController;

/**
 * Created by geddy on 06/06/16.
 */

public abstract class VuforiaActivity extends AppCompatActivity {
    protected VuforiaController vuforiaController;
    protected LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    protected AudioPlayer audio;
    protected App app;
    public String sceneName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app =(App) getApplication();

        vuforiaController = new VuforiaController(this);
    }

    @Override
    protected void onResume() {
        Log.d(sceneName, "onResume");
        super.onResume();

        if(audio != null) {
            audio.resumeAudio();
        }

        try {
            App.vuforiaSession.resumeAR();
        } catch (ExRoomException e) {
            Log.e(sceneName, e.getString());
        }

        if (vuforiaController != null && vuforiaController.glView != null) {
            vuforiaController.glView.setVisibility(View.VISIBLE);
            vuforiaController.glView.onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(sceneName, "onPause");
        super.onPause();
        if(audio != null) {
            audio.pauseAudio();
        }

        if(vuforiaController != null) {
            if (vuforiaController.glView != null) {
                vuforiaController.glView.setVisibility(View.INVISIBLE);
                vuforiaController.glView.onPause();
            }
        }

        // Turn off the flash
        try {
            App.vuforiaSession.pauseAR();
        } catch (ExRoomException e) {
            Log.e(sceneName, e.getString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // pause the audio if the audio is played from the phone/tablet
        if(audio != null) {
            audio.pauseAudio();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(sceneName, "onDestroy");
        super.onDestroy();
        new AudioApiHandler(this, AudioOptions.Stop,null).execute();

        try {
            App.vuforiaSession.stopAR();
        } catch (ExRoomException e) {
            Log.e(sceneName, e.getString());
        }
        System.gc();
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(sceneName, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        App.vuforiaSession.onConfigurationChanged();
    }

    public AudioPlayer getAudio() {
        return audio;
    }
}

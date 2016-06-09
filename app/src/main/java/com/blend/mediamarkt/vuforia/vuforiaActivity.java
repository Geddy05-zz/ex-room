package com.blend.mediamarkt.vuforia;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
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

/**
 * Created by geddy on 06/06/16.
 */
public abstract class VuforiaActivity extends AppCompatActivity {
    protected VuforiaController vuforiaController;
    protected LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    protected AudioPlayer audio;
    protected App app;
    protected String sceneName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app =(App) getApplication();

        vuforiaController = new VuforiaController(this);

        startLoadingAnimation( (RelativeLayout) View.inflate(this, R.layout.camera_overlay, null));
    }

    @Override
    protected void onResume() {
        Log.d(sceneName, "onResume");
        super.onResume();

        if(audio != null) {
            audio.resumeAudio();
        }

        try {
            app.vuforiaSession.resumeAR();
        } catch (ExRoomException e) {
            Log.e(sceneName, e.getString());
        }

        // Resume the GL view:
        if (vuforiaController != null && vuforiaController.mGlView != null) {
            vuforiaController.mGlView.setVisibility(View.VISIBLE);
            vuforiaController.mGlView.onResume();
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
            if (vuforiaController.mGlView != null) {
                vuforiaController.mGlView.setVisibility(View.INVISIBLE);
                vuforiaController.mGlView.onPause();
            }
        }

        // Turn off the flash
        try {
            app.vuforiaSession.pauseAR();
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

//        audio.destroyAudio();

        try {
            app.vuforiaSession.stopAR();
        } catch (ExRoomException e) {
            Log.e(sceneName, e.getString());
        }
        System.gc();
    }

    protected void startLoadingAnimation(RelativeLayout uiLayout) {

        uiLayout.setVisibility(View.VISIBLE);
        uiLayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = uiLayout.findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(uiLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public AudioPlayer getAudio() {
        return audio;
    }
}

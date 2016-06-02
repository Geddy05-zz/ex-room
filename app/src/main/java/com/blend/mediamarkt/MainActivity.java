package com.blend.mediamarkt;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.blend.mediamarkt.utils.LoadingDialogHandler;
import com.blend.mediamarkt.utils.Texture;
import com.vuforia.CameraDevice;

import java.util.ArrayList;
import java.util.Vector;
import com.blend.mediamarkt.utils.AudioPlayer;

public class MainActivity extends AppCompatActivity implements RequestURL{

    private static final String LOGTAG = "Media_Markt_Room";
    private App app;

    private Vector<Texture> mTextures;
    private VuforiaController mVuforiaController;

    // Stores the projection matrix to use for rendering purposes
    private RelativeLayout mUILayout;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    public AudioPlayer mAudio;

    boolean mIsDroidDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();

        mAudio = new AudioPlayer(this.getApplicationContext());
        startLoadingAnimation();
        new ApiHandler(playSound).execute();

        mVuforiaController = new VuforiaController(this);

        mTextures = new Vector<Texture>();
        loadTextures();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mAudio.pauseAudio();
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        mAudio.resumeAudio();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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
        mAudio.pauseAudio();
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
        mAudio.destroyAudio();

        try {
            app.vuforiaSession.stopAR();
        } catch (ExRoomException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }

    private void loadTextures() {

        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                getAssets()));
    }

    private void startLoadingAnimation() {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }

}

package com.blend.mediamarkt.vuforia;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blend.mediamarkt.R;
import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.utils.LoadingDialogHandler;

/**
 * Created by geddy on 06/06/16.
 */
public abstract class vuforiaActivity extends AppCompatActivity {
    protected VuforiaController mVuforiaController;
    protected LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    protected AudioPlayer mAudio;
    public String sceneName;

    @Override
    public void onStop() {
        super.onStop();

        mAudio.pauseAudio();
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
        return mAudio;
    }
}

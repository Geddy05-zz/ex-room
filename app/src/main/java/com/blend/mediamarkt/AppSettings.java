package com.blend.mediamarkt;

/**
 * Created by Nimbus on 26/05/16.
 */

public class AppSettings {

    private boolean soundIsOn = true;
    private boolean showCameraInBackground = false;

    public void setSoundIsOn(){
        soundIsOn = (soundIsOn) ? false : true;
    }

    public void setShowCameraInBackground(){
        showCameraInBackground = (showCameraInBackground)? false : true;
    }

    public boolean isSoundIsOn(){
        return soundIsOn;
    }

    public boolean isShowCameraInBackground (){
        return showCameraInBackground;
    }

}

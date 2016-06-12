package com.blend.mediamarkt.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.blend.mediamarkt.R;
import com.blend.mediamarkt.activities.MainActivity;
import com.blend.mediamarkt.enumerations.Sounds;

/**
 * Created by geddy on 19/05/16.
 */
public class AudioPlayer {

    private MediaPlayer mMediaPlayer;
    private static Context context;
    private boolean mIsPause =false;
    private int mPausePosition = 0;
    private final Sounds sound;

    public AudioPlayer(Context context,Sounds sound){
        AudioPlayer.context = context;
        this.sound = sound;
    }

    public void startAudio() {
        if(MainActivity.musicEnabled) {
            getMediaPlayer().start();
        }
    }

    public void pauseAudio() {
        if(getMediaPlayer() != null && getMediaPlayer().isPlaying()) {
            mIsPause = true;
            mPausePosition = getMediaPlayer().getCurrentPosition();
            getMediaPlayer().pause();
        }
    }

    public void resumeAudio(){
        if(getMediaPlayer() != null && mIsPause) {
            getMediaPlayer().seekTo(mPausePosition);
            getMediaPlayer().start();
            mIsPause = false;
        }
    }

    public void destroyAudio(){
        getMediaPlayer().stop();
        mMediaPlayer = null;
    }

    public void newSong(int song){
        getMediaPlayer().stop();
        mMediaPlayer = MediaPlayer.create(context,song);
    }

    public MediaPlayer getMediaPlayer() {
        if(mMediaPlayer == null && context != null) {
            mMediaPlayer = MediaPlayer.create(context, sound.getSound());
            mMediaPlayer.setLooping(true);
        }
        return mMediaPlayer;
    }
}

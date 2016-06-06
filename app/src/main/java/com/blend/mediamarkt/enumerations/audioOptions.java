package com.blend.mediamarkt.enumerations;

/**
 * Created by geddy on 06/06/16.
 */
public enum audioOptions {
    Play("play_song_id"),
    Stop("stop"); // this url is not yet used

    private final String url;

    audioOptions(final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }
}

package com.blend.mediamarkt.enumerations;

/**
 * Created by geddy on 06/06/16.
 */
public enum AudioOptions {
//    Play("sounds/play_song_id"),
    Play("/sounds/"),
    Stop("/sounds/stop");

    private final String url;

    AudioOptions(final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }
}

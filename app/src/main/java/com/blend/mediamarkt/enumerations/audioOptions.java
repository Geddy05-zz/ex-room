package com.blend.mediamarkt.enumerations;

/**
 * Created by geddy on 06/06/16.
 */
public enum audioOptions {
//    Play("sounds/play_song_id"),
    Play("/sounds/2"),
    Stop("/sounds/stop");

    private final String url;

    audioOptions(final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }
}

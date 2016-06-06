package com.blend.mediamarkt.enumerations;

/**
 * Created by geddy on 06/06/16.
 */
public enum audioOptions {
    Play("play"),
    Stop("stop");

    private final String text;

    audioOptions(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

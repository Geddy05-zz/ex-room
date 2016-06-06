package com.blend.mediamarkt.enumerations;

import com.blend.mediamarkt.R;

/**
 * Created by geddy on 06/06/16.
 */
public enum Sounds {
    the_good_the_bad_the_ugly(1),
    forest(2);

    private final int sound;

    Sounds(final int sound) {
        this.sound = sound;
    }

    public int getId() {
        return sound;
    }

    public int getSound(){
        switch(sound) {
            case 1:
                return R.raw.the_good_the_bad_and_the_ugly;
            default:
        }        return R.raw.forest;
    }
}

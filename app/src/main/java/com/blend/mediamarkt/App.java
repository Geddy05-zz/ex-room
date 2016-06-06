package com.blend.mediamarkt;

import android.app.Application;

import com.blend.mediamarkt.vuforia.ExRoomSession;

/**
 * Created by geddy on 30/05/16.
 */
public class App extends Application {

    public static ExRoomSession vuforiaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        vuforiaSession = new ExRoomSession();
    }
}

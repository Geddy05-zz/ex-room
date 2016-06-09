package com.blend.mediamarkt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.util.Log;

import com.blend.mediamarkt.activities.MainActivity;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.enumerations.audioOptions;
import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.vuforia.ExRoomSession;
import com.blend.mediamarkt.vuforia.VuforiaController;
import com.blend.mediamarkt.vuforia.vuforiaActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({android.util.Log.class})
public class ExampleUnitTest extends AndroidTestCase {

    MainActivity activity;
    VuforiaController vuforiaController;
    ApiHandler apiHandlerPlay;
    ApiHandler apiHandlerStop;
    boolean serverIsOffline = true;

    @Before
    public void setUp() {

        activity = new MainActivity();

        vuforiaController = new VuforiaController(activity);

        apiHandlerPlay = new ApiHandler(activity, audioOptions.Play);
        apiHandlerStop = new ApiHandler(activity, audioOptions.Stop);
    }

    @Test
    public void handleResponse_areCorrect() {
        ApiHandler apiHandler = new ApiHandler(activity, audioOptions.Play);
        boolean result199 = apiHandler.handleResponse(199);
        boolean result200 = apiHandler.handleResponse(200);
        boolean result201 = apiHandler.handleResponse(201);

        boolean result299 = apiHandler.handleResponse(299);
        boolean result300 = apiHandler.handleResponse(300);
        boolean result301 = apiHandler.handleResponse(301);

        assertEquals(result199, false);
        assertEquals(result200, true);
        assertEquals(result201, true);

        assertEquals(result299, true);
        assertEquals(result300, false);
        assertEquals(result301, false);
    }

    @Test
    public void SoundsEnum_isCorrect(){
        assertEquals(Sounds.the_good_the_bad_the_ugly.getId(),1);
        assertEquals(Sounds.forest.getId(),2);

        assertEquals(Sounds.the_good_the_bad_the_ugly.getSound(),R.raw.the_good_the_bad_and_the_ugly);
        assertEquals(Sounds.forest.getSound(),R.raw.forest);
    }

    @Test
    public void urls_areCorrect(){
        apiHandlerPlay = new ApiHandler(activity, audioOptions.Play);
        apiHandlerStop = new ApiHandler(activity, audioOptions.Stop);

        boolean responsePlay = apiHandlerPlay.doInBackground();
        boolean responseStop = apiHandlerStop.doInBackground();
        if(serverIsOffline){
            assertEquals(responsePlay, false);
            assertEquals(responseStop, false);
        }else {
            assertEquals(responsePlay, true);
            assertEquals(responseStop, true);
        }
    }
}
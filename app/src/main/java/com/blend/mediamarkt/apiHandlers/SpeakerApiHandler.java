package com.blend.mediamarkt.apiHandlers;

import android.os.AsyncTask;

import com.blend.mediamarkt.enumerations.AudioOptions;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.activities.VuforiaActivity;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by geddy on 09/06/16.
 */

public class SpeakerApiHandler extends AsyncTask<Void, Void, Boolean> {

    private static  String TAG = "APIHandler";
    public static String baseUrl = "http://10.0.1.3:5000";
    private VuforiaActivity activity;
    private AudioOptions audioOptions;
    private Sounds sound;

    public SpeakerApiHandler(VuforiaActivity activity, AudioOptions option, Sounds sound){
        this.activity = activity;
        this.audioOptions = option;
        this.sound = sound;
    }

    public URL createURL() throws MalformedURLException {
        if(sound != null) {
            return new URL(baseUrl + audioOptions.toString() + sound.getId());
        }else{
            return new URL(baseUrl + audioOptions.toString());
        }
    }

    @Override
    public Boolean doInBackground(Void... voids) {
        boolean succes = false;

        try {
            // Defined URL  where to send data
            URL url = createURL();

            HttpURLConnection connection;
            connection =(HttpURLConnection) url.openConnection();

            // set output to true for creating a body in the request
            connection.setDoOutput( false );
            connection.setRequestMethod("GET");

            /* We set the timeout to 5 sec. because of the user experience
               If this is to height the user missed important sounds */
            connection.setConnectTimeout(5000);

            // Handle response
            succes = handleResponse(connection.getResponseCode());

        } catch (java.net.SocketTimeoutException e) {
            return false;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return succes;
    }

    public boolean handleResponse(int responseCode){
        return responseCode > 199 && responseCode < 300;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        // play audio on device when server isn't available
        if(!result){
            if(activity.getAudio() != null) {
                if (audioOptions == AudioOptions.Play) {
                    activity.getAudio().startAudio();
                }else {
                    activity.getAudio().destroyAudio();
                }
            }
        }
    }
}

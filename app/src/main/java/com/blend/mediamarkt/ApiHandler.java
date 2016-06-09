package com.blend.mediamarkt;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.blend.mediamarkt.activities.MainActivity;
import com.blend.mediamarkt.enumerations.audioOptions;
import com.blend.mediamarkt.utils.AudioPlayer;
import com.blend.mediamarkt.vuforia.vuforiaActivity;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by geddy on 02/06/16.
 */
public class ApiHandler extends AsyncTask<Void, Void, Boolean>{

    private static  String TAG = "APIHandler";
    private static String baseUrl = "http://10.0.1.3:5000";
    private vuforiaActivity activity;
    private com.blend.mediamarkt.enumerations.audioOptions audioOptions;

    public ApiHandler(vuforiaActivity activity, audioOptions option){
        this.activity = activity;
        this.audioOptions = option;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean succes = false;

        try {
            // Defined URL  where to send data
            URL url = new URL(baseUrl+ audioOptions.toString());

            // Send POST request
            HttpURLConnection connection;
            connection =(HttpURLConnection) url.openConnection();

            // set output to true for creating a body in the request
            connection.setDoOutput( false );
            connection.setRequestMethod("GET");
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
        if(responseCode > 199 &&  responseCode < 300) {
            return true;
        }
        return  false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(!result){
            if(activity.getAudio() != null) {
                if (audioOptions == audioOptions.Play) {
                    activity.getAudio().startAudio();
                }else {
                    activity.getAudio().destroyAudio();
                }
            }
        }
    }
}

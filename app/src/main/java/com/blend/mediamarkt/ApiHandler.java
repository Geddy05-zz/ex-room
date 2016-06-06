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
    private AudioPlayer audio;


    public ApiHandler(vuforiaActivity activity, audioOptions option){
        super();
        this.activity = activity;
        this.audioOptions = option;
        this.audio = null;
    }

    public ApiHandler(vuforiaActivity activity, audioOptions option, AudioPlayer audio ){
        this(activity,option);
        this.audio = audio;
    }

    public static int calculate(int a , int b){
        return a+b;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean succes = false;

        try {
            // Defined URL  where to send data
//            URL url = new URL(baseUrl+ "/sounds/" + audioOptions.toString());
            URL url = new URL(baseUrl+ "/sounds/abc");


            // Send POST request
            HttpURLConnection connection;
            connection =(HttpURLConnection) url.openConnection();

            // set output to true for creating a body in the request
//            connection.setDoOutput( true );
//            connection.setRequestMethod("POST");

            connection.setDoOutput( false );
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            Log.i(TAG,"HTTP response is " + responseCode);
            if(responseCode > 199 &&  responseCode < 300) {
                succes = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return succes;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(!result){
            if(activity.getAudio() != null) {
                activity.getAudio().startAudio();
            }
        }
    }
}

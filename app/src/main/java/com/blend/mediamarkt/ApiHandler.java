package com.blend.mediamarkt;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by geddy on 02/06/16.
 */
public class ApiHandler extends AsyncTask<Void, Void, Boolean>{

    private static  String TAG = "APIHandler";
    private static String baseUrl = "http://192.168.0.102:5000/";
    private String params;
    private Void returnFunction;
    private MainActivity activity;

    public ApiHandler(String params, MainActivity activity){
        super();
        this.params = params;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean succes = false;

        try {

            // Defined URL  where to send data
            URL url = new URL(baseUrl);

            // Send POST request
            URLConnection conn;
            conn = url.openConnection();

            int responseCode = ((HttpURLConnection)conn).getResponseCode();
            Log.i(TAG,"HTTP response is " + responseCode);
            succes = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return succes;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(!result){
            if(activity.mAudio != null) {
                activity.mAudio.startAudio();
            }
        }
    }
}

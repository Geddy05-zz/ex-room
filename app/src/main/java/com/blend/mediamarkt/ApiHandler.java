package com.blend.mediamarkt;

import android.os.AsyncTask;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by geddy on 02/06/16.
 */
public class ApiHandler extends AsyncTask<Void, Void, Void>{

    private static  String TAG = "APIHandler";
    private static String baseUrl = "http://www.oogogougy.com/";
    private String params;

    public ApiHandler(String params){
        super();
        this.params = params;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {

            // Defined URL  where to send data
            URL url = new URL(baseUrl+params);

            // Send POST request
            URLConnection conn;
            conn = url.openConnection();

            int responseCode = ((HttpURLConnection)conn).getResponseCode();
            Log.i(TAG,"HTTP response is " + responseCode);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.blend.mediamarkt;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "Media_Markt_Room";

    private DataSet mCurrentDataset;
    private Activity mActivity;
    private Object mShutdownLock = new Object();
    private InitVuforiaTask mInitVuforiaTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        try
        {
            mInitVuforiaTask = new InitVuforiaTask();
            mInitVuforiaTask.execute();
        } catch (Exception e)
        {
            String logMessage = "Initializing Vuforia SDK failed";
            Log.e(LOGTAG, logMessage);
        }

        initTracker();

        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        //Todo: Implement own opengl implementation
//        mGlView = new SampleGLView(this);
//        mGlView.init(translucent, depthSize, stencilSize);
//
//        mRenderer = new ImageTargetRenderer();
//        mGlView.setRenderer(mRenderer);

//        addContentView(mGlView, new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
//                LinearLayoutCompat.LayoutParams.MATCH_PARENT));

//        CameraDevice.getInstance().init(camera);

//        configureVideoBackground();

        CameraDevice.getInstance().selectVideoMode(
                CameraDevice.MODE.MODE_DEFAULT)

        CameraDevice.getInstance().start();

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);

// Start the tracker
        TrackerManager tman = TrackerManager.getInstance();
        Tracker tracker = tman.getTracker(ObjectTracker.getClassType());
        tracker.start();

    }

    private void initTracker(){
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker = tManager.initTracker(ObjectTracker.getClassType());

        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        mCurrentDataset = objectTracker.createDataSet();

    }

    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {
        // Initialize with invalid value:
        private int mProgressValue = -1;


        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock)
            {
                Vuforia.setInitParameters(mActivity, Vuforia.GL_20, "AerVtbn/////AAAAAZoEtTkaNUFVt33brNSVmuYLvikZecNvutuFTLvzPex9wdCATTeTtNn8XugB5UnFe/MHzBLjNMHZ4Bp2B6zI9AjTZkZYJtRUchvU25Xddk48nPvIm5Yk33wDdHl38IDGvh/J+SDy9GW64sWSbh8YYV3nCmC3KW0JHXRsBXy8OcSignR3Ede3xSQACCvLQynoY4NkHvm2VC0uDU83XBu3BFeyPk0Xp/c05CCkTgOtGycShbLdDavkCC3Vwmrm2u2NoaUkgfFCXDV2Dkbk5Uqr8dcJccGWJk3fZVs2orrZ5p4YenorSiDKXZMc4NFSxq4WmR/Vwx8XlqgCc3vzvLdvaZZ/+DgUhFNhQylAHuFyJtpO");

                do
                {
                    mProgressValue = Vuforia.init();

                    // Publish the progress value:
                    publishProgress(mProgressValue);

                } while (!isCancelled() && mProgressValue >= 0
                        && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }


        protected void onProgressUpdate(Integer... values)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }


        protected void onPostExecute(Boolean result)
        {
            if (result)
            {

            } else
            {

            }
        }
    }

}

package com.blend.mediamarkt;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.blend.mediamarkt.utils.LoadingDialogHandler;
import com.blend.mediamarkt.utils.Texture;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.Matrix44F;
import com.vuforia.ObjectTracker;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Renderer;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec2I;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.Vector;

public class MainActivity extends AppCompatActivity  implements SampleApplicationControl{

    private static final String LOGTAG = "Media_Markt_Room";

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;

    private boolean mSwitchDatasetAsap = false;
    private SampleApplicationSession vuforiaAppSession;
    private int mVuforiaFlags = 0;


    private Vector<Texture> mTextures;

    // Stores the projection matrix to use for rendering purposes
    private Matrix44F mProjectionMatrix;

    private RelativeLayout mUILayout;
    private boolean mCameraRunning = false;
    private Activity mActivity;
    private Object mShutdownLock = new Object();
    private InitVuforiaTask mInitVuforiaTask;
    private ExRoomGL mGlView;
    private int mCamera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    private boolean mExtendedTracking = false;
    private ImageTargetRenderer mRenderer;
    private GestureDetector mGestureDetector;

    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    // Stores viewport to be used for rendering purposes
    private int[] mViewport;
    // Stores orientation
    private boolean mIsPortrait = false;

    boolean mIsDroidDevice = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();
        mDatasetStrings.add("StonesAndChips.xml");

        vuforiaAppSession.initAR(this,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        mTextures = new Vector<Texture>();
        loadTextures();

        try {
            vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
            doStartTrackers();
        }catch (Exception e){
            EventLog.writeEvent(500,"dotracker failed");
        }
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }

    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }



    public void initAR(Activity activity, int screenOrientation)
    {
        SampleApplicationException vuforiaException = null;
        mActivity = activity;

        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // SampleApplicationSession needs to update the Projection Matrix.
        OrientationEventListener orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int i) {
                int activityRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                if(mLastRotation != activityRotation)
                {
                    // Signal the ApplicationSession to refresh the projection matrix
                    setProjectionMatrix();
                    mLastRotation = activityRotation;
                }
            }

            int mLastRotation = -1;
        };

        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();

        // Apply screen orientation
        mActivity.setRequestedOrientation(screenOrientation);

        updateActivityOrientation();

        // Query display dimensions:
        storeScreenDimensions();

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        mActivity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVuforiaFlags = Vuforia.GL_20;

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null)
        {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new SampleApplicationException(
                    SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,
                    logMessage);
//            Log.e(LOGTAG, logMessage);
        }

        if (vuforiaException == null)
        {
            try
            {
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e)
            {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
//                Log.e(LOGTAG, logMessage);
            }
        }

        if (vuforiaException != null)
            this.onInitARDone(vuforiaException);
    }

    public void startAR(int camera) throws SampleApplicationException
    {
        String error;
//        if(true)
//        {
//            error = "Camera already running, unable to open again";
////        	Log.e(LOGTAG, error);
//            throw new SampleApplicationException(
//                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
//        }

        mCamera = camera;
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera;
//            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        if (!CameraDevice.getInstance().selectVideoMode(
                CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        // Configure the rendering of the video background
        configureVideoBackground();

        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
//            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        setProjectionMatrix();

        this.doStartTrackers();

        mCameraRunning = true;

        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }

    public void setProjectionMatrix()
    {
        CameraCalibration camCal = CameraDevice.getInstance()
                .getCameraCalibration();
        mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
    }

//    private void initTracker(){
//        TrackerManager tManager = TrackerManager.getInstance();
//        Tracker tracker = tManager.initTracker(ObjectTracker.getClassType());
//
//        ObjectTracker objectTracker = (ObjectTracker) tManager
//                .getTracker(ObjectTracker.getClassType());
//        mCurrentDataset = objectTracker.createDataSet();
//
//    }

    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg",
                getAssets()));
    }

    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }

    private void updateActivityOrientation()
    {
        Configuration config = mActivity.getResources().getConfiguration();

        switch (config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }

//        Log.i(LOGTAG, "Activity is in "
//            + (mIsPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }

    // Gets the viewport to be used fo rendering
    public int[] getViewport()
    {
        return mViewport;
    }

    private void storeScreenDimensions()
    {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }


    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }

    // Configures the video mode and sets offsets for the camera's image
    private void configureVideoBackground()
    {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize = 0, ySize = 0;
        if (mIsPortrait)
        {
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
                    .getWidth()));
            ySize = mScreenHeight;

            if (xSize < mScreenWidth)
            {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
        } else
        {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
                    .getWidth()));

            if (ySize < mScreenHeight)
            {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
                        .getHeight()));
                ySize = mScreenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));

        // The Vuforia VideoBackgroundConfig takes the position relative to the
        // centre of the screen, where as the OpenGL glViewport call takes the
        // position relative to the lower left corner
        mViewport = new int[4];
        mViewport[0] = ((mScreenWidth - xSize) / 2) + config.getPosition().getData()[0];
        mViewport[1] = ((mScreenHeight - ySize) / 2) + config.getPosition().getData()[1];
        mViewport[2] = xSize;
        mViewport[3] = ySize;

//        Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
//            + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
//            + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");

        Renderer.getInstance().setVideoBackgroundConfig(config);

    }



    boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new ExRoomGL(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession,mTextures);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
//        // Process the Gestures
//        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
//            return true;

        return mGestureDetector.onTouchEvent(event);
    }


    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);


            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

//            if (result)
//                mContAutofocus = true;
//            else
//                Log.e(LOGTAG, "Unable to enable continuous autofocus");

//            mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
//                    mGlView, mUILayout, null);
//            setSampleAppMenuSettings();

        } else
        {
            Log.e(LOGTAG, exception.getString());
//            showInitializationErrorMessage(exception.getString());
        }
    }

    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }

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
//                initTracker();
//                initTracker();
//
//
//                Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
//
//                // Start the tracker
//                TrackerManager tman = TrackerManager.getInstance();
//                Tracker tracker = tman.getTracker(ObjectTracker.getClassType());
//                tracker.start();
            } else
            {

            }
        }
    }

}

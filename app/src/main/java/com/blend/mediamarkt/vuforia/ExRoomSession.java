package com.blend.mediamarkt.vuforia;

/**
 * Created by geddy on 12/05/16.
 */

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.blend.mediamarkt.R;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Vec2I;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;
import com.vuforia.Vuforia.UpdateCallbackInterface;

public class ExRoomSession implements UpdateCallbackInterface
{
    // Reference to the current activity
    private Activity activity;
    private ExRoomControl exRoomControl;

    // Flags
    private boolean started = false;
    private boolean cameraRunning = false;

    // Display size of the device:
    private int screenWidth = 0;
    private int screenHeight = 0;

    // The async tasks to initialize the Vuforia SDK:
    private InitVuforiaTask initVuforiaTask;
    private LoadTrackerTask loadTrackerTask;

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    private Object shutdownLock = new Object();

    // Vuforia initialization flags:
    private int vuforiaFlags = 0;

    // Holds the camera configuration to use upon resuming
    private int camera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT;

    // Stores the projection matrix to use for rendering purposes
    private Matrix44F projectionMatrix;

    // Stores viewport to be used for rendering purposes
    private int[] viewport;

    // Stores orientation
    private boolean isPortrait = false;

    public void setExRoomControl(ExRoomControl sessionControl){
        exRoomControl = sessionControl;
    }


    // Initializes Vuforia and sets up preferences.
    public void initAR(Activity activity, int screenOrientation)
    {
        ExRoomException vuforiaException = null;
        this.activity = activity;

        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // ExRoomSession needs to update the Projection Matrix.
        OrientationEventListener orientationEventListener = new OrientationEventListener(ExRoomSession.this.activity) {
            @Override
            public void onOrientationChanged(int i) {
                int activityRotation = ExRoomSession.this.activity.getWindowManager().getDefaultDisplay().getRotation();
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
        this.activity.setRequestedOrientation(screenOrientation);

        updateActivityOrientation();

        // Query display dimensions:
        storeScreenDimensions();

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        this.activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        vuforiaFlags = Vuforia.GL_20;

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (initVuforiaTask != null)
        {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new ExRoomException(
                    ExRoomException.VUFORIA_ALREADY_INITIALIZATED,
                    logMessage);
        }

        if (vuforiaException == null)
        {
            try
            {
                initVuforiaTask = new InitVuforiaTask();
                initVuforiaTask.execute();
            } catch (Exception e)
            {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new ExRoomException(
                        ExRoomException.INITIALIZATION_FAILURE,
                        logMessage);
            }
        }

        if (vuforiaException != null)
            exRoomControl.onInitARDone(vuforiaException);
    }


    // Starts Vuforia, initialize and starts the camera and start the trackers
    public void startAR(int camera) throws ExRoomException
    {
        String error;
        if(cameraRunning)
        {
            error = "Camera already running, unable to open again";
//        	Log.e(LOGTAG, error);
            throw new ExRoomException(
                    ExRoomException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        this.camera = camera;
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera;
//            Log.e(LOGTAG, error);
            throw new ExRoomException(
                    ExRoomException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        if (!CameraDevice.getInstance().selectVideoMode(
                CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode";
//            Log.e(LOGTAG, error);
            throw new ExRoomException(
                    ExRoomException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        // Configure the rendering of the video background
        configureVideoBackground();

        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
            throw new ExRoomException(
                    ExRoomException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        setProjectionMatrix();

        exRoomControl.doStartTrackers();

        cameraRunning = true;

        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }


    // Stops any ongoing initialization, stops Vuforia
    public void stopAR() throws ExRoomException
    {
        // Cancel potentially running tasks
        if (initVuforiaTask != null
                && initVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            initVuforiaTask.cancel(true);
            initVuforiaTask = null;
        }

        if (loadTrackerTask != null
                && loadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            loadTrackerTask.cancel(true);
            loadTrackerTask = null;
        }

        initVuforiaTask = null;
        loadTrackerTask = null;

        started = false;

        stopCamera();

        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized (shutdownLock)
        {

            boolean unloadTrackersResult;
            boolean deinitTrackersResult;

            // Destroy the tracking data set:
            unloadTrackersResult = exRoomControl.doUnloadTrackersData();

            // Deinitialize the trackers:
            deinitTrackersResult = exRoomControl.doDeinitTrackers();

            // Deinitialize Vuforia SDK:
            Vuforia.deinit();

            if (!unloadTrackersResult)
                throw new ExRoomException(
                        ExRoomException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data");

            if (!deinitTrackersResult)
                throw new ExRoomException(
                        ExRoomException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers");

        }
    }


    // Resumes Vuforia, restarts the trackers and the camera
    public void resumeAR() throws ExRoomException
    {
        // Vuforia-specific resume operation
        Vuforia.onResume();

        if (started)
        {
            startAR(camera);
        }
    }


    // Pauses Vuforia and stops the camera
    public void pauseAR() throws ExRoomException
    {
        if (started)
        {
            stopCamera();
        }

        Vuforia.onPause();
    }

    // Callback called every cycle
    @Override
    public void Vuforia_onUpdate(State s)
    {
        exRoomControl.onVuforiaUpdate(s);
    }


    // Manages the configuration changes
    public void onConfigurationChanged()
    {
        updateActivityOrientation();

        storeScreenDimensions();

        if (isARRunning())
        {
            // configure video background
            configureVideoBackground();

            // Update projection matrix:
            setProjectionMatrix();
        }

    }

    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }


    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }

    // An async task to initialize Vuforia asynchronously.
    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {
        // Initialize with invalid value:
        private int mProgressValue = -1;


        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (shutdownLock)
            {
                Vuforia.setInitParameters(activity, vuforiaFlags, "AerVtbn/////AAAAAZoEtTkaNUFVt33brNSVmuYLvikZecNvutuFTLvzPex9wdCATTeTtNn8XugB5UnFe/MHzBLjNMHZ4Bp2B6zI9AjTZkZYJtRUchvU25Xddk48nPvIm5Yk33wDdHl38IDGvh/J+SDy9GW64sWSbh8YYV3nCmC3KW0JHXRsBXy8OcSignR3Ede3xSQACCvLQynoY4NkHvm2VC0uDU83XBu3BFeyPk0Xp/c05CCkTgOtGycShbLdDavkCC3Vwmrm2u2NoaUkgfFCXDV2Dkbk5Uqr8dcJccGWJk3fZVs2orrZ5p4YenorSiDKXZMc4NFSxq4WmR/Vwx8XlqgCc3vzvLdvaZZ/+DgUhFNhQylAHuFyJtpO");

                do
                {
                    // Vuforia.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If Vuforia.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = Vuforia.init();

                    // Publish the progress value:
                    publishProgress(mProgressValue);

                    // We check whether the task has been canceled in the
                    // meantime (by calling AsyncTask.cancel(true)).
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that
                    // started is.
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
            // Done initializing Vuforia, proceed to next application
            // initialization status:

            ExRoomException vuforiaException = null;

            if (result)
            {
                boolean initTrackersResult;
                initTrackersResult = exRoomControl.doInitTrackers();

                if (initTrackersResult)
                {
                    try
                    {
                        loadTrackerTask = new LoadTrackerTask();
                        loadTrackerTask.execute();
                    } catch (Exception e)
                    {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new ExRoomException(
                                ExRoomException.LOADING_TRACKERS_FAILURE,
                                logMessage);
                        exRoomControl.onInitARDone(vuforiaException);
                    }

                } else
                {
                    vuforiaException = new ExRoomException(
                            ExRoomException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers");
                    exRoomControl.onInitARDone(vuforiaException);
                }
            } else
            {
                String logMessage;

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                logMessage = getInitializationErrorString(mProgressValue);

                // Send Vuforia Exception to the application and call initDone
                // to stop initialization process
                vuforiaException = new ExRoomException(
                        ExRoomException.INITIALIZATION_FAILURE,
                        logMessage);
                exRoomControl.onInitARDone(vuforiaException);
            }
        }
    }

    // An async task to load the tracker data asynchronously.
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap:
            synchronized (shutdownLock)
            {
                // Load the tracker data set:
                return exRoomControl.doLoadTrackersData();
            }
        }


        protected void onPostExecute(Boolean result)
        {

            ExRoomException vuforiaException = null;

            if (!result)
            {
                String logMessage = "Failed to load tracker data.";
                vuforiaException = new ExRoomException(
                        ExRoomException.LOADING_TRACKERS_FAILURE,
                        logMessage);
            } else
            {
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc();

                Vuforia.registerCallback(ExRoomSession.this);

                started = true;
            }

            // Done loading the tracker, update application status, send the
            // exception to check errors
            exRoomControl.onInitARDone(vuforiaException);
        }
    }


    // Returns the error message for each error code
    private String getInitializationErrorString(int code)
    {
        if (code == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
            return activity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
        if (code == Vuforia.INIT_NO_CAMERA_ACCESS)
            return activity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
        if (code == Vuforia.INIT_LICENSE_ERROR_MISSING_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_INVALID_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return activity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return activity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_CANCELED_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            return activity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
        else
        {
            return activity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
        }
    }


    // Stores screen dimensions
    private void storeScreenDimensions()
    {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }


    // Stores the orientation depending on the current resources configuration
    private void updateActivityOrientation()
    {
        Configuration config = activity.getResources().getConfiguration();

        switch (config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                isPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                isPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
    }


    // Method for setting / updating the projection matrix for AR content
    // rendering
    public void setProjectionMatrix()
    {
        CameraCalibration camCal = CameraDevice.getInstance()
                .getCameraCalibration();
        projectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
    }


    public void stopCamera()
    {
        if(cameraRunning)
        {
            exRoomControl.doStopTrackers();
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
            cameraRunning = false;
        }
    }


    // Configures the video mode and sets offsets for the camera's image
    public void configureVideoBackground()
    {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize = 0, ySize = 0;
        if (isPortrait)
        {
            xSize = (int) (vm.getHeight() * (screenHeight / (float) vm
                    .getWidth()));
            ySize = screenHeight;

            if (xSize < screenWidth)
            {
                xSize = screenWidth;
                ySize = (int) (screenWidth * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
        } else
        {
            xSize = screenWidth;
            ySize = (int) (vm.getHeight() * (screenWidth / (float) vm
                    .getWidth()));

            if (ySize < screenHeight)
            {
                xSize = (int) (screenHeight * (vm.getWidth() / (float) vm
                        .getHeight()));
                ySize = screenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));

        // The Vuforia VideoBackgroundConfig takes the position relative to the
        // centre of the screen, where as the OpenGL glViewport call takes the
        // position relative to the lower left corner
        viewport = new int[4];
        viewport[0] = ((screenWidth - xSize) / 2) + config.getPosition().getData()[0];
        viewport[1] = ((screenHeight - ySize) / 2) + config.getPosition().getData()[1];
        viewport[2] = xSize;
        viewport[3] = ySize;

        Renderer.getInstance().setVideoBackgroundConfig(config);

    }

    // Returns true if Vuforia is initialized, the trackers started and the
    // tracker data loaded
    private boolean isARRunning()
    {
        return started;
    }

}


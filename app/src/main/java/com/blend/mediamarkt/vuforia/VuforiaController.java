package com.blend.mediamarkt.vuforia;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.R;
import com.blend.mediamarkt.activities.VuforiaActivity;
import com.blend.mediamarkt.utils.BaseScene;
import com.blend.mediamarkt.utils.WesternScene;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;

/**
 * Created by geddy on 30-5-2016.
 */
public class VuforiaController implements ExRoomControl {

    private static final String LOGTAG = "Media_Markt_Room";
    private DataSet currentDataset;
    private int currentDatasetSelectionIndex = 0;
    private ArrayList<String> datasetStrings = new ArrayList<String>();
    private boolean extendedTracking = false;
    private BaseScene scene;
    private RelativeLayout uiLayout;
    private ObjectTracker objectTracker;
    private VuforiaActivity activity;
    public ExRoomGL glView;


    public VuforiaController(VuforiaActivity activity){

        this.activity = activity;

        datasetStrings.add("StonesAndChips.xml");

        new Thread(new Runnable() {
            public void run() {
                start();
            }
        }).start();

    }


    private void start() {
        uiLayout = (RelativeLayout) View.inflate(activity, R.layout.camera_overlay,
                null);

        try {
            App.vuforiaSession.setmSessionControl(this);
            App.vuforiaSession.initAR(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            App.vuforiaSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
            doStartTrackers();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            objectTracker = (ObjectTracker) tManager
                    .getTracker(ObjectTracker.getClassType());
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        if (objectTracker == null)
            return false;

        if (currentDataset == null)
            currentDataset = objectTracker.createDataSet();

        if (currentDataset == null)
            return false;

        if (!currentDataset.load(
                datasetStrings.get(currentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(currentDataset))
            return false;

        int numTrackables = currentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = currentDataset.getTrackable(count);
            if (isExtendedTrackingActive()) {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + trackable.getUserData());
        }

        return true;
    }

    boolean isExtendedTrackingActive() {
        return extendedTracking;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = false;

        if (objectTracker != null) {
            result = true;
            objectTracker.start();
        }

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        if (objectTracker == null)
            return false;

        if (currentDataset != null && currentDataset.isActive()) {
            if (objectTracker.getActiveDataSet().equals(currentDataset)
                    && !objectTracker.deactivateDataSet(currentDataset)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(currentDataset)) {
                result = false;
            }

            currentDataset = null;
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

    @Override
    public void onInitARDone(ExRoomException exception) {
        if (exception == null) {
            initApplicationAR();

            scene.mIsActive = true;
            activity.addContentView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            uiLayout.bringToFront();
            uiLayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                App.vuforiaSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (ExRoomException e) {
                Log.e(LOGTAG, e.getString());
            }

            CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

        } else {
            Log.e(LOGTAG, exception.getString());
        }
    }

    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        glView = new ExRoomGL(activity);
        glView.init(translucent, depthSize, stencilSize);

        scene = initScene();

        glView.setRenderer(scene);

    }

    private BaseScene initScene(){
        switch (activity.sceneName){
            case "Western":
                return new WesternScene(activity);
            default:
                return new WesternScene(activity);

        }
    }

    @Override
    public void onVuforiaUpdate(State state) {}
}

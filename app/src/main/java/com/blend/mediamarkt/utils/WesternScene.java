package com.blend.mediamarkt.utils;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.apiHandlers.AudioApiHandler;
import com.blend.mediamarkt.enumerations.AudioOptions;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.vuforia.ExRoomSession;
import com.blend.mediamarkt.vuforia.VuforiaActivity;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.vuforia.CameraCalibration;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;
//import com.blend.mediamarkt.utils.Texture;


import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by geddy on 21/05/16.
 */

public class WesternScene extends BaseScene {


    private static final String LOGTAG = "ImageTargetRenderer";
    private ExRoomSession vuforiaAppSession;
    private VuforiaActivity mActivity;
    private Vector<Texture> mTextures;

    private int texSampler2DHandle;
    private int shaderProgramID;

    private Renderer mRenderer;
//    public boolean mIsActive = false;

    private World world;
    private Light sun;
//    private Object3D cylinder;
    private Object3D home1;
    private Object3D home2;
    private Object3D road;
    private Camera cam;
    private FrameBuffer fb;
    private float[] modelViewMat;
    private float fov;
    private float fovy;
    private boolean findTrackable = false;


    private String sceneName;
    public enum objectOBJ{

    }

    public WesternScene(VuforiaActivity activity) {
        mActivity =  activity;
        App app =(App) mActivity.getApplication();
        vuforiaAppSession = app.vuforiaSession;

//        mTextures = texture;

        world = new World();
        world.setAmbientLight(100, 100, 100);
        // set the following value according to your need, so the object won't be disappeared.
        world.setClippingPlanes(2.0f, 3000.0f);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);

        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");


        try {
            TextureManager.getInstance().addTexture("texture" ,new Texture(mActivity.getAssets().open("Cottage Texture.jpg")));
            InputStream streamObj = mActivity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl = mActivity.getAssets().open("Snow covered CottageOBJ.mtl");

            InputStream streamObj2 = mActivity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl2 = mActivity.getAssets().open("Snow covered CottageOBJ.mtl");

            home1 = null;
            home2 = null;

            home1 = loadModel("house", streamObj, streamMtl,texSampler2DHandle);
            home1.translate(100.0f, 0.0f, 0.0f);
            home1.rotateX(30.0f);

            home2 = loadModel("house", streamObj2, streamMtl2,texSampler2DHandle);
            home2.translate(0.0f, 0.0f, 0.0f);
            home2.rotateX(30.0f);

            world.addObject(home1);
            world.addObject(home2);

            world.buildAllObjects();


        }catch (Exception e){
            System.out.println("Not rendering obj");
            System.out.println(e);
        }
        cam = world.getCamera();

//        sun = new Light(world);
//        sun.setIntensity(250, 250, 250);

        // for older Android versions, which had massive problems with garbage collection
        MemoryHelper.compact();

    }


    private Object3D loadModel(String nameObject, InputStream streamObj, InputStream streamMtl, int texSampler2DHandle) throws IOException {
        Object3D[] model = Loader.loadOBJ(streamObj,streamMtl, 1.5f);
        Object3D o3d = new Object3D(0);
        Object3D temp = null;

        for (int i = 0; i < model.length; i++) {
            temp = model[i];
            temp.setCenter(SimpleVector.ORIGIN);
            temp.setTexture("texture");
            temp.setRotationMatrix(new Matrix());
            o3d = Object3D.mergeObjects(o3d, temp);
            o3d.strip();
            o3d.build();

            // activate texture 0, bind it, and pass to shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//          GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(4).mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandle, 0);
        }
        return o3d;
    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();

        updateCamera();
        world.renderScene(fb);
        world.draw(fb);
        fb.display();

    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering(); // NOTE: Cocokin sama cpp - DONE

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        if (fb != null) {
            fb.dispose();
        }
        fb = new FrameBuffer(width, height);
        Config.viewportOffsetAffectsRenderTarget = true;

        updateRendering(width, height);

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering() {
        mRenderer = Renderer.getInstance();

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
    }

    private void updateRendering(int width, int height) {
        // Reconfigure the video background
        vuforiaAppSession.configureVideoBackground();

        CameraCalibration camCalibration = com.vuforia.CameraDevice.getInstance().getCameraCalibration();
        Vec2F size = camCalibration.getSize();
        Vec2F focalLength = camCalibration.getFocalLength();
        float fovyRadians = (float) (2 * Math.atan(0.5f * size.getData()[1] / focalLength.getData()[1]));
        float fovRadians = (float) (2 * Math.atan(0.5f * size.getData()[0] / focalLength.getData()[0]));

        setFovy(fovRadians);
        setFov(fovyRadians);
    }

    // The render function.
    private void renderFrame() {
        // clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // get the state, and mark the beginning of a rendering section
        State state = mRenderer.begin();
        // explicitly render the video background
        mRenderer.drawVideoBackground();

        float[] modelviewArray = new float[16];
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // get the trackable
            findTrackable = true;
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);

            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());
            Matrix44F inverseMV = VuforiaMath.Matrix44FInverse(modelViewMatrix);
            Matrix44F invTranspMV = VuforiaMath.Matrix44FTranspose(inverseMV);

            modelviewArray = invTranspMV.getData();
            updateModelviewMatrix(modelviewArray);

        }
        // hide the objects when the targets are not detected
        if (state.getNumTrackableResults() == 0) {
            float m [] = {
                    1,0,0,0,
                    0,1,0,0,
                    0,0,1,0,
                    0,0,-10000,1
            };
            modelviewArray = m;
            updateModelviewMatrix(modelviewArray);
        }

        if(findTrackable){
            new AudioApiHandler(mActivity, AudioOptions.Play, Sounds.the_good_the_bad_the_ugly).execute();
        }

        mRenderer.end();
    }


    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }

    private void updateModelviewMatrix(float mat[]) {
        modelViewMat = mat;
    }

    private void updateCamera() {
        if (modelViewMat != null) {
            float[] m = modelViewMat;

            final SimpleVector camUp;

            camUp = new SimpleVector(-m[0], -m[1], -m[2]);

            final SimpleVector camDirection = new SimpleVector(m[8], m[9], m[10]);
            final SimpleVector camPosition = new SimpleVector(m[12], m[13], m[14]);

            cam.setOrientation(camDirection, camUp);
            cam.setPosition(camPosition);

            cam.setFOV(fov);
            cam.setYFOV(fovy);
        }
    }

    private void setFov(float fov) {
        this.fov = fov;
    }

    private void setFovy(float fovy) {
        this.fovy = fovy;
    }
}

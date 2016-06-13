package com.blend.mediamarkt.utils;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by geddy on 09/06/16.
 */
abstract public class BaseScene implements GLSurfaceView.Renderer{
    public boolean mIsActive = false;

    public abstract void onSurfaceCreated(GL10 gl, EGLConfig config);
    public abstract void onDrawFrame(GL10 gl);

}

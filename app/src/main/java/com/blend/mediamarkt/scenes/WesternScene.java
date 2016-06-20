package com.blend.mediamarkt.scenes;
import android.opengl.GLES20;

import com.blend.mediamarkt.App;
import com.blend.mediamarkt.scenes.BaseScene;
import com.blend.mediamarkt.activities.VuforiaActivity;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;

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

/**
 * Created by geddy on 21/05/16.
 */

public class WesternScene extends BaseScene {

    private int texSampler2DHandle;

    private Light sun;
    private Object3D home1;
    private Object3D home2;

    public WesternScene(VuforiaActivity activity) {
        this.activity =  activity;

        vuforiaAppSession = App.vuforiaSession;

        world = new World();
        world.setAmbientLight(100, 100, 100);
        // set the following value according to your need, so the object won't be disappeared.
        world.setClippingPlanes(2.0f, 3000.0f);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);

        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");


        try {
            TextureManager.getInstance().addTexture("texture" ,new Texture(this.activity.getAssets().open("Cottage Texture.jpg")));
            InputStream streamObj = this.activity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl = this.activity.getAssets().open("Snow covered CottageOBJ.mtl");

            InputStream streamObj2 = this.activity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl2 = this.activity.getAssets().open("Snow covered CottageOBJ.mtl");

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
}

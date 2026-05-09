package com.bella.testapp.view;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {

    public MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(3);
        setRenderer(new MyRenderer(context));
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
}
package com.lbo.book.openglbasicsquaretex;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by tomcat2 on 2015-06-02.
 */
public class MainGLSurfaceView extends GLSurfaceView {
    private final MainGLRenderer mRenderer;

    public MainGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRenderer = new MainGLRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}

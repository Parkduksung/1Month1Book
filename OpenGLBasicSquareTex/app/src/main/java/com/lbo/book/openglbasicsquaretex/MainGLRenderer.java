package com.lbo.book.openglbasicsquaretex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tomcat2 on 2015-06-02.
 */
public class MainGLRenderer implements GLSurfaceView.Renderer {
    private Tex mTex;
    private Context mContext;

    public MainGLRenderer(Context context){
        mContext = context;
    }
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 0.0f);
        mTex.draw();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        init();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void init() {
        Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier(
                "drawable/b_spearman1", null, mContext.getPackageName()
        ));
        mTex = new Tex(this, image);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

}

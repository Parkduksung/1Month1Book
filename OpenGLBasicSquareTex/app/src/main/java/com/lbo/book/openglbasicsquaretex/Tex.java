package com.lbo.book.openglbasicsquaretex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
// 사각형 출력
public class Tex {
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    protected FloatBuffer mUvBuffer;
    protected static float mUvs[];
    private final float[] mMtrxView = new float[16];

    private int mHandleBitmap;

    // 쉐이더 이미지
    public static final String vs_Image = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" + "}";
    public static final String fs_Image = "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";
    float mSquareCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f,  0.5f, 0.0f };
    // top right
    private short mDrawOrder[] = { 0, 1, 2, 0, 2, 3 };
    // order to draw vertices
    private final int mProgram;
    private int mPositionHandle;
    MainGLRenderer mMainGLRenderer;

    public Tex(MainGLRenderer mainGLRenderer, Bitmap bitmap) {
        mMainGLRenderer = mainGLRenderer;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                mSquareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mSquareCoords);
        mVertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                mDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);
        mUvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        ByteBuffer bbUvs = ByteBuffer.allocateDirect(mUvs.length * 4);
        bbUvs.order(ByteOrder.nativeOrder());
        mUvBuffer = bbUvs.asFloatBuffer();
        mUvBuffer.put(mUvs);
        mUvBuffer.position(0);
        int vertexShader = mMainGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vs_Image);
        int fragmentShader = mMainGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fs_Image);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        mHandleBitmap = getImageHandle(bitmap);

    }
    public void draw() { GLES20.glUseProgram(mProgram);
        Matrix.setIdentityM(mMtrxView, 0);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        int texCoordLoc = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        GLES20.glEnableVertexAttribArray(texCoordLoc);
        GLES20.glVertexAttribPointer(texCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);
        int mtrxhandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mMtrxView, 0);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHandleBitmap);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(texCoordLoc);
    }
    private int getImageHandle(Bitmap bitmap){
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return texturenames[0];

    }
}

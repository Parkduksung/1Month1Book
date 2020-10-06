package com.rsupport.litecam.record;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.view.Surface;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.util.LLog;
import com.rsupport.litecam.util.RecordScreen;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RecordHelperGPUInput {
    private final boolean isLog = true;
    private static final String TAG = "RecordHelper";

    private Context conetxt;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

    private Surface mSurface;
    private ResolutionInfo mResolutionInfo;

    // 4 각형 그림을 그릴 포인터 지정
    private final short[] mIndicesData = {0, 1, 2, 0, 2, 3};

    private int[] mTextureIDs;
    private int mProgram;
    private int muMVPMatrixHandle;
    private FloatBuffer mTriangleVertices;
    private FloatBuffer mTriangleVerticesWatermark;
    private ShortBuffer mIndices;

    private float[] mMVPMatrix = new float[16];

    private int maPositionHandle;
    private int maTextureHandle;

    /**
     * Creates a CodecInputSurface from a surface.
     */
    public RecordHelperGPUInput(Context context, Surface surface, ResolutionInfo resolutionInfo) {
        if (surface == null) {
            throw new NullPointerException();
        }
        this.conetxt = context;
        mSurface = surface;
        mResolutionInfo = resolutionInfo;

        // initialize byte buffer for the draw list
        mIndices = ByteBuffer.allocateDirect(mIndicesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(mIndicesData).position(0);


        eglSetup();
    }

    /**
     * Prepares EGL. We want a GLES 2.0 context and a surface that supports
     * recording.
     * <p>
     * 화면을 가상으로 생성하기 위해서 EGL을 사용. Encoder에서 사용하기에 화면이 출력될 필요는 없어 아래와 같이
     * EGL로 화면 처리
     */
    private void eglSetup() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }

        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for recording and OpenGL ES 2.0.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE};

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
        checkEglError("eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attrib_list, 0);
        checkEglError("eglCreateContext");

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {EGL14.EGL_NONE};
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface, surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");

        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        checkEglError("eglMakeCurrent");
    }

    /**
     * Discards all resources held by this class, notably the EGL context.
     * Also releases the Surface that was passed to our constructor.
     */
    public void release() {
        if (EGL14.eglGetCurrentContext().equals(mEGLContext)) {
            // Clear the current context and surface to ensure they are discarded immediately.
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }

        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);

        mSurface.release();

        // null everything out so future attempts to use this object will cause an NPE
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;

        mSurface = null;
    }

    /**
     * Checks for EGL errors. Throws an exception if one is found.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    /**
     * Calls eglSwapBuffers. Use this to "publish" the current frame.
     */
    public boolean swapBuffers() {
        if (mEGLDisplay == null) {
            LLog.e(isLog, "mEGLDisplay == null");
        }
        if (mEGLSurface == null) {
            LLog.e(isLog, "mEGLSurface == null");
        }
        boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        checkEglError("eglSwapBuffers");
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL. Time is expressed in
     * nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
        checkEglError("eglPresentationTimeANDROID");
    }

    /**
     * Generates a frame of data using GL commands. we have an draw RGB data.
     */
    public void generateSurfaceFrame(ByteBuffer buffer) {
        // Clear the color buffer
        GLES20.glClearColor(.0f, .0f, .0f, .0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        // Alpha com.rsupport.setting
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        /**
         * capture image mapping
         *
         * 좌표를 Shader의 각 함수에 맵핑하는 부분과 RGBA 데이터를 출력하는 코드
         */
        // get handle to vertex shader's a_position member
        // Shader의 Position(vertex)과 Texture(Fragment)를 맵핑하기 위한 코드로 mTriangleVertices의 좌표값을 활용
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkEglError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkEglError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkEglError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkEglError("glEnableVertexAttribArray maTextureHandle");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0]);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mResolutionInfo.bytePerLine / 4, mResolutionInfo.dstScreenSize.y, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);


        /**
         * overlay image mapping
         */
        if (RecordSet.ISWatermark) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[1]);

            // get handle to vertex shader's a_position member
            mTriangleVerticesWatermark.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVerticesWatermark);
            checkEglError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkEglError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVerticesWatermark.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVerticesWatermark);
            checkEglError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkEglError("glEnableVertexAttribArray maTextureHandle");

            Matrix.setIdentityM(mMVPMatrix, 0);
            Matrix.setRotateM(mMVPMatrix, 0, (float) RecordScreen.getDegressWatermark(mResolutionInfo.rotate), 0, 0, -1.0f);
            if (mResolutionInfo.rotate == Surface.ROTATION_0 || mResolutionInfo.rotate == Surface.ROTATION_180) {
                Matrix.translateM(mMVPMatrix, 0, 1.f - 0.45f, -(1.f - 0.2f), 0); // 화면 이동

            } else { // ROTATION_90, ROTATION_270
                Matrix.translateM(mMVPMatrix, 0, 1.f - 0.45f, -(1.f - 0.2f), 0); // 화면 이동
            }
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);
        }
    }


    /**
     * OpenGL ES 2.0 Shader code build
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkEglError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            LLog.e(TAG, "Could not compile shader " + shaderType + ":");
            LLog.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkEglError("glCreateProgram");
        if (program == 0) {
            LLog.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkEglError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkEglError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            LLog.e(TAG, "Could not link program: ");
            LLog.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }


    /**
     * OpenGL ES 2.0 Vertex Shader code
     * Shader의 좌표를 설정하는 코드
     */
    final String vertexShaderSource =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 a_position; \n" +
                    "attribute vec2 a_texCoord; \n" +
                    "varying vec2 v_texCoord; \n" +

                    "void main() \n" +
                    "{ \n" +
                    "  gl_Position = uMVPMatrix * a_position;\n" +
                    " 	v_texCoord = a_texCoord; \n" +
                    "} \n";

    /**
     * OpenGL ES 2.0 Fragment Shader code
     * Shader의 화면을 출력하는 코드(RGBA를 Surface에 출력하기 위한 코드)
     */
    final String fragmentShaderSource =
            "precision mediump float; \n" +
                    "uniform sampler2D texture; \n" +
                    "varying vec2 v_texCoord;\n" +

                    "void main() \n" +
                    "{ \n" +
                    "	gl_FragColor = texture2D(texture, v_texCoord); \n" +
                    "} \n";

    /**
     * OpenGL ES 2.0 Renderer prepare
     */
    Bitmap bitmap;

    public void prepareRenderer(RecordFormat format) {
        int exponentWidth = getMinPowerByTwo(mResolutionInfo.bytePerLine / 4);
        int exponentHeight = getMinPowerByTwo(mResolutionInfo.dstScreenSize.y);

        /*
         * 실제 비디오 사이즈와 2의 지수승을 나누어 실제 확대해야할 비율 지정
         */
        float xValue, yValue;
        xValue = ((float) (mResolutionInfo.bytePerLine / 4) / (float) exponentWidth);
        yValue = ((float) mResolutionInfo.dstScreenSize.y / (float) exponentHeight);


        /**
         * 해당 코드는 화면의 좌표를 설정하는 코드이며, 텍스쳐 크기가 16의 배수가 아닌 경우를 처리하기 위하여 16의 배수로 확장하고,
         * 이를 아래와 같이 좌표로 설정하는 부분
         *
         * verticesData 값 중 9, 13, 14, 18 번의 값을 2의 지수승으로 나눈 비율로 변경 (확대 사이즈로 처리)
         * private final float[] verticesData = {
         * 			-1.f, 1.f, 0.0f, // Position 0
         * 			0.0f, 0.0f, // TexCoord 0
         * 			-1.f, -1.f, 0.0f, // Position 1
         * 			1.f, -1.f, 0.0f, // Position 2
         * 			xValue, yValue, // TexCoord 2
         * 			1.f, 1.f, 0.0f, // Position 3
         * 			xValue, 0.0f // TexCoord 3
         *
         * 의로 정의하며 좌표의 값은 0.0 ~ 1.0의 비율값을 가진다.
         */
        float[] verticesData = {
                -1.f, 1.f, 0.0f, // Position 0
                0.0f, 0.0f, // TexCoord 0
                -1.f, -1.f, 0.0f, // Position 1
                0.0f, yValue, // TexCoord 1
                1.f, -1.f, 0.0f, // Position 2
                xValue, yValue, // TexCoord 2
                1.f, 1.f, 0.0f, // Position 3
                xValue, 0.0f // TexCoord 3
        };

        /**
         *  OpenGL ES 2.0 에서 Shader를 사용하기 위한 코드
         *  OpenGL ES에서 Shader 코드를 빌드하도록 처리
         */
        mProgram = createProgram(vertexShaderSource, fragmentShaderSource);
        if (mProgram == 0) {
            LLog.e("Could not create program.");
            return;
        }

        /**
         * 빌드한 Shader에 함수별로 매핑하여 사용하기 위한 코드
         */
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        checkEglError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for a_position");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        checkEglError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for a_texCoord");
        }

        // 해당 코드는 화면의 좌표를 조절하기 위한 코드로 불필요시 사용하지 않아도 됩니다.
        // 화면의 텍스쳐를 상하좌우 또는 회전 하기위해서 사용하는 코드입니다.
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkEglError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        mTextureIDs = new int[2];
        GLES20.glGenTextures(2, mTextureIDs, 0);
        checkEglError("glBindTexture mTextureID");

        /*
         * 화면에 출력하기 위한 텍스쳐 RGBA texture 생성
         */
        mTriangleVertices = ByteBuffer.allocateDirect(verticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(verticesData).position(0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, exponentWidth, exponentHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);


        /**
         * Watermark image init
         */
        if (RecordSet.ISWatermark) {
            switch (mResolutionInfo.rotate) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    setWatermarkVerticesData(.4f, .09f);
                    break;

                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    setWatermarkVerticesData(.35f, .17f);
                    break;
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[1]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            try {
                InputStream is = conetxt.getAssets().open(format.getWatermarkImage(RecordFormat.KEY_WATERMARK_IMAGE).getWatermarkName(conetxt, format));
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                bitmap = Bitmap.createScaledBitmap(bitmap, getMinPowerByTwo(bitmap.getWidth()), getMinPowerByTwo(bitmap.getHeight()), false);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                bitmap.recycle();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setWatermarkVerticesData(float x, float y) {
        float[] verticesData = {
                // x, 	y,		z
                -x, y, 0.0f,    // Position 0
                // u,		v
                .0f, 0.0f,            // TexCoord 0
                -x, -y, 0.0f,    // Position 1
                .0f, 1.0f,            // TexCoord 1
                x, -y, 0.0f,    // Position 2
                1.0f, 1.0f,            // TexCoord 2
                x, y, 0.0f,    // Position 3
                1.0f, 0.0f            // TexCoord 3
        };
        mTriangleVerticesWatermark = ByteBuffer.allocateDirect(verticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVerticesWatermark.put(verticesData).position(0);
    }

    /**
     * 2 의 배수 확인
     *
     * @param value
     * @return
     */
    private int getMinPowerByTwo(int value) {
        int result = 2;

        do {
            result *= 2;
        } while (result < value);

        return result;
    }
}

package com.rsupport.srn30.screen.encoder.gl;

import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.Surface;

import com.rsupport.srn30.screen.encoder.surface.OnSurfaceDrawable;
import com.rsupport.util.rslog.MLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by kwcho on 3/20/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GLSurfaceDrawable implements OnSurfaceDrawable {
    // 4 각형 그림을 그릴 포인터 지정
    private final short[] mIndicesData = { 0, 1, 2, 0, 2, 3 };

    private final int FLOAT_SIZE_BYTES = 4;
    private final int EGL_RECORDABLE_ANDROID = 0x3142;
    private final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private FloatBuffer mTriangleVertices = null;
    private ShortBuffer mIndices = null;

    private long startTime = 0;
    private float[] mMVPMatrix = null;

    private int[] mTextureIDs;
    private int mProgram;

    private int maPositionHandle;
    private int maTextureHandle;
    private int muMVPMatrixHandle;

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

    private Surface encoderInputSurface = null;
    private int width = 0;
    private int height = 0;

    public GLSurfaceDrawable(Surface encoderInputSurface, int width, int height){
        MLog.d("GLSurfaceDrawable");
        this.encoderInputSurface = encoderInputSurface;
        this.width = width;
        this.height = height;
    }

    public class GLDrawableInfo{
        public String filePath = null;
        public float x = 0f;
        public float y = 0f;
    }

    @Override
    public void initialized() {
        MLog.d("width.%d, height.%d", width, height);

        mMVPMatrix = new float[16];
        mIndices = ByteBuffer.allocateDirect(mIndicesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(mIndicesData).position(0);

        eglSetup(encoderInputSurface);

        try {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            checkEglError("eglMakeCurrent");
            prepareRenderer(width, height);
        } catch (Exception e) {
            MLog.e(Log.getStackTraceString(e));
        } finally{
            if (EGL14.eglGetCurrentContext().equals(mEGLContext)) {
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            }
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    public void release() {
        if (EGL14.eglGetCurrentContext().equals(mEGLContext)) {
            // Clear the current context and surface to ensure they are discarded immediately.
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }

        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);

        // null everything out so future attempts to use this object will cause an NPE
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;

        mMVPMatrix = null;
        startTime = 0;

    }

    @Override
    public void onDrawable(ByteBuffer imageBuffer, int width, int height, int pixelStride, int rowStride, int rowPadding) {
        generateSurfaceFrame(imageBuffer, width, height, pixelStride, rowStride, rowPadding);
    }

    private void eglSetup(Surface encoderInputSurface) {
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
                EGL14.EGL_NONE };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
        checkEglError("eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attrib_list, 0);
        checkEglError("eglCreateContext");

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = { EGL14.EGL_NONE };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], encoderInputSurface, surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");

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

    public void prepareRenderer(int width, int height) {
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
                0.0f, 1.0f, // TexCoord 1
                1.f, -1.f, 0.0f, // Position 2
                1.0f, 1.0f, // TexCoord 2
                1.f, 1.f, 0.0f, // Position 3
                1.0f, 0.0f // TexCoord 3
        };

        /**
         *  OpenGL ES 2.0 에서 Shader를 사용하기 위한 코드
         *  OpenGL ES에서 Shader 코드를 빌드하도록 처리
         */
        mProgram = createProgram(vertexShaderSource, fragmentShaderSource);
        if (mProgram == 0) {
            MLog.e("Could not create program.");
            return ;
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

        mTextureIDs = new int[1];
        GLES20.glGenTextures(1, mTextureIDs, 0);
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

//		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, exponentWidth, exponentHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
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
            MLog.e("Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkEglError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkEglError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            MLog.e("Could not link program: ");
            MLog.e(GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
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
            MLog.e("Could not compile shader " + shaderType + ":");
            MLog.e(" " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }


    public void generateSurfaceFrame(ByteBuffer buffer, int width, int height, int pixelStride, int rowStride, int rowPadding) {
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

        // Clear the color buffer
        GLES20.glClearColor(.0f, .0f, .0f, .0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        // Alpha setting
        GLES20.glEnable ( GLES20.GL_BLEND );
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
//		checkEglError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
//		checkEglError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
//		checkEglError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
//		checkEglError("glEnableVertexAttribArray maTextureHandle");

        int stride = (rowStride/pixelStride);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, stride, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
//		GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, (rowStride/pixelStride), height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);


        float scaleX = (float)stride/(float)width;
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.translateM(mMVPMatrix, 0, scaleX-1.0f, 0, 0);
        Matrix.scaleM(mMVPMatrix, 0, scaleX, 1, 1);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);

        // Sends the presentation time stamp to EGL. Time is expressed in
        // nanoseconds.
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, getPresentationUTime());
//		checkEglError("eglPresentationTimeANDROID");

        swapBuffers();

        if (EGL14.eglGetCurrentContext().equals(mEGLContext)) {
            // Clear the current context and surface to ensure they are discarded immediately.
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
    }

    private boolean swapBuffers() {
        if (mEGLDisplay == null) {
            MLog.e("mEGLDisplay == null");
            return false;
        }
        if (mEGLSurface == null) {
            MLog.e("mEGLSurface == null");
            return false;
        }
        boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
//		checkEglError("eglSwapBuffers");
        return result;
    }

    private long getPresentationUTime(){
        return (long) (System.currentTimeMillis() - startTime) * 1000 * 1000;
    }
}

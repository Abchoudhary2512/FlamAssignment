package com.example.assignmentflamrnd;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRenderer";
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    vTexCoord = aTexCoord;\n" +
            "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform sampler2D uTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
            "}";
    private FloatBuffer vertexBuffer, texCoordBuffer;
    private int program;
    private int textureId = -1;
    private int aPosition, aTexCoord, uTexture;
    private int frameWidth = 0, frameHeight = 0;
    private ByteBuffer frameBuffer = null;
    private boolean frameAvailable = false;
    private final Object frameLock = new Object();
    private static final float[] VERTICES = {
            -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f
    };
    private static final float[] TEX_COORDS = {
            0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f
    };

    public GLRenderer() {
        Log.d(TAG, "GLRenderer constructor");
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(VERTICES).position(0);
        texCoordBuffer = ByteBuffer.allocateDirect(TEX_COORDS.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(TEX_COORDS).position(0);
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        checkGlError("createProgram");
        if (program == 0) {
            Log.e(TAG, "Failed to create OpenGL program");
        } else {
            Log.d(TAG, "OpenGL program created: " + program);
        }
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        checkGlError("glGetAttribLocation aTexCoord");
        uTexture = GLES20.glGetUniformLocation(program, "uTexture");
        checkGlError("glGetUniformLocation uTexture");
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("glGenTextures");
        textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        checkGlError("glBindTexture");
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameteri");
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        Log.d(TAG, "GLRenderer onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        Log.d(TAG, "onDrawFrame");
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkGlError("glClear");
        GLES20.glUseProgram(program);
        checkGlError("glUseProgram");
        GLES20.glEnableVertexAttribArray(aPosition);
        checkGlError("glEnableVertexAttribArray aPosition");
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        checkGlError("glVertexAttribPointer aPosition");
        GLES20.glEnableVertexAttribArray(aTexCoord);
        checkGlError("glEnableVertexAttribArray aTexCoord");
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
        checkGlError("glVertexAttribPointer aTexCoord");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        checkGlError("glBindTexture");
        boolean drewTexture = false;
        synchronized (frameLock) {
            if (frameAvailable && frameBuffer != null && frameWidth > 0 && frameHeight > 0) {
                frameBuffer.position(0);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, frameWidth, frameHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, frameBuffer);
                checkGlError("glTexImage2D");
                Log.d(TAG, "glTexImage2D called: " + frameWidth + "x" + frameHeight);
                frameAvailable = false;
                drewTexture = true;
            }
        }
        GLES20.glUniform1i(uTexture, 0);
        checkGlError("glUniform1i");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
        if (!drewTexture) {
            Log.d(TAG, "No texture available, drawing fallback color");
        }
    }

    public void updateFrame(byte[] rgba, int width, int height) {
        Log.d(TAG, "updateFrame called: " + width + "x" + height + ", first bytes: " + (rgba.length > 4 ? (rgba[0] & 0xFF) + "," + (rgba[1] & 0xFF) + "," + (rgba[2] & 0xFF) + "," + (rgba[3] & 0xFF) : "short"));
        synchronized (frameLock) {
            if (frameBuffer == null || frameWidth != width || frameHeight != height) {
                frameWidth = width;
                frameHeight = height;
                frameBuffer = ByteBuffer.allocateDirect(width * height * 4);
            }
            frameBuffer.position(0);
            frameBuffer.put(rgba);
            frameBuffer.position(0);
            frameAvailable = true;
        }
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        } else {
            Log.d(TAG, "Program linked successfully");
        }
        return program;
    }

    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ": " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        } else {
            Log.d(TAG, "Shader " + type + " compiled successfully");
        }
        return shader;
    }

    public int getTextureId() {
        Log.d(TAG, "GLRenderer getTextureId: " + textureId);
        return textureId;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
        }
    }
}

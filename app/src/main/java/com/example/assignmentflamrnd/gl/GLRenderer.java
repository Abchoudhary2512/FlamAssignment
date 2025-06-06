package com.example.assignmentflamrnd.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRenderer";

    // Vertex shader with texture coordinates
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * aPosition;" +
            "  vTexCoord = aTexCoord;" +
            "}";

    // Fragment shader for texture sampling
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(sTexture, vTexCoord);" +
            "}";

    // Square vertices with texture coordinates
    private static final float[] VERTEX_COORDS = {
            -1.0f, -1.0f, 0.0f,  // 0 bottom left
            1.0f, -1.0f, 0.0f,   // 1 bottom right
            -1.0f, 1.0f, 0.0f,   // 2 top left
            1.0f, 1.0f, 0.0f     // 3 top right
    };

    private static final float[] TEXTURE_COORDS = {
            1.0f, 1.0f,  // 0 bottom left (flipped)
            0.0f, 1.0f,  // 1 bottom right (flipped)
            1.0f, 0.0f,  // 2 top left (flipped)
            0.0f, 0.0f   // 3 top right (flipped)
    };

    private static final short[] INDICES = {
            0, 1, 2,  // First triangle
            2, 1, 3   // Second triangle
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ByteBuffer indexBuffer;
    private int mProgram;
    private int mTextureID;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mMVPMatrixHandle;
    private int mTextureHandle;
    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    private int mWidth;
    private int mHeight;
    private byte[] mFrameData;
    private boolean mFrameUpdated = false;

    public GLRenderer() {
        // Initialize vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_COORDS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(VERTEX_COORDS);
        vertexBuffer.position(0);

        // Initialize texture coordinate buffer
        ByteBuffer tb = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(TEXTURE_COORDS);
        textureBuffer.position(0);

        // Initialize index buffer
        ByteBuffer ib = ByteBuffer.allocateDirect(INDICES.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib;
        for (short index : INDICES) {
            indexBuffer.putShort(index);
        }
        indexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // Create and compile shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        // Create program
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        // Get handles
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");

        // Create texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // Set up projection matrix
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Use our shader program
        GLES20.glUseProgram(mProgram);

        // Update texture if new frame is available
        if (mFrameUpdated && mFrameData != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(mFrameData));
            mFrameUpdated = false;
        }

        // Set the vertex attributes
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        // Set the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glUniform1i(mTextureHandle, 0);

        // Set the MVP matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICES.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }

    public void updateFrame(byte[] frameData, int width, int height) {
        mFrameData = frameData;
        mWidth = width;
        mHeight = height;
        mFrameUpdated = true;
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Shader compilation failed: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }
} 
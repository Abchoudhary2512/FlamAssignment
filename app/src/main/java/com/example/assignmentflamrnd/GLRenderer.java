package com.example.assignmentflamrnd;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class GLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "GLRenderer";

    private int program;
    private int textureId = -1;

    private int positionHandle;
    private int texCoordHandle;
    private int textureUniformHandle;

    private int width, height;

    private ByteBuffer imageBuffer;  // RGBA pixel data buffer

    // Vertex coordinates for a fullscreen quad (X, Y)
    private final float[] vertexCoords = {
            -1f, 1f,   // top-left
            -1f, -1f,  // bottom-left
            1f, 1f,    // top-right
            1f, -1f    // bottom-right
    };

    // Texture coordinates (S, T)
    private final float[] textureCoords = {
            0f, 0f,  // top-left
            0f, 1f,  // bottom-left
            1f, 0f,  // top-right
            1f, 1f   // bottom-right
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public GLRenderer(int width, int height) {
        this.width = width;
        this.height = height;

        // Allocate byte buffer for RGBA image (width * height * 4)
        imageBuffer = ByteBuffer.allocateDirect(width * height * 4);
        imageBuffer.order(ByteOrder.nativeOrder());

        // Setup vertex buffers
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexCoords).position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(textureCoords).position(0);
    }

    /**
     * Update the texture with new RGBA image data
     * @param rgbaData byte[] RGBA pixel data (length = width * height * 4)
     */
    public void updateFrame(byte[] rgbaData) {
        if (rgbaData.length != width * height * 4) {
            Log.e(TAG, "updateFrame: Invalid data length");
            return;
        }
        imageBuffer.clear();
        imageBuffer.put(rgbaData);
        imageBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Clear background
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        // Compile shaders and link program
        program = createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        if (program == 0) {
            throw new RuntimeException("Error creating OpenGL program.");
        }

        // Get attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture");

        // Generate texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Allocate empty texture (RGBA)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        // Enable attributes
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // Prepare vertex data
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);

        // Prepare texture coord data
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT,
                false, 0, textureBuffer);

        // Update texture with new frame data
        imageBuffer.position(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0,
                width, height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, imageBuffer);

        // Bind texture uniform
        GLES20.glUniform1i(textureUniformHandle, 0);

        // Draw quad (2 triangles with TRIANGLE_STRIP)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Disable attributes
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            return 0;
        }
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program:");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static final String VERTEX_SHADER_CODE =
            "attribute vec2 aPosition;\n" +
                    "attribute vec2 aTexCoord;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
                    "  vTexCoord = aTexCoord;\n" +
                    "}";

    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "uniform sampler2D uTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
                    "}";
}

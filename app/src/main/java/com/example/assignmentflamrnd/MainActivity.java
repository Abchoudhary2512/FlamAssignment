package com.example.assignmentflamrnd;
import com.example.assignmentflamrnd.gl.GLRenderer;
import com.example.assignmentflamrnd.jni.FrameProcessor;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.assignmentflamrnd.databinding.ActivityMainBinding;



import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import android.opengl.GLSurfaceView;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private FrameProcessor frameProcessor;
    private int currentFilter = 0; // FILTER_NONE
    private TextureView textureView;
    private GLRenderer glRenderer;
    private GLSurfaceView glSurfaceView;
    private int frameCounter = 0;
    private static final int FRAME_SKIP = 2; // Process every 2nd frame


    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;
    private static final String TAG = "MainActivity";

    private long lastFrameTime = 0;
    private int frames = 0;
    private double fps = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FrameProcessor
        frameProcessor = new FrameProcessor();
        Log.d(TAG, "FrameProcessor initialized");

        // Camera preview setup
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);
        Log.d(TAG, "TextureView listener set");

        // OpenGL setup
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glRenderer = new GLRenderer();
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setVisibility(View.GONE);
        Log.d(TAG, "GLSurfaceView and GLRenderer initialized");

        // FPS counter
        TextView fpsCounter = findViewById(R.id.fpsCounter);
        lastFrameTime = System.currentTimeMillis();

        // Setup filter buttons
        setupFilterButtons();
        Log.d(TAG, "Filter buttons set up");
        textureView.setSurfaceTextureListener(textureListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
            Log.d(TAG, "Camera permission requested");
        }
    }

    private void setupFilterButtons() {
        findViewById(R.id.btnRaw).setOnClickListener(v -> {
            Log.d(TAG, "Raw button clicked");
            glSurfaceView.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            currentFilter = 0; // FILTER_NONE
            Log.d(TAG, "Switched to RAW mode");
        });
        findViewById(R.id.btnCanny).setOnClickListener(v -> {
            Log.d(TAG, "Canny button clicked");
            glSurfaceView.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
            currentFilter = 1; // FILTER_CANNY
            Log.d(TAG, "Switched to CANNY mode");
            processAndShowCurrentFrame(currentFilter);
        });
        findViewById(R.id.btnGaussian).setOnClickListener(v -> {
            Log.d(TAG, "Gaussian button clicked");
            glSurfaceView.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
            currentFilter = 2; // FILTER_GAUSSIAN_BLUR
            Log.d(TAG, "Switched to GAUSSIAN mode");
            processAndShowCurrentFrame(currentFilter);
        });
        findViewById(R.id.btnThreshold).setOnClickListener(v -> {
            Log.d(TAG, "Threshold button clicked");
            glSurfaceView.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
            currentFilter = 3; // FILTER_THRESHOLD
            Log.d(TAG, "Switched to THRESHOLD mode");
            processAndShowCurrentFrame(currentFilter);
        });
    }

    private void processAndShowCurrentFrame(int filterType) {
        Log.d(TAG, "processAndShowCurrentFrame called with filterType: " + filterType);
        Bitmap bitmap = textureView.getBitmap();
        if (bitmap != null) {
            // Ensure we're using the preview dimensions
            int width = PREVIEW_WIDTH;
            int height = PREVIEW_HEIGHT;
            
            // Create a scaled bitmap to match preview dimensions
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            
            byte[] inputRGBA = new byte[width * height * 4];
            byte[] outputRGBA = new byte[width * height * 4];
            int[] pixels = new int[width * height];
            scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            
            for (int i = 0; i < pixels.length; i++) {
                inputRGBA[i * 4] = (byte) ((pixels[i] >> 16) & 0xFF);
                inputRGBA[i * 4 + 1] = (byte) ((pixels[i] >> 8) & 0xFF);
                inputRGBA[i * 4 + 2] = (byte) (pixels[i] & 0xFF);
                inputRGBA[i * 4 + 3] = (byte) ((pixels[i] >> 24) & 0xFF);
            }
            
            Log.d(TAG, "Calling frameProcessor.processFrame with dimensions: " + width + "x" + height);
            frameProcessor.processFrame(inputRGBA, width, height, outputRGBA, filterType);
            Log.d(TAG, "Updating GLRenderer with processed frame");
            glRenderer.updateFrame(outputRGBA, width, height);
            runOnUiThread(() -> glSurfaceView.requestRender());
            
            // Clean up
            scaledBitmap.recycle();
        } else {
            Log.w(TAG, "Bitmap from TextureView is null in processAndShowCurrentFrame");
        }
    }

    private void openCamera() {
        Log.d(TAG, "openCamera called");
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Camera permission not granted in openCamera");
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
            Log.d(TAG, "manager.openCamera called");
        } catch (Exception e) {
            Log.e(TAG, "Exception in openCamera", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice onDisconnected");
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice onError: " + error);
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "SurfaceTexture available: " + width + "x" + height);
            openCamera();
        }
        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "SurfaceTexture size changed: " + width + "x" + height);
        }
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "SurfaceTexture destroyed");
            return false;
        }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            frameCounter++;
            if (frameCounter % FRAME_SKIP != 0) return; // Skip this frame

            // FPS calculation
            frames++;
            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= 1000) {
                fps = frames * 1000.0 / (now - lastFrameTime);
                runOnUiThread(() -> {
                    TextView fpsCounter = findViewById(R.id.fpsCounter);
                    fpsCounter.setText(String.format("FPS: %.1f", fps));
                });
                Log.d(TAG, "FPS updated: " + fps);
                frames = 0;
                lastFrameTime = now;
            }

            if (glSurfaceView.getVisibility() == View.GONE) return; // Show only raw preview

            Bitmap bitmap = textureView.getBitmap();
            if (bitmap != null) {
                // Ensure we're using the preview dimensions
                int width = PREVIEW_WIDTH;
                int height = PREVIEW_HEIGHT;
                
                // Create a scaled bitmap to match preview dimensions
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                
                byte[] inputRGBA = new byte[width * height * 4];
                byte[] outputRGBA = new byte[width * height * 4];
                int[] pixels = new int[width * height];
                scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                
                for (int i = 0; i < pixels.length; i++) {
                    inputRGBA[i * 4] = (byte) ((pixels[i] >> 16) & 0xFF);
                    inputRGBA[i * 4 + 1] = (byte) ((pixels[i] >> 8) & 0xFF);
                    inputRGBA[i * 4 + 2] = (byte) (pixels[i] & 0xFF);
                    inputRGBA[i * 4 + 3] = (byte) ((pixels[i] >> 24) & 0xFF);
                }
                
                Log.d(TAG, "Processing frame in onSurfaceTextureUpdated with filter: " + currentFilter);
                frameProcessor.processFrame(inputRGBA, width, height, outputRGBA, currentFilter);
                glRenderer.updateFrame(outputRGBA, width, height);
                runOnUiThread(() -> glSurfaceView.requestRender());
                
                // Clean up
                scaledBitmap.recycle();
            }
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            cameraCaptureSession = session;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                        null, backgroundHandler);
                                Log.d(TAG, "Camera preview started");
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "CameraAccessException in createCameraPreview", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Camera configuration failed");
                            Toast.makeText(MainActivity.this, "Configuration change failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException in createCameraPreview", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread called");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.d(TAG, "stopBackgroundThread called");
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException in stopBackgroundThread", e);
        }
    }

    private void closeCamera() {
        Log.d(TAG, "closeCamera called");
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}

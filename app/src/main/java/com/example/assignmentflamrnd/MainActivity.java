package com.example.assignmentflamrnd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.assignmentflamrnd.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import android.opengl.GLSurfaceView;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("assignmentflamrnd");
    }

    private ActivityMainBinding binding;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private ImageReader imageReader;
    private FrameProcessor frameProcessor;
    private int currentFilter = FrameProcessor.FILTER_NONE;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private TextureView textureView;
    private GLRenderer glRenderer;
    private GLSurfaceView glSurfaceView;

    private static final int CAMERA_PERMISSION_REQUEST = 200;
    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FrameProcessor
        frameProcessor = new FrameProcessor();

        // Camera preview setup
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);

        // OpenGL setup
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glRenderer = new GLRenderer();
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setVisibility(View.GONE);

        // Setup filter buttons
        setupFilterButtons();

        // Ask for permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    private void setupFilterButtons() {
        binding.btnNone.setOnClickListener(v -> {
            setFilter(FrameProcessor.FILTER_NONE);
            glSurfaceView.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
        });
        binding.btnGrayscale.setOnClickListener(v -> {
            setFilter(FrameProcessor.FILTER_GRAYSCALE);
            glSurfaceView.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
        });
        binding.btnCanny.setOnClickListener(v -> {
            setFilter(FrameProcessor.FILTER_CANNY);
            glSurfaceView.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
        });
    }

    private void setFilter(int filterType) {
        currentFilter = filterType;
        String filterName = "None";
        switch (filterType) {
            case FrameProcessor.FILTER_GRAYSCALE:
                filterName = "Grayscale";
                break;
            case FrameProcessor.FILTER_CANNY:
                filterName = "Canny";
                break;
        }
        Toast.makeText(this, "Filter changed to: " + filterName, Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        Log.d(TAG, "openCamera called");
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d("MainActivity", "onSurfaceTextureUpdated called, filter: " + currentFilter);
            if (currentFilter != FrameProcessor.FILTER_NONE) {
                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    // Fill with a red-green gradient for testing
                    byte[] outputRGBA = new byte[width * height * 4];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int i = (y * width + x) * 4;
                            outputRGBA[i] = (byte)(x * 255 / width);   // R
                            outputRGBA[i+1] = (byte)(y * 255 / height); // G
                            outputRGBA[i+2] = 0;                        // B
                            outputRGBA[i+3] = (byte)255;                // A
                        }
                    }
                    Log.d("MainActivity", "Calling updateFrame: " + width + "x" + height + ", first bytes: " +
                        (outputRGBA.length > 4 ? (outputRGBA[0] & 0xFF) + "," + (outputRGBA[1] & 0xFF) + "," + (outputRGBA[2] & 0xFF) + "," + (outputRGBA[3] & 0xFF) : "short"));
                    glRenderer.updateFrame(outputRGBA, width, height);
                    glSurfaceView.requestRender();
                }
            }
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);

            // Create ImageReader for processing if needed (optional)
            // imageReader = ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 2);
            // imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            // captureRequestBuilder.addTarget(imageReader.getSurface()); // Optional

            cameraDevice.createCaptureSession(
                Arrays.asList(surface),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        cameraCaptureSession = session;
                        try {
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                    null, backgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Toast.makeText(MainActivity.this, "Configuration failed", Toast.LENGTH_SHORT).show();
                    }
                },
                backgroundHandler
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        if (imageReader != null) {
            imageReader.close();
        }
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.example.assignmentflamrnd.jni;

import com.example.assignmentflamrnd.filters.FilterManager;

public class FrameProcessor {
    static {
        System.loadLibrary("native-lib");
    }

    public static native void processFrame(byte[] inputFrame, int width, int height,
                                        byte[] outputFrame, int filterType,
                                        int cannyLowThreshold, int cannyHighThreshold,
                                        int cannyBlurAmount, int cannyApertureSize,
                                        int gaussianKernelSize,
                                        int thresholdValue, int thresholdMaxValue);

    public void processFrame(byte[] inputFrame, int width, int height,
                           byte[] outputFrame, int filterType) {
        FilterManager filterManager = FilterManager.getInstance();
        processFrame(inputFrame, width, height, outputFrame, filterType,
                    filterManager.getCannyLowThreshold(),
                    filterManager.getCannyHighThreshold(),
                    filterManager.getCannyBlurAmount(),
                    filterManager.getCannyApertureSize(),
                    filterManager.getGaussianKernelSize(),
                    filterManager.getThresholdValue(),
                    filterManager.getThresholdMaxValue());
    }
} 
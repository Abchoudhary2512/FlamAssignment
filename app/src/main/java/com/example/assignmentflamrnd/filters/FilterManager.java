package com.example.assignmentflamrnd.filters;

public class FilterManager {
    // Filter types
    public static final int FILTER_NONE = 0;
    public static final int FILTER_CANNY = 1;
    public static final int FILTER_GAUSSIAN_BLUR = 2;
    public static final int FILTER_THRESHOLD = 3;

    // Canny parameters
    private int cannyLowThreshold = 50;
    private int cannyHighThreshold = 150;
    private int cannyBlurAmount = 1;
    private int cannyApertureSize = 3;

    // Gaussian blur parameters
    private int gaussianKernelSize = 15;

    // Threshold parameters
    private int thresholdValue = 128;
    private int thresholdMaxValue = 255;

    // Singleton instance
    private static FilterManager instance;

    private FilterManager() {}

    public static synchronized FilterManager getInstance() {
        if (instance == null) {
            instance = new FilterManager();
        }
        return instance;
    }

    // Getters and setters for Canny parameters
    public int getCannyLowThreshold() { return cannyLowThreshold; }
    public void setCannyLowThreshold(int value) { cannyLowThreshold = value; }
    public int getCannyHighThreshold() { return cannyHighThreshold; }
    public void setCannyHighThreshold(int value) { cannyHighThreshold = value; }
    public int getCannyBlurAmount() { return cannyBlurAmount; }
    public void setCannyBlurAmount(int value) { cannyBlurAmount = value; }
    public int getCannyApertureSize() { return cannyApertureSize; }
    public void setCannyApertureSize(int value) { cannyApertureSize = value; }

    // Getters and setters for Gaussian parameters
    public int getGaussianKernelSize() { return gaussianKernelSize; }
    public void setGaussianKernelSize(int value) { gaussianKernelSize = value; }

    // Getters and setters for Threshold parameters
    public int getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(int value) { thresholdValue = value; }
    public int getThresholdMaxValue() { return thresholdMaxValue; }
    public void setThresholdMaxValue(int value) { thresholdMaxValue = value; }

    // Reset all parameters to default values
    public void resetToDefaults() {
        cannyLowThreshold = 50;
        cannyHighThreshold = 150;
        cannyBlurAmount = 1;
        cannyApertureSize = 3;
        gaussianKernelSize = 15;
        thresholdValue = 128;
        thresholdMaxValue = 255;
    }
} 
package com.example.assignmentflamrnd;

public class FrameProcessor {

    static {
        System.loadLibrary("assignmentflamrnd");  // Your native library name
    }

    // Filter type constants matching native implementation
    public static final int FILTER_NONE = 0;
    public static final int FILTER_GRAYSCALE = 1;
    public static final int FILTER_CANNY = 2;

    /**
     * Processes input RGBA frame and outputs processed RGBA frame.
     *
     * @param inputRGBA Input byte array (RGBA)
     * @param width     Frame width
     * @param height    Frame height
     * @param outputRGBA Output byte array (RGBA) - must be allocated by caller
     * @param filterType Filter type constant (FILTER_NONE, FILTER_GRAYSCALE, FILTER_CANNY)
     */
    public native void processFrame(byte[] inputRGBA, int width, int height,
                                    byte[] outputRGBA, int filterType);
}

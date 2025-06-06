package com.example.assignmentflamrnd;

public class FrameProcessor {

    static {
        System.loadLibrary("assignmentflamrnd");  // Your native library name
    }

    public static final int FILTER_CANNY = 0;
    public static final int FILTER_SMOOTH = 1;
    public static final int FILTER_BILATERAL = 2;

    /**
     * Processes input RGBA frame and outputs processed RGBA frame.
     *
     * @param inputRGBA Input byte array (RGBA)
     * @param width     Frame width
     * @param height    Frame height
     * @param outputRGBA Output byte array (RGBA) - must be allocated by caller
     * @param filterType Filter type constant
     */
    public native void processFrame(byte[] inputRGBA, int width, int height,
                                    byte[] outputRGBA, int filterType);
}

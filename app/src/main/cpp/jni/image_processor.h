#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <opencv2/opencv.hpp>

// Constants for filter types
#define FILTER_NONE 0
#define FILTER_CANNY 1
#define FILTER_GAUSSIAN_BLUR 2
#define FILTER_THRESHOLD 3

namespace image_processor {
    void processFrame(cv::Mat& input, cv::Mat& output, int filterType,
                     int cannyLowThreshold, int cannyHighThreshold, int cannyBlurAmount, int cannyApertureSize,
                     int gaussianKernelSize,
                     int thresholdValue, int thresholdMaxValue);
    void applyCanny(cv::Mat& input, cv::Mat& output, int lowThreshold, int highThreshold, 
                    int blurAmount, int apertureSize);
    void applyGaussianBlur(cv::Mat& input, cv::Mat& output, int kernelSize);
    void applyThreshold(cv::Mat& input, cv::Mat& output, int thresholdValue, int maxValue);
}

#endif // IMAGE_PROCESSOR_H 
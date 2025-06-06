#include "image_processor.h"
#include <android/log.h>

namespace image_processor {

void applyCanny(cv::Mat& input, cv::Mat& output, int lowThreshold, int highThreshold, 
                int blurAmount, int apertureSize) {
    cv::Mat blurred, gray, edges;
    cv::GaussianBlur(input, blurred, cv::Size(2 * blurAmount + 1, 2 * blurAmount + 1), 0);
    cv::cvtColor(blurred, gray, cv::COLOR_RGBA2GRAY);
    cv::Canny(gray, edges, lowThreshold, highThreshold, apertureSize);
    cv::cvtColor(edges, output, cv::COLOR_GRAY2RGBA);
}

void applyGaussianBlur(cv::Mat& input, cv::Mat& output, int kernelSize) {
    cv::GaussianBlur(input, output, cv::Size(kernelSize, kernelSize), 0);
}

void applyThreshold(cv::Mat& input, cv::Mat& output, int thresholdValue, int maxValue) {
    cv::Mat gray, thresh;
    cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    cv::threshold(gray, thresh, thresholdValue, maxValue, cv::THRESH_BINARY);
    cv::cvtColor(thresh, output, cv::COLOR_GRAY2RGBA);
}

void processFrame(cv::Mat& input, cv::Mat& output, int filterType,
                 int cannyLowThreshold, int cannyHighThreshold, int cannyBlurAmount, int cannyApertureSize,
                 int gaussianKernelSize,
                 int thresholdValue, int thresholdMaxValue) {

        switch (filterType) {
            case FILTER_CANNY:
                applyCanny(input, output, cannyLowThreshold, cannyHighThreshold, 
                          cannyBlurAmount, cannyApertureSize);
                break;
            case FILTER_GAUSSIAN_BLUR:
                applyGaussianBlur(input, output, gaussianKernelSize);
                break;
            case FILTER_THRESHOLD:
                applyThreshold(input, output, thresholdValue, thresholdMaxValue);
                break;
            default:
                input.copyTo(output);
                break;
        }

        if (output.channels() != 4) {
            cv::cvtColor(output, output, cv::COLOR_BGR2RGBA);
        }
    }

} // namespace image_processor 
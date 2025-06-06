#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <cstring>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Constants for filter types
#define FILTER_NONE 0
#define FILTER_GRAYSCALE 1
#define FILTER_CANNY 2
#define FILTER_GAUSSIAN_BLUR 3
#define FILTER_THRESHOLD 4

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_assignmentflamrnd_FrameProcessor_processFrame(JNIEnv *env, jobject /* this */,
                                                           jbyteArray inputFrame,
                                                           jint width, jint height,
                                                           jbyteArray outputFrame,
                                                           jint filterType) {
    try {
        jbyte* inputPtr = env->GetByteArrayElements(inputFrame, nullptr);
        jbyte* outputPtr = env->GetByteArrayElements(outputFrame, nullptr);

        if (!inputPtr || !outputPtr) {
            return;
        }

        cv::Mat rgbaImg(height, width, CV_8UC4, inputPtr);
        cv::Mat processedImg;

        if (filterType == 1) { // Canny
            int lowThreshold = 50, highThreshold = 150, blurAmount = 1, apertureSize = 3;
            cv::Mat blurred, gray, edges;
            cv::GaussianBlur(rgbaImg, blurred, cv::Size(2 * blurAmount + 1, 2 * blurAmount + 1), 0);
            cv::cvtColor(blurred, gray, cv::COLOR_RGBA2GRAY);
            cv::Canny(gray, edges, lowThreshold, highThreshold, apertureSize);
            cv::cvtColor(edges, processedImg, cv::COLOR_GRAY2RGBA);
        } else if (filterType == 2) { // Gaussian blur
            cv::GaussianBlur(rgbaImg, processedImg, cv::Size(15, 15), 0);
        } else if (filterType == 3) { // Threshold
            cv::Mat gray, thresh;
            cv::cvtColor(rgbaImg, gray, cv::COLOR_RGBA2GRAY);
            cv::threshold(gray, thresh, 128, 255, cv::THRESH_BINARY);
            cv::cvtColor(thresh, processedImg, cv::COLOR_GRAY2RGBA);
        } else {
            rgbaImg.copyTo(processedImg);
        }

        if (processedImg.channels() != 4) {
            cv::cvtColor(processedImg, processedImg, cv::COLOR_BGR2RGBA);
        }

        memcpy(outputPtr, processedImg.data, width * height * 4);

        env->ReleaseByteArrayElements(inputFrame, inputPtr, JNI_ABORT);
        env->ReleaseByteArrayElements(outputFrame, outputPtr, 0);
    } catch (...) {}
}

} // extern "C"

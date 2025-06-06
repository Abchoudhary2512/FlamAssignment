#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <cstring>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_assignmentflamrnd_FrameProcessor_processFrame(JNIEnv *env, jobject /* this */,
                                                           jbyteArray inputFrame,
                                                           jint width, jint height,
                                                           jbyteArray outputFrame,
                                                           jint filterType) {
    // Get pointers to the Java byte arrays
    LOGI("OpenCV version: %s", CV_VERSION);
    jbyte* inputPtr = env->GetByteArrayElements(inputFrame, nullptr);
    jbyte* outputPtr = env->GetByteArrayElements(outputFrame, nullptr);

    if (!inputPtr || !outputPtr) {
        LOGE("Failed to get byte array elements");
        return;
    }

    // Wrap input buffer as OpenCV Mat (RGBA 8UC4)
    cv::Mat rgbaImg(height, width, CV_8UC4, reinterpret_cast<unsigned char*>(inputPtr));
    cv::Mat processedImg;

    if (filterType == 0) {
        // Grayscale filter: convert RGBA -> Gray -> RGBA
        cv::Mat gray;
        cv::cvtColor(rgbaImg, gray, cv::COLOR_RGBA2GRAY);
        cv::cvtColor(gray, processedImg, cv::COLOR_GRAY2RGBA);
    } else if (filterType == 1) {
        // Canny edge detection
        cv::Mat gray, edges;
        cv::cvtColor(rgbaImg, gray, cv::COLOR_RGBA2GRAY);
        cv::Canny(gray, edges, 100, 200);
        cv::cvtColor(edges, processedImg, cv::COLOR_GRAY2RGBA);
    } else {
        // If unknown filter, just copy original frame
        rgbaImg.copyTo(processedImg);
    }

    // Copy processed data back to output array
    memcpy(outputPtr, processedImg.data, width * height * 4);

    // Release arrays
    env->ReleaseByteArrayElements(inputFrame, inputPtr, JNI_ABORT); // no need to copy back input
    env->ReleaseByteArrayElements(outputFrame, outputPtr, 0);       // copy back processed data

    LOGI("Frame processed with filterType=%d", filterType);
}

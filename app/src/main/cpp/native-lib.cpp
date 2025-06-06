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

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_assignmentflamrnd_FrameProcessor_processFrame(JNIEnv *env, jobject /* this */,
                                                           jbyteArray inputFrame,
                                                           jint width, jint height,
                                                           jbyteArray outputFrame,
                                                           jint filterType) {
    try {
        // Get pointers to the Java byte arrays
        LOGI("Processing frame with OpenCV version: %s", CV_VERSION);
        jbyte* inputPtr = env->GetByteArrayElements(inputFrame, nullptr);
        jbyte* outputPtr = env->GetByteArrayElements(outputFrame, nullptr);

        if (!inputPtr || !outputPtr) {
            LOGE("Failed to get byte array elements");
            return;
        }

        // Wrap input buffer as OpenCV Mat (RGBA 8UC4)
        cv::Mat rgbaImg(height, width, CV_8UC4, reinterpret_cast<unsigned char*>(inputPtr));
        cv::Mat processedImg;

        switch (filterType) {
            case FILTER_GRAYSCALE: {
                // Grayscale filter
                cv::Mat gray;
                cv::cvtColor(rgbaImg, gray, cv::COLOR_RGBA2GRAY);
                cv::cvtColor(gray, processedImg, cv::COLOR_GRAY2RGBA);
                LOGI("Applied Grayscale filter");
                break;
            }
            case FILTER_CANNY: {
                // Canny edge detection with optimized parameters
                cv::Mat gray, edges;
                cv::cvtColor(rgbaImg, gray, cv::COLOR_RGBA2GRAY);
                cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.5);
                cv::Canny(gray, edges, 50, 150);
                cv::cvtColor(edges, processedImg, cv::COLOR_GRAY2RGBA);
                LOGI("Applied Canny filter");
                break;
            }
            default: {
                // If unknown filter, just copy original frame
                rgbaImg.copyTo(processedImg);
                LOGI("No filter applied");
                break;
            }
        }

        // Copy processed data back to output array
        if (processedImg.data) {
            size_t size = width * height * 4;
            memcpy(outputPtr, processedImg.data, size);
            LOGI("Copied processed data to output buffer, size: %zu", size);
        } else {
            LOGE("Processed image is empty");
        }

        // Release arrays
        env->ReleaseByteArrayElements(inputFrame, inputPtr, JNI_ABORT);
        env->ReleaseByteArrayElements(outputFrame, outputPtr, 0);

        LOGI("Frame processed successfully with filterType=%d", filterType);
    } catch (const cv::Exception& e) {
        LOGE("OpenCV error: %s", e.what());
    } catch (const std::exception& e) {
        LOGE("Error: %s", e.what());
    } catch (...) {
        LOGE("Unknown error occurred during frame processing");
    }
}

} // extern "C"

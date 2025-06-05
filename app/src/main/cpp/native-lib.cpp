#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/core.hpp>

#define LOG_TAG "OpenCVLog"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_assignmentflamrnd_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

// New function to log the OpenCV version
extern "C" JNIEXPORT void JNICALL
Java_com_example_assignmentflamrnd_MainActivity_logOpenCVVersion(
        JNIEnv* env,
        jobject /* this */) {
    const char* version = CV_VERSION;
    LOGI("OpenCV Version: %s", version);
}

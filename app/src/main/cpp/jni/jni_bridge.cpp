#include <jni.h>
#include <android/log.h>
#include "image_processor.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_assignmentflamrnd_jni_FrameProcessor_processFrame(JNIEnv *env, jobject /* this */,
                                                           jbyteArray inputFrame,
                                                           jint width, jint height,
                                                           jbyteArray outputFrame,
                                                           jint filterType,
                                                           jint cannyLowThreshold,
                                                           jint cannyHighThreshold,
                                                           jint cannyBlurAmount,
                                                           jint cannyApertureSize,
                                                           jint gaussianKernelSize,
                                                           jint thresholdValue,
                                                           jint thresholdMaxValue) {

        jbyte* inputPtr = env->GetByteArrayElements(inputFrame, nullptr);
        jbyte* outputPtr = env->GetByteArrayElements(outputFrame, nullptr);

        if (!inputPtr || !outputPtr) {
            return;
        }

        cv::Mat rgbaImg(height, width, CV_8UC4, inputPtr);
        cv::Mat processedImg(height, width, CV_8UC4);

        image_processor::processFrame(rgbaImg, processedImg, filterType,
                                    cannyLowThreshold, cannyHighThreshold,
                                    cannyBlurAmount, cannyApertureSize,
                                    gaussianKernelSize,
                                    thresholdValue, thresholdMaxValue);

        memcpy(outputPtr, processedImg.data, width * height * 4);

        env->ReleaseByteArrayElements(inputFrame, inputPtr, JNI_ABORT);
        env->ReleaseByteArrayElements(outputFrame, outputPtr, 0);

}

} // extern "C" 
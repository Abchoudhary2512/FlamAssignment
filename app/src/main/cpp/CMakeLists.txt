cmake_minimum_required(VERSION 3.4.1)

project("assignmentflamrnd")

# Paths to OpenCV and project (adjust as needed)
set(pathtoOpenCv "I:/openCvandroid/OpenCV-android-sdk")
set(OpenCV_DIR "${pathtoOpenCv}/sdk/native/jni")

# Find OpenCV package
find_package(OpenCV REQUIRED core imgproc)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Create shared library from native source(s)
add_library(native-lib SHARED
        jni/jni_bridge.cpp
        jni/image_processor.cpp)

# Include directories
target_include_directories(native-lib PRIVATE
        ${OpenCV_INCLUDE_DIRS}
        ${CMAKE_CURRENT_SOURCE_DIR}/jni)

# Link OpenCV libs + Android libs (log, android)
target_link_libraries(native-lib
        ${OpenCV_LIBS}
        android
        log)

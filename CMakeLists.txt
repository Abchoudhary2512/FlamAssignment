cmake_minimum_required(VERSION 3.22.1)

project("assignmentflamrnd")

# Path to OpenCV (adjust as needed)
set(pathtoOpenCv "I:/openCvandroid/OpenCV-android-sdk")
set(OpenCV_DIR "${pathtoOpenCv}/sdk/native/jni")

# Find OpenCV package
find_package(OpenCV REQUIRED)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Create shared library from native source(s)
add_library(${PROJECT_NAME} SHARED native-lib.cpp)

# Find Android log and GLESv2 libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(GLESv2-lib GLESv2)

# Link OpenCV, GLESv2, and Android system libraries
target_link_libraries(${PROJECT_NAME}
    ${OpenCV_LIBS}
    ${GLESv2-lib}
    ${android-lib}
    ${log-lib}
) 
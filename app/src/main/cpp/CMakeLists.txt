cmake_minimum_required(VERSION 3.22.1)

project("assignmentflamrnd")

# Paths to OpenCV and project (adjust as needed)
set(pathtoOpenCv "I:/openCvandroid/OpenCV-android-sdk")
set(OpenCV_DIR "${pathtoOpenCv}/sdk/native/jni")

# Find OpenCV package
find_package(OpenCV REQUIRED)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Create shared library from native source(s)
add_library(${PROJECT_NAME} SHARED native-lib.cpp)

# Link OpenCV libs + Android libs (log, android)
target_link_libraries(${PROJECT_NAME}
        ${OpenCV_LIBS}
        android
        log
)

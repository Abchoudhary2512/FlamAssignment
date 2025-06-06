## Features Implemented

- Real-time camera preview using Camera2 API
- Canny edge detection filter (OpenCV)
- Gaussian blur filter (OpenCV)
- Threshold filter (OpenCV)
- Raw mode (normal camera preview)
- Toggle between raw and filtered modes with buttons
- OpenGL rendering for processed frames
- FPS counter overlay
- Modular code: Camera, JNI, OpenGL, and filter management


## Flow of the Applicatoin
![Editor _ Mermaid Chart-2025-06-06-131032](https://github.com/user-attachments/assets/a25ce4ab-1eed-47ad-9f04-ddf295f7d033)


## Screenshots of the App
![6201968016086911496](https://github.com/user-attachments/assets/be8c39a7-2a2a-4525-953f-ee9d080508a6)
![6201968016086911498](https://github.com/user-attachments/assets/f7879fd2-63c1-4b75-9646-529828e77335)
![6201968016086911499](https://github.com/user-attachments/assets/e0791958-8681-40d2-b681-ebf667e96bd3)
![6201968016086911500](https://github.com/user-attachments/assets/cf3a97e7-d761-4f2a-8245-1710c25a771d)
![6201968016086911501](https://github.com/user-attachments/assets/d8df04d6-2097-417a-ae89-a3f452bf5ef0)
![6201968016086911502](https://github.com/user-attachments/assets/d7698297-26ba-42cb-8dde-237a6a5e3463)
![6201968016086911503](https://github.com/user-attachments/assets/70b1c66a-b441-42c0-8941-b145a449fd46)
![6201968016086911495](https://github.com/user-attachments/assets/8b0793f0-9034-4a3e-aba5-a848a53a01ba)


## Logs when the application working

![building4](https://github.com/user-attachments/assets/fd8e1fc2-7e7c-417c-9a0d-11483f18b27f)
![building5](https://github.com/user-attachments/assets/d4a25723-9d40-4eab-9a4a-d367e231590e)
![building2](https://github.com/user-attachments/assets/25016f0a-c9dc-4f86-9260-7c944ab6c484)
![building3](https://github.com/user-attachments/assets/ad101ab3-cc2d-46ce-a4e4-aec9dc362f19)


## Setup Instructions
1. Download and Set Up Dependencies
Download Android Studio and the OpenCV Android SDK.
Extract the OpenCV SDK to a location of your choice and note the path, as it will be used to import OpenCV as a module in the project.

2. Create the Project in Android Studio
Launch Android Studio and create a Native C++ project.
Set the programming languages to Java and C++17(after the main page it is asked).
Ensure that the following components are preinstalled or enabled:(in native c++ all are preinstalled)
- NDK (Native Development Kit)
- CMake
- Android Platform Tools
- Android Build Tools

3. Run the App
You can run the application using either an emulator or a physical device connected to your system.
 

## Quick Explanation of Architecture

- **Camera2 API**: Captures real-time frames from the device camera
- **TextureView**: Displays the raw camera preview
- **FrameProcessor (Java, JNI)**: Handles frame processing requests and passes data to native code
- **JNI Bridge (C++)**: Receives frames and filter parameters from Java, calls OpenCV functions
- **OpenCV (C++)**: Performs image processing (Canny, Gaussian, Threshold)
- **GLRenderer (Java, OpenGL)**: Renders processed frames efficiently to the screen
- **FilterManager**: Manages filter parameters and settings
- **Frame Flow**: Camera → TextureView (raw) → FrameProcessor (Java) → JNI (C++) → OpenCV → GLRenderer (Java/OpenGL) → Display


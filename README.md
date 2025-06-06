==============================
✅ Features Implemented
==============================
- Real-time camera preview using Camera2 API
- Canny edge detection filter (OpenCV)
- Gaussian blur filter (OpenCV)
- Threshold filter (OpenCV)
- Raw mode (normal camera preview)
- Toggle between raw and filtered modes with buttons
- OpenGL rendering for processed frames
- FPS counter overlay
- Modular code: Camera, JNI, OpenGL, and filter management

==============================
📷 Screenshots or GIF of the Working App
==============================


==============================
⚙️ Setup Instructions
==============================


==============================
🧠 Quick Explanation of Architecture
==============================
- **Camera2 API**: Captures real-time frames from the device camera
- **TextureView**: Displays the raw camera preview
- **FrameProcessor (Java, JNI)**: Handles frame processing requests and passes data to native code
- **JNI Bridge (C++)**: Receives frames and filter parameters from Java, calls OpenCV functions
- **OpenCV (C++)**: Performs image processing (Canny, Gaussian, Threshold)
- **GLRenderer (Java, OpenGL)**: Renders processed frames efficiently to the screen
- **FilterManager**: Manages filter parameters and settings
- **Frame Flow**: Camera → TextureView (raw) → FrameProcessor (Java) → JNI (C++) → OpenCV → GLRenderer (Java/OpenGL) → Display


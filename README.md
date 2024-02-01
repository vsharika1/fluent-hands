# Sign Language Reader App Documentation

Welcome to the Sign Language Reader App, a cutting-edge application that leverages the power of machine learning to interpret hand gestures in real-time. This project is inspired by and utilizes components from [GoogleSample/MediaPipe](https://github.com/googlesamples/mediapipe).

## Overview

The Sign Language Reader App is designed to enhance communication accessibility through technology. Utilizing your device's front camera, it can detect hand landmarks and classify hand gestures by identifying the gesture name and its confidence level. This functionality is not limited to live camera feeds; it extends to processing static images and videos from your device's gallery. At the heart of this application is a **task file**, which is essential for gesture recognition. This file is automatically integrated into the project during the build process via a Gradle script, eliminating the need for manual downloads. However, for those interested in customizing gesture recognition, there's an option to use a personal task file by placing it in the application's *assets* directory. For optimal performance, it's recommended to run this application on a physical Android device.

## Building the Demo with Android Studio

### Prerequisites

Before you begin, ensure you have the following:

- **[Android Studio](https://developer.android.com/studio/index.html)** IDE, with the sample tested on Android Studio Dolphin.
- A physical Android device running at least SDK 24 (Android 7.0 - Nougat) with developer mode activated. The activation process for developer mode varies by device.

### Build Instructions

1. **Open Android Studio:** Start Android Studio and from the Welcome screen, choose "Open an existing Android Studio project."

2. **Select the Project:** In the "Open File or Project" window, navigate to the `mediapipe/examples/gesture_recognizer/android` directory, select it, and click "OK." If prompted to trust the project, choose "Trust."

3. **Gradle Sync:** If a Gradle Sync is requested, confirm by clicking "OK."

4. **Run the Application:** With your Android device connected to your computer (ensure developer mode is enabled), hit the green Run arrow in Android Studio to deploy the app.

### Models and Task File

The process of downloading, extracting, and positioning the models in the *assets* folder is seamlessly managed by the **download.gradle** script, making the setup process smooth and straightforward.

/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.fluenthands

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.examples.fluenthands.logic.ContextHolder
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

class GestureRecognizerHelper(
    var detectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var trackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var handCount: Int = DEFAULT_HAND_NUMBER,
    var currentDelegate: Int = DELEGATE_CPU,
    var mode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val gestureListener: GestureRecognizerListener? = null,
    var isFrontCamera: Boolean = true
) {

    private var gestureRecognizer: GestureRecognizer? = null

    init {
        setupGestureRecognizer()
    }

    fun clearGestureRecognizer() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    // Sets up the gesture recognizer with the current configuration
    fun setupGestureRecognizer() {
        try {
            val baseOptions = configureBaseOptions()
            val gestureOptions = buildGestureOptions(baseOptions)

            gestureRecognizer = GestureRecognizer.createFromOptions(context, gestureOptions)
        } catch (e: Exception) {
            handleGestureSetupException(e)
        }
    }

    // Configures the base options for the gesture recognizer
    private fun configureBaseOptions(): BaseOptions {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.apply {
            setModelAssetPath(MP_RECOGNIZER_TASK)// Sets the path to the gesture recognition model
            setDelegate(getDelegateBasedOnType())// Sets the computation delegate (CPU/GPU)
        }
        return baseOptionBuilder.build() // Builds and returns the configured base options
    }


    // Determines the delegate type based on the current configuration
    private fun getDelegateBasedOnType(): Delegate = when (currentDelegate) {
        DELEGATE_CPU -> Delegate.CPU// Uses CPU for processing
        DELEGATE_GPU -> Delegate.GPU// Uses GPU for faster processing
        else -> Delegate.CPU// Defaults to CPU if no specific delegate is set
    }

    // Builds gesture recognition options from the base configuration
    private fun buildGestureOptions(baseOptions: BaseOptions): GestureRecognizer.GestureRecognizerOptions {
        val optionsBuilder =
            GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(handCount)// Sets the number of hands to detect
                .setMinHandDetectionConfidence(detectionConfidence)// Sets the minimum confidence for detection
                .setMinTrackingConfidence(trackingConfidence)// Sets the minimum confidence for tracking
                .setMinHandPresenceConfidence(minHandPresenceConfidence)// Sets the minimum confidence for hand presence
                .setRunningMode(mode)// Sets the running mode (image or live stream)

        if (mode == RunningMode.LIVE_STREAM) {
            optionsBuilder
                .setResultListener(this::returnLivestreamResult) // Sets the listener for results in live stream mode
                .setErrorListener(this::returnLivestreamError)// Sets the listener for errors in live stream mode
        }
        return optionsBuilder.build()// Builds and returns the gesture options
    }

    // Handles exceptions during gesture recognizer setup
    private fun handleGestureSetupException(e: Exception) {
        val errorMessage = "Gesture recognizer failed to initialize. See error logs for details"
        val errorCode = if (e is RuntimeException) GPU_ERROR else -1 // Assigns an error code based on the exception type
        gestureListener?.onError(errorMessage, errorCode)// Notifies the listener of the error
        Log.e(TAG, "MP Task Vision failed to load the task with error: ${e.message}")
    }

    // Recognizes gestures from a live camera feed using ImageProxy
    fun recognizeLiveStream(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = createBitmapFromImageProxy(imageProxy)
        val rotatedBitmap = rotateBitmap(bitmapBuffer, imageProxy)
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        recognizeAsync(mpImage, frameTime)
    }

    // Creates a bitmap from an ImageProxy
    private fun createBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap {
        val bitmap = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        try {
            val buffer = imageProxy.planes[0].buffer
            bitmap.copyPixelsFromBuffer(buffer) // Copies pixel data from the buffer to the bitmap
        } finally {
            imageProxy.close() // Ensure that the ImageProxy is always closed
        }
        return bitmap
    }

    // Rotates a bitmap based on the orientation of the ImageProxy
    private fun rotateBitmap(bitmap: Bitmap, imageProxy: ImageProxy): Bitmap {
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) // Applies rotation based on image info
            val rotation = if (isFrontCamera) -1f else 1f // Adjusts rotation based on camera type
            postScale(rotation, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    @VisibleForTesting
    fun recognizeAsync(mpImage: MPImage, frameTime: Long) {
        gestureRecognizer?.recognizeAsync(mpImage, frameTime)
    }


    // Returns whether the gesture recognizer has been closed
    fun isClosed(): Boolean {
        return gestureRecognizer == null
    }

    // Processes and returns results for live stream gesture recognition
    private fun returnLivestreamResult(
        result: GestureRecognizerResult, input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        if (!result.gestures().isNullOrEmpty()) {
            ContextHolder.processGestureResult(result)
        }

        gestureListener?.onResults(
            ResultBundle(
                listOf(result), inferenceTime, input.height, input.width
            )
        )
    }

    // Handles errors during live stream gesture recognition
    private fun returnLivestreamError(error: RuntimeException) {
        gestureListener?.onError(
            error.message ?: "An unknown error has occurred"
        )
    }

    companion object {
        val TAG = "GestureRecognizerHelper ${this.hashCode()}"
        private const val MP_RECOGNIZER_TASK = "pjm_v5.task"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_NUMBER = 2
        const val ERROR = 0
        const val GPU_ERROR = 1
    }

    data class ResultBundle(
        val results: List<GestureRecognizerResult>,
        val inferenceTime: Long,
        val imageHeight: Int,
        val imageWidth: Int,
    )

    // Interface for handling gesture recognition events
    interface GestureRecognizerListener {
        fun onError(error: String, errorCode: Int = ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}

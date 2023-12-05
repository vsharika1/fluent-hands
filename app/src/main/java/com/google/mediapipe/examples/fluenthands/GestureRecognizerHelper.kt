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

    // For this example this needs to be a var so it can be reset on changes. If the GestureRecognizer
    // will not change, a lazy val would be preferable.
    private var gestureRecognizer: GestureRecognizer? = null

    init {
        setupGestureRecognizer()
    }

    fun clearGestureRecognizer() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    // Initialize the gesture recognizer using current settings on the
    // thread that is using it. CPU can be used with recognizers
    // that are created on the main thread and used on a background thread, but
    // the GPU delegate needs to be used on the thread that initialized the recognizer
    fun setupGestureRecognizer() {
        try {
            val baseOptions = configureBaseOptions()
            val gestureOptions = buildGestureOptions(baseOptions)

            gestureRecognizer = GestureRecognizer.createFromOptions(context, gestureOptions)
        } catch (e: Exception) {
            handleGestureSetupException(e)
        }
    }

    private fun configureBaseOptions(): BaseOptions {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.apply {
            setModelAssetPath(MP_RECOGNIZER_TASK)
            setDelegate(getDelegateBasedOnType())
        }
        return baseOptionBuilder.build()
    }

    private fun getDelegateBasedOnType(): Delegate = when (currentDelegate) {
        DELEGATE_CPU -> Delegate.CPU
        DELEGATE_GPU -> Delegate.GPU
        else -> Delegate.CPU
    }

    private fun buildGestureOptions(baseOptions: BaseOptions): GestureRecognizer.GestureRecognizerOptions {
        val optionsBuilder =
            GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(handCount)
                .setMinHandDetectionConfidence(detectionConfidence)
                .setMinTrackingConfidence(trackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setRunningMode(mode)

        if (mode == RunningMode.LIVE_STREAM) {
            optionsBuilder
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)
        }
        return optionsBuilder.build()
    }

    private fun handleGestureSetupException(e: Exception) {
        val errorMessage = "Gesture recognizer failed to initialize. See error logs for details"
        // Assuming a default error code, like -1, to represent an unknown or non-runtime error.
        val errorCode = if (e is RuntimeException) GPU_ERROR else -1
        gestureListener?.onError(errorMessage, errorCode)
        Log.e(TAG, "MP Task Vision failed to load the task with error: ${e.message}")
    }

    // Convert the ImageProxy to MP Image and feed it to GestureRecognizer.
    fun recognizeLiveStream(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = createBitmapFromImageProxy(imageProxy)
        val rotatedBitmap = rotateBitmap(bitmapBuffer, imageProxy)
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        recognizeAsync(mpImage, frameTime)
    }

    private fun createBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap {
        val bitmap = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        try {
            val buffer = imageProxy.planes[0].buffer
            bitmap.copyPixelsFromBuffer(buffer)
        } finally {
            imageProxy.close() // Ensure that the ImageProxy is always closed
        }
        return bitmap
    }


    private fun rotateBitmap(bitmap: Bitmap, imageProxy: ImageProxy): Bitmap {
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val rotation = if (isFrontCamera) -1f else 1f
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


    // Return running status of the recognizer helper
    fun isClosed(): Boolean {
        return gestureRecognizer == null
    }

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

    interface GestureRecognizerListener {
        fun onError(error: String, errorCode: Int = ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}

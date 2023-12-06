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
package com.google.mediapipe.examples.fluenthands.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mediapipe.examples.fluenthands.GestureRecognizerHelper
import com.google.mediapipe.examples.fluenthands.MainViewModel
import com.google.mediapipe.examples.fluenthands.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.google.mediapipe.examples.fluenthands.R

class CameraFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "Hand gesture recognizer"
    }

    //variable declarations
    private var _cameraBinding: FragmentCameraBinding? = null

    private val cameraBinding get() = _cameraBinding!!

    private lateinit var gestureHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imgAnalyzer: ImageAnalysis? = null
    private var cam: Camera? = null
    private var camProvider: ProcessCameraProvider? = null
    private var camFacing = CameraSelector.LENS_FACING_FRONT
    private var numResults = 1
    private val gestureAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter().apply {
            adjustDisplayItemCount(numResults)
        }
    }

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        checkPermissions()
        restartGestureRecognizer()
    }

    // Check if the camera permissions are granted
    private fun checkPermissions() {
        if (!PermissionsFragment.checkCameraPermission(requireContext())) {
            navigateToPermissions()
        }
    }

    // Navigate to permissions fragment if permissions are not granted
    private fun navigateToPermissions() {
        Navigation.findNavController(
            requireActivity(), R.id.fragment_container
        ).navigate(R.id.action_camera_to_permissions)
    }

    // Restart gesture recognizer on resume
    private fun restartGestureRecognizer() {
        backgroundExecutor.execute {
            if (gestureHelper.isClosed()) {
                gestureHelper.setupGestureRecognizer()
            }
        }
    }

    // Save gesture settings and close gesture recognizer on pause
    override fun onPause() {
        super.onPause()
        saveGestureSettings()
        closeGestureRecognizer()
    }

    // Save current gesture recognizer settings to ViewModel
    private fun saveGestureSettings() {
        if (this::gestureHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureHelper.detectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureHelper.trackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureHelper.currentDelegate)
        }
    }

    private fun closeGestureRecognizer() {
        if (this::gestureHelper.isInitialized) {
            backgroundExecutor.execute {
                gestureHelper.clearGestureRecognizer()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseResources()
        _cameraBinding = null
    }

    private fun releaseResources() {
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _cameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return cameraBinding.root
    }


    // Setup UI and initialize necessary components after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initializeBackgroundExecutor()
        prepareCameraSetup()
        initializeGestureRecognizerHelper()
        setupUIControls()
    }

    private fun setupRecyclerView() {
        with(cameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gestureAdapter
        }
    }

    private fun initializeBackgroundExecutor() {
        backgroundExecutor = Executors.newSingleThreadExecutor()
    }

    // Prepare the camera setup by setting up the camera after the view is ready
    private fun prepareCameraSetup() {
        cameraBinding.viewFinder.post {
            setUpCamera()
        }
    }

    private fun initializeGestureRecognizerHelper() {
        backgroundExecutor.execute {
            createGestureRecognizerHelper()
        }
    }

    // Create a new instance of GestureRecognizerHelper
    private fun createGestureRecognizerHelper() {
        gestureHelper = GestureRecognizerHelper(
            context = requireContext(),
            mode = RunningMode.LIVE_STREAM,
            detectionConfidence = viewModel.currentMinHandDetectionConfidence,
            trackingConfidence = viewModel.currentMinHandTrackingConfidence,
            minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
            currentDelegate = viewModel.currentDelegate,
            gestureListener = this
        )
    }

    private fun setupUIControls() {
        initBottomSheetControls()
    }


    // Initialize controls on the bottom sheet
    private fun initBottomSheetControls() {
        updateThresholdValues()
        setupThresholdAdjustmentButtons()
        setupDelegateSpinner()
    }

    // Update the threshold values displayed in the UI
    private fun updateThresholdValues() {
        with(cameraBinding.bottomSheetLayout) {
            detectionThresholdValue.text = formatConfidence(viewModel.currentMinHandDetectionConfidence)
            trackingThresholdValue.text = formatConfidence(viewModel.currentMinHandTrackingConfidence)
            presenceThresholdValue.text = formatConfidence(viewModel.currentMinHandPresenceConfidence)
        }
    }

    // Format confidence values for display
    private fun formatConfidence(value: Float): String {
        return String.format(Locale.US, "%.2f", value)
    }

    // Setup buttons for adjusting detection, tracking, and presence thresholds
    private fun setupThresholdAdjustmentButtons() {
        with(cameraBinding.bottomSheetLayout) {
            // Setup buttons for adjusting detection threshold
            setupAdjustmentButton(detectionThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.detectionConfidence -= it
            }
            setupAdjustmentButton(detectionThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.detectionConfidence += it
            }

            // Setup buttons for adjusting tracking threshold
            setupAdjustmentButton(trackingThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.trackingConfidence -= it
            }
            setupAdjustmentButton(trackingThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.trackingConfidence += it
            }

            // Setup buttons for adjusting presence threshold
            setupAdjustmentButton(presenceThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.minHandPresenceConfidence -= it
            }
            setupAdjustmentButton(presenceThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.minHandPresenceConfidence += it
            }
        }
    }

    // Configure each threshold adjustment button
    private fun setupAdjustmentButton(
        button: View,
        threshold: Float,
        increment: Float,
        isDecrease: Boolean,
        adjustment: (Float) -> Unit
    ) {
        button.setOnClickListener {
            val currentConfidence = if (isDecrease) {
                minOf(threshold + increment, gestureHelper.detectionConfidence)
            } else {
                maxOf(threshold - increment, gestureHelper.detectionConfidence)
            }
            adjustment(if (isDecrease) -increment else increment)
            updateControlsUi()
        }
    }

    // Setup the delegate selection spinner in the bottom sheet
    private fun setupDelegateSpinner() {
        with(cameraBinding.bottomSheetLayout.spinnerDelegate) {
            setSelection(viewModel.currentDelegate, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    try {
                        gestureHelper.currentDelegate = position
                        updateControlsUi()
                    } catch (e: UninitializedPropertyAccessException) {
                        Log.e(TAG, "GestureRecognizerHelper has not been initialized yet.")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    // Update UI controls based on gesture recognizer settings
    private fun updateControlsUi() {
        cameraBinding.bottomSheetLayout.detectionThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.detectionConfidence
            )
        cameraBinding.bottomSheetLayout.trackingThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.trackingConfidence
            )
        cameraBinding.bottomSheetLayout.presenceThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.minHandPresenceConfidence
            )

        // Reset and setup gesture recognizer after changes
        backgroundExecutor.execute {
            gestureHelper.clearGestureRecognizer()
            gestureHelper.setupGestureRecognizer()
        }
        cameraBinding.overlay.clear()
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                camProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            camProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(camFacing).build()


        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(cameraBinding.viewFinder.display.rotation)
            .build()


        imgAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(cameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        recognizeHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {

            cam = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imgAnalyzer
            )

            preview?.setSurfaceProvider(cameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    // Function to recognize hand gestures in a live camera feed
    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    // Handles configuration changes, such as device orientation change
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Update the target rotation of the image analyzer based on the new configuration
        imgAnalyzer?.targetRotation = cameraBinding.viewFinder.display.rotation
    }

    // Callback function that gets triggered when a hand gesture is recognized
    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        activity?.runOnUiThread {
            // Check if the camera binding is not null before updating the UI
            if (_cameraBinding != null) {
                // Update the UI based on the recognized gestures
                updateResultsDisplay(resultBundle)
                updateInferenceTime(resultBundle)
                updateOverlay(resultBundle)
            }
        }
    }

    // Updates the display with the recognized gesture results
    private fun updateResultsDisplay(resultBundle: GestureRecognizerHelper.ResultBundle) {
        // Extract gesture categories and select the most relevant results
        val gestureCategories = resultBundle.results.first().gestures()
        val results = if (gestureCategories.isNotEmpty()) {
            gestureCategories.first()
        } else {
            emptyList()
        }
        // Refresh the adapter to display the new gesture results
        gestureAdapter.refreshDisplayedResults(results)
    }

    // Updates the inference time display
    private fun updateInferenceTime(resultBundle: GestureRecognizerHelper.ResultBundle) {
        // Format the inference time and update the corresponding UI element
        val formattedTime = String.format("%d ms", resultBundle.inferenceTime)
        cameraBinding.bottomSheetLayout.inferenceTimeVal.text = formattedTime
    }

    // Updates the overlay with the gesture recognition results
    private fun updateOverlay(resultBundle: GestureRecognizerHelper.ResultBundle) {
        // Set the results in the overlay and invalidate it to redraw
        cameraBinding.overlay.setResults(
            resultBundle.results.first(),
            resultBundle.imageHeight,
            resultBundle.imageWidth,
            RunningMode.LIVE_STREAM
        )
        cameraBinding.overlay.invalidate()
    }

    // Error handling function
    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            // Clear any displayed results in case of an error
            gestureAdapter.refreshDisplayedResults(emptyList())

            // Handle specific error codes, e.g., GPU-related errors
            if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                // Change the processing delegate to CPU in case of GPU errors
                cameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    GestureRecognizerHelper.DELEGATE_CPU, false
                )
            }
        }
    }
}
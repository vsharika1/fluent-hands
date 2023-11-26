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
package com.google.mediapipe.examples.gesturerecognizer.fragment

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
import com.google.mediapipe.examples.gesturerecognizer.GestureRecognizerHelper
import com.google.mediapipe.examples.gesturerecognizer.MainViewModel
import com.google.mediapipe.examples.gesturerecognizer.R
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "Hand gesture recognizer"
    }

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

    private fun checkPermissions() {
        if (!PermissionsFragment.checkCameraPermission(requireContext())) {
            navigateToPermissions()
        }
    }

    private fun navigateToPermissions() {
        Navigation.findNavController(
            requireActivity(), R.id.fragment_container
        ).navigate(R.id.action_camera_to_permissions)
    }

    private fun restartGestureRecognizer() {
        backgroundExecutor.execute {
            if (gestureHelper.isClosed()) {
                gestureHelper.setupGestureRecognizer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveGestureSettings()
        closeGestureRecognizer()
    }

    private fun saveGestureSettings() {
        if (this::gestureHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureHelper.minHandTrackingConfidence)
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

    private fun createGestureRecognizerHelper() {
        gestureHelper = GestureRecognizerHelper(
            context = requireContext(),
            runningMode = RunningMode.LIVE_STREAM,
            minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
            minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
            minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
            currentDelegate = viewModel.currentDelegate,
            gestureRecognizerListener = this
        )
    }

    private fun setupUIControls() {
        initBottomSheetControls()
    }


    private fun initBottomSheetControls() {
        updateThresholdValues()
        setupThresholdAdjustmentButtons()
        setupDelegateSpinner()
    }

    private fun updateThresholdValues() {
        with(cameraBinding.bottomSheetLayout) {
            detectionThresholdValue.text = formatConfidence(viewModel.currentMinHandDetectionConfidence)
            trackingThresholdValue.text = formatConfidence(viewModel.currentMinHandTrackingConfidence)
            presenceThresholdValue.text = formatConfidence(viewModel.currentMinHandPresenceConfidence)
        }
    }

    private fun formatConfidence(value: Float): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun setupThresholdAdjustmentButtons() {
        with(cameraBinding.bottomSheetLayout) {
            setupAdjustmentButton(detectionThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.minHandDetectionConfidence -= it
            }
            setupAdjustmentButton(detectionThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.minHandDetectionConfidence += it
            }
            setupAdjustmentButton(trackingThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.minHandTrackingConfidence -= it
            }
            setupAdjustmentButton(trackingThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.minHandTrackingConfidence += it
            }
            setupAdjustmentButton(presenceThresholdMinus, 0.2f, 0.1f, true) {
                gestureHelper.minHandPresenceConfidence -= it
            }
            setupAdjustmentButton(presenceThresholdPlus, 0.8f, 0.1f, false) {
                gestureHelper.minHandPresenceConfidence += it
            }
        }
    }

    private fun setupAdjustmentButton(
        button: View,
        threshold: Float,
        increment: Float,
        isDecrease: Boolean,
        adjustment: (Float) -> Unit
    ) {
        button.setOnClickListener {
            val currentConfidence = if (isDecrease) {
                minOf(threshold + increment, gestureHelper.minHandDetectionConfidence)
            } else {
                maxOf(threshold - increment, gestureHelper.minHandDetectionConfidence)
            }
            adjustment(if (isDecrease) -increment else increment)
            updateControlsUi()
        }
    }

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


    // Update the values displayed in the bottom sheet. Reset recognition helper.
    private fun updateControlsUi() {
        cameraBinding.bottomSheetLayout.detectionThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.minHandDetectionConfidence
            )
        cameraBinding.bottomSheetLayout.trackingThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.minHandTrackingConfidence
            )
        cameraBinding.bottomSheetLayout.presenceThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureHelper.minHandPresenceConfidence
            )

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

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imgAnalyzer?.targetRotation = cameraBinding.viewFinder.display.rotation
    }

    // Update UI after a hand gesture has been recognized
    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_cameraBinding != null) {
                updateResultsDisplay(resultBundle)
                updateInferenceTime(resultBundle)
                updateOverlay(resultBundle)
            }
        }
    }

    private fun updateResultsDisplay(resultBundle: GestureRecognizerHelper.ResultBundle) {
        val gestureCategories = resultBundle.results.first().gestures()
        val results = if (gestureCategories.isNotEmpty()) {
            gestureCategories.first()
        } else {
            emptyList()
        }
        gestureAdapter.refreshDisplayedResults(results)
    }

    private fun updateInferenceTime(resultBundle: GestureRecognizerHelper.ResultBundle) {
        val formattedTime = String.format("%d ms", resultBundle.inferenceTime)
        cameraBinding.bottomSheetLayout.inferenceTimeVal.text = formattedTime
    }

    private fun updateOverlay(resultBundle: GestureRecognizerHelper.ResultBundle) {
        cameraBinding.overlay.setResults(
            resultBundle.results.first(),
            resultBundle.inputImageHeight,
            resultBundle.inputImageWidth,
            RunningMode.LIVE_STREAM
        )
        cameraBinding.overlay.invalidate()
    }


    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureAdapter.refreshDisplayedResults(emptyList())

            if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                cameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    GestureRecognizerHelper.DELEGATE_CPU, false
                )
            }
        }
    }
}
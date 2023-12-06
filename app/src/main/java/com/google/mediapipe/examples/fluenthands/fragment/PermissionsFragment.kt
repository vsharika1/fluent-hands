package com.google.mediapipe.examples.fluenthands.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.mediapipe.examples.fluenthands.R

class PermissionsFragment : Fragment() {

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            processCameraPermissionResult(isGranted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if camera permissions granted open camera
        if (cameraPermissionIsGranted()) {
            goToCameraFragment()
        } else {
            requestCameraPermission() // else request permissions
        }
    }

    // Checks if camera permission is granted
    private fun cameraPermissionIsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Requests the camera permission
    private fun requestCameraPermission() {
        cameraPermissionRequest.launch(Manifest.permission.CAMERA)
    }

    // Processes the result of the camera permission request
    private fun processCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            displayToast("Camera Permission Granted")
            goToCameraFragment()// Navigate to camera fragment if permission is granted
        } else {
            displayToast("Camera Permission Denied")
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Navigates to the camera fragment
    private fun goToCameraFragment() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_permissions_to_camera)
        }
    }

    companion object {
        fun checkCameraPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
}

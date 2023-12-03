package com.google.mediapipe.examples.fluenthands

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity: ComponentActivity() {
    private lateinit var profilePhoto: ImageView
    private lateinit var changePhotoButton: Button
    private lateinit var name: EditText
//    private lateinit var email: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePhoto = findViewById(R.id.profilePhoto)
        changePhotoButton = findViewById(R.id.buttonChangePhoto)
        name = findViewById(R.id.editName)
//        email = findViewById(R.id.editEmail)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        changePhotoButton.setOnClickListener {
            changePhoto()
        }
        saveButton.setOnClickListener() {
            saveInputs()
        }
        cancelButton.setOnClickListener() {
            finish()
        }

    }

    private fun changePhoto() {
        val cameraIntent = Intent(this, CameraActivity::class.java)
        startActivity(cameraIntent)
    }

    private fun saveInputs() {
        val updatedName = name.text.toString()
//        val updatedEmail = email.text.toString()

        // Get the current user from FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Update the display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(updatedName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { profileUpdateTask ->
                                    Toast.makeText(
                                        this,
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to update profile",
                            Toast.LENGTH_SHORT
                        ).show()
        }
        finish()
        }
}

package com.google.mediapipe.examples.gesturerecognizer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import androidx.activity.ComponentActivity

class ProfileActivity: ComponentActivity() {
    private lateinit var profilePhoto: ImageView
    private lateinit var changePhotoButton: Button
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePhoto = findViewById(R.id.profilePhoto)
        changePhotoButton = findViewById(R.id.buttonChangePhoto)
        name = findViewById(R.id.editName)
        email = findViewById(R.id.editEmail)
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
        TODO("Store Values in DATABASE")
    }

}
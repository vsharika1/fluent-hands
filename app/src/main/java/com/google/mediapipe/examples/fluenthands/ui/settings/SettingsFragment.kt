package com.google.mediapipe.examples.fluenthands.ui.settings

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.mediapipe.examples.fluenthands.ProfileActivity
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.SignInActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SettingsFragment : Fragment() {
    private lateinit var helloName: TextView
    private lateinit var name: TextView
    private lateinit var userPicture: ImageView
    private lateinit var email: TextView
    private lateinit var userProfile: Button
    private lateinit var accountCreated: TextView
    private lateinit var logOutButton: Button
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var difficultySeekBar: SeekBar

    private lateinit var creditsButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_settings2, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        difficultySeekBar = view.findViewById(R.id.difficultySeekBar)

        difficultySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Play sound effect
                if (fromUser) {
                    playSoundEffect()
                }
                // Update SharedPreferences with the selected difficulty
                Log.d("SeekBar", "Progress changed: $progress")
                saveDifficulty(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })

//        Get xml items for udpating UI
        helloName = view.findViewById(R.id.helloSettings)
        userPicture = view.findViewById(R.id.profilePhoto)
        name = view.findViewById(R.id.settingsName)
        email = view.findViewById(R.id.settingsEmail)
        userProfile = view.findViewById(R.id.userProfileButton)
        accountCreated = view.findViewById(R.id.dateCreated)
        logOutButton = view.findViewById(R.id.logoutButton)
        logOutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

        fetchData()

        userProfile.setOnClickListener{
            val activity = requireActivity()
            val intent: Intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        creditsButton = view.findViewById(R.id.creditsButton)
        creditsButton.setOnClickListener {
            openCreditsDialog()
        }

        return view
    }
    private fun playSoundEffect() {
        mediaPlayer?.release()  // Release any previous MediaPlayer
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.sound) // Replace 'sound_effect' with your actual sound file's name
        mediaPlayer?.start()  // Play sound

        // Optionally set OnCompletionListener if you want to do something after the sound finishes playing
        mediaPlayer?.setOnCompletionListener {
            // Release the MediaPlayer once the sound has finished playing
            it.release()
        }
    }
    override fun onResume() {
        super.onResume()
        fetchData()
    }

    companion object {
        private const val SHARED_PREFS_FILE = "shared_preferences"
        private const val DIFFICULTY_KEY = "difficulty"
    }

    private fun saveDifficulty(progress: Int) {

        Log.d("SeekBar", "Saved Difficulty level changed to: $progress")
        val sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(DIFFICULTY_KEY, progress)
        editor.apply()
    }

    private fun fetchData() {

//        Get data from DB then set values
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {
            var key: String =  user?.uid.toString()
            val sharedPreferences = requireActivity().getSharedPreferences(key, MODE_PRIVATE)
            userPicture.setImageURI(Uri.parse(sharedPreferences.getString("imgUri", "")))
            var getName = user.displayName
            var getEmail = user.email
            var getAccountCreated = getAccountCreationDate(user)

            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    // Update UI with the refreshed user information
                    helloName.text = "Hello ${user.displayName}..."
                    name.text = "Name: ${user.displayName}"
                    email.text = "Email: $getEmail"
                }
            }
//        userPicture.setImageURI()
            email.text = "Email: $getEmail"
            accountCreated.text = "Accounted Created: $getAccountCreated"
            }
        }

    private fun getAccountCreationDate(user: FirebaseUser): String {
        val creationTimestamp = user.metadata?.creationTimestamp ?: return "N/A"

        val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm z", Locale.getDefault())
        val creationDate = Date(creationTimestamp)
        return sdf.format(creationDate)
    }

    private fun openCreditsDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Credits")
        alertDialog.setMessage("This application has been brought to fruition by Group 18, comprising Archit V., Daniel K., Janit K., Varun R., and Vishavjit H.\n\n" +
                "We extend our special gratitude to GitHub user AdamMod13, whose sign language recognition dataset has been instrumental and served as an inspiration for this project.\n\n" +
                "Our sincere thanks to MediaPipe for permitting the use of their gesture recognition codebase and framework, which forms the foundation of our Android application.");

        alertDialog.setPositiveButton("Close") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // Release the MediaPlayer when the Fragment is destroyed
        mediaPlayer = null
    }
}
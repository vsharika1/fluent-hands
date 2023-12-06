package com.google.mediapipe.examples.fluenthands

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.fluenthands.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)  //Initiate firebase
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        playLoginSuccessSound()
                        val intent = Intent(this, BottomNavActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Incorrect password/email."+"\n"+"Please check credentials and try again!", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }
    private fun playLoginSuccessSound() {
        // Use try-catch to handle any exceptions
        try {
            val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.login_success)
            mediaPlayer.start() // no need to call prepare(); create() does that for you
            mediaPlayer.setOnCompletionListener {
                it.release() // Release the MediaPlayer when the sound has finished playing
            }
        } catch (e: Exception) {
            // Handle exceptions such as no resource found or IO issues
            e.printStackTrace()
        }
    }
    override fun onStart() {
        super.onStart()

        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, BottomNavActivity::class.java)
            startActivity(intent)
        }
    }
}
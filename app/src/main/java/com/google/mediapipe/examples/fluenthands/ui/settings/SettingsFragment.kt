package com.google.mediapipe.examples.fluenthands.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var averageScore: TextView
    private lateinit var userProfile: Button
    private lateinit var accountCreated: TextView
    private lateinit var logOutButton: Button

    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val PROFILE_ACTIVITY_REQUEST_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_settings2, container, false)

        firebaseAuth = FirebaseAuth.getInstance()


//        Get xml items for udpating UI
        helloName = view.findViewById(R.id.helloSettings)
        userPicture = view.findViewById(R.id.profilePhoto)
        name = view.findViewById(R.id.settingsName)
        email = view.findViewById(R.id.settingsEmail)
        averageScore = view.findViewById(R.id.settingsScore)
        userProfile = view.findViewById(R.id.userProfileButton)
        accountCreated = view.findViewById(R.id.dateCreated)
        logOutButton = view.findViewById(R.id.logoutButton)
        logOutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

        userProfile.setOnClickListener{
            val activity = requireActivity()
            val intent: Intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        fetchData()

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }
    private fun fetchData() {
//        Get data from DB then set values
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {
            var getName = user.displayName
            var getEmail = user.email
            var getAverageScore = 80 // TO-DO
            var getAccountCreated = getAccountCreationDate(user)

            helloName.text = "Hello $getName..."
//        userPicture.setImageURI()
            name.text = "Name: $getName"
            email.text = "Email: $getEmail"
            averageScore.text = "Average Score: $getAverageScore%"
            accountCreated.text = "Accounted Created: $getAccountCreated"
            }
        }

    private fun getAccountCreationDate(user: FirebaseUser): String {
        val creationTimestamp = user.metadata?.creationTimestamp ?: return "N/A"

        val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm z", Locale.getDefault())
        val creationDate = Date(creationTimestamp)
        return sdf.format(creationDate)
    }



}
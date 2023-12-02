package com.google.mediapipe.examples.gesturerecognizer.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.mediapipe.examples.gesturerecognizer.ProfileActivity
import com.google.mediapipe.examples.gesturerecognizer.R

class SettingsFragment : Fragment() {
    private lateinit var helloName: TextView
    private lateinit var name: TextView
    private lateinit var userPicture: ImageView
    private lateinit var email: TextView
    private lateinit var averageScore: TextView
    private lateinit var userProfile: Button
    private lateinit var accountCreated: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_settings2, container, false)

//        INSERT FIREBASE CODE TO FETCH DATA
//        **
        //**
//        **

//        Get xml items for udpating UI
        helloName = view.findViewById(R.id.helloSettings)
        userPicture = view.findViewById(R.id.profilePhoto)
        name = view.findViewById(R.id.settingsName)
        email = view.findViewById(R.id.settingsEmail)
        averageScore = view.findViewById(R.id.settingsScore)
        userProfile = view.findViewById(R.id.userProfileButton)
        accountCreated = view.findViewById(R.id.dateCreated)

        userProfile.setOnClickListener{
            val activity = requireActivity()
            val intent: Intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        fetchData()


        return view
    }

    private fun fetchData() {
//        Get data from DB then set values
        var getName = "Daniel"
        var getEmail = "daniel@sfu.ca"
        var getAverageScore = 80
        var getAccountCreated = "Nov 22, 2023 - 21:45 pst"

        helloName.text = "Hello $getName..."
//        userPicture.setImageURI()
        name.text = "Name: $getName"
        email.text = "Email: $getEmail"
        averageScore.text = "Average Score: $getAverageScore%"
        accountCreated.text = "Accounted Created: $getAccountCreated"
    }
}
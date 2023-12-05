package com.google.mediapipe.examples.fluenthands.ui.home

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.mediapipe.examples.fluenthands.LearningPage
import com.google.mediapipe.examples.fluenthands.MainActivity
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recognizerCard: FrameLayout
    private lateinit var learningCard: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playLoginSuccessSound()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        recognizerCard = binding.card1
        recognizerCard.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }
        learningCard = binding.card3
        learningCard.setOnClickListener {
            val intent = Intent(requireActivity(), LearningPage::class.java)
            startActivity(intent)
        }

        return binding.root
    }
    private fun playLoginSuccessSound() {
        // Use try-catch to handle any exceptions
        try {
            val mediaPlayer: MediaPlayer = MediaPlayer.create(requireContext(), R.raw.login_success)
            mediaPlayer.start() // no need to call prepare(); create() does that for you
            mediaPlayer.setOnCompletionListener {
                it.release() // Release the MediaPlayer when the sound has finished playing
            }
        } catch (e: Exception) {
            // Handle exceptions such as no resource found or IO issues
            e.printStackTrace()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
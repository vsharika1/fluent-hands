package com.google.mediapipe.examples.fluenthands.ui.help

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupCards()

        return root
    }

    private fun setupCards() {
        binding.card1.setOnClickListener { showAlertDialog(1) }
        binding.card2.setOnClickListener { showAlertDialog(2) }
        binding.card3.setOnClickListener { showAlertDialog(3) }
        binding.card4.setOnClickListener { showAlertDialog(4) }
        binding.card5.setOnClickListener { showAlertDialog(5) }
    }

    private fun showAlertDialog(cardNumber: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.dialog_layout, null)
        builder.setView(customLayout)

        // Set the image and text based on the cardNumber
        val imageView: ImageView = customLayout.findViewById(R.id.dialogImage)
        val textView: TextView = customLayout.findViewById(R.id.dialogText)
        when (cardNumber) {
            1 -> {
                imageView.setImageResource(R.drawable.quizpage)
                val formattedText = """
                    <b>Gesture Recognition:</b><br>
                    • At the top of the page, a letter or word is displayed. This is what the user is required to gesture using their hands. The application will recognize this gesture through the camera view.<br><br>
    
                    <b>Response Submission:</b><br>
                    • After gesturing, the user has two options:<br>
                    • If the gesture is correct, the user can confirm by clicking the 'Yes' button.<br>
                    • If the user wants to retry, they can click the 'No' button to make another attempt.<br><br>
    
                    <b>Gesture Words in Sequence:</b><br>
                    • In cases where the quiz requires gesturing a word, the user can perform the gestures for the word sequentially. Each correctly gestured letter will appear below the camera view.<br><br>
    
                    <b>Editing the Answer:</b><br>
                    • If the user needs to add a space between letters, they can use the 'Space' button.<br>
                    • To correct any mistakes, the user can remove letters by clicking the 'Backspace' button.<br><br>
    
                    <b>Submitting the Quiz:</b><br>
                    • Once the user is satisfied with their answer, they can submit their quiz by pressing the 'Submit' button.<br>
                    """.trimIndent()

                textView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            2 -> {
                imageView.setImageResource(R.drawable.learningpage)
                val formattedText = """
                    <b>Visual Learning Aid:</b><br>
                    • At the center of the screen, an illustrated hand gesture is displayed, representing a letter from the alphabet. The corresponding letter is shown below the image for reference.<br><br>
    
                    <b>Interactive Navigation:</b><br>
                    • To view different gestures, use the forward and backward arrows at the bottom of the gesture image to move through the alphabet.<br>
                    • The display updates with a new gesture and letter as you navigate.<br><br>
    
                    <b>Hands-On Practice:</b><br>
                    • Use the camera view to practice replicating the gesture shown in the illustration with your own hand.<br><br>
    
                    <b>Progression:</b><br>
                    • When comfortable with the current letter's gesture, proceed to the next by clicking the forward arrow.<br>
                    • To review a previous letter, click the backward arrow.<br>
                    <br>
                    This step-by-step approach allows you to learn the hand gestures for the entire alphabet at your own pace, with visual aids and real-time practice.
                    """.trimIndent()

                textView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }

            3 -> {
                imageView.setImageResource(R.drawable.results)
                val formattedText = """
                    <b>Accessing the Results Page:</b><br>
                    • This page can typically be reached using the bottom navigation bar.<br><br>
    
                    <b>Understanding Your Scores:</b><br>
                    • On this page, users will see a list or summary of the quizzes they have taken. Each entry includes the name of the quiz and the score received.<br><br>
    
                    <b>Score Details:</b><br>
                    • The score may be presented as a total number of points earned.<br><br>
    
                    <b>Navigation:</b><br>
                    • Users can scroll through their history of quizzes to review their progress over time.<br><br>
    
                    By regularly checking the Results Page, users can track their learning journey, celebrate their successes, and identify gestures that might require further practice.
                    """.trimIndent()
                textView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }

            4 -> {
                imageView.setImageResource(R.drawable.profilepage)
                val formattedText = """
                <b>User’s Profile Photo:</b><br>
                • At the top, your profile photo is displayed.<br><br>

                <b>User Info:</b><br>
                • Right below your profile photo, you'll find your name, email address, and average quiz score. This information provides a quick snapshot of your identity and performance in the app.<br><br>

                <b>Update Profile Button:</b><br>
                • Tap on the 'Update User Profile' button to navigate to a page where you can change your profile photo and name.<br><br>

                <b>Set Difficulty Slider:</b><br>
                • Adjust the slider to set the difficulty level for your quizzes. You can choose from 'Easy', 'Medium', or 'Hard' to match the quizzes with your comfort level.<br><br>

                <b>Account Creation Date:</b><br>
                • Just below the difficulty slider, the date when you created your account is shown. This helps you keep track of how long you've been improving your skills with us.<br><br>

                <b>Log Out Button:</b><br>
                • When you're ready to leave, tap the 'Log Out' button to securely exit your account.<br><br>

                <b>Show Credits Button:</b><br>
                • The 'Credits' button takes you to a dialog acknowledging the creators of the application. It's a place to learn about the team and the sources that made this app possible.<br><br>
                """.trimIndent()
                textView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }

            5 -> {
                imageView.setImageResource(R.drawable.updateprofilepage)
                val formattedText = """
                    <b>Update Your Profile:</b><br>
                    • This is where you can personalize your account details.<br><br>

                    <b>Changing Your Profile Picture:</b><br>
                    • Displayed at the top is your current profile picture. To update it, tap the 'Change' button, which will open up your camera. Snap a new photo to set as your profile picture.<br><br>

                    <b>Editing Your Name:</b><br>
                    • Below your profile picture is a text field labeled 'Your Name.'. If you wish to change the name associated with your profile, simply enter the new name here.<br><br>

                    <b>Saving Your Changes:</b><br>
                    • After updating your profile picture and/or name, click the 'Save' button to apply the changes. If you've changed your mind or entered something by mistake, hit the 'Cancel' button to disregard any changes.<br><br>

                    <p>Both 'Save' and 'Cancel' buttons will redirect you back to the Profile Page once clicked, allowing you to continue using the app with your updated details or to resume without changes.</p>
                """.trimIndent()
                textView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
        }

        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
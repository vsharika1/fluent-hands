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
package com.google.mediapipe.examples.fluenthands

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.fluenthands.databinding.ActivityMainBinding
import com.google.mediapipe.examples.fluenthands.logic.ContextHolder

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var backButton: Button

    private var totalWords = 10 // Total number of words to generate
    private var currentWordCount = 0 // Current word count
    private var userScore = 0 // User's score

    // Array containing alphabets from a to z
    private val easy = ('a'..'z').toList().toCharArray()

    // Array containing a list of 3-letter words without the alphabet 'v'
    private val medium = arrayOf("cat", "dog", "bat", "hat", "run", "fun", "sun", "car", "bar", "jar", "pen", "hen", "fox", "box", "cup", "rug", "mug", "bug", "big", "dig")

    // Array containing a list of 5-letter words without the alphabet 'v'
    private val difficult = arrayOf("house", "mouse", "apple", "happy", "cloud", "table", "chair", "beach", "earth", "river", "mount", "smile", "grape", "melon", "snake", "plane", "ocean", "knife", "honey", "music")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

        val difficulty = getSavedDifficulty()
        displayRandomWord(difficulty)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.yesButton).setOnClickListener {
            yesButtonClick()
        }

        findViewById<ImageButton>(R.id.noButton).setOnClickListener {
            noButtonClick()
        }
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD
    }

    companion object {
        private const val SHARED_PREFS_FILE = "shared_preferences"
        private const val DIFFICULTY_KEY = "difficulty"
    }

    private fun getSavedDifficulty(): Difficulty {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE)
        val savedDifficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 0)

        Log.d("MainActivity", "Saved Difficulty level: $savedDifficulty")
        return getDifficulty(savedDifficulty)
    }

    private fun getDifficulty(progress: Int): Difficulty {
        return when (progress) {
            0 -> Difficulty.EASY
            1 -> Difficulty.MEDIUM
            2 -> Difficulty.HARD
            else -> Difficulty.EASY
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun getRandomWord(difficulty: Difficulty): String {
        return when (difficulty) {
            Difficulty.EASY -> {
                // Return a random letter from the alphabets array
                easy.random().toString()
            }
            Difficulty.MEDIUM -> {
                // Return a random 3-letter word without 'v' from the words3LetterWithoutV array
                medium.random()
            }
            Difficulty.HARD -> {
                // Return a random 5-letter word without 'v' from the words5LetterWithoutV array
                difficult.random()
            }
        }
    }

    fun deletebuttonClick(view: View?) {
        val textLabel = findViewById<TextView>(R.id.textLabel)
        val currentText = textLabel.text.toString()

        if (currentText.isNotEmpty()) {
            val updatedText = currentText.substring(0, currentText.length - 1)
            textLabel.text = updatedText
            ContextHolder.currentWord = updatedText
        }
    }

    fun addSpacebuttonClick(view: View?) {
        val textLabel = findViewById<TextView>(R.id.textLabel)
        textLabel.text = textLabel.text.toString() + " "
        ContextHolder.currentWord = textLabel.text.toString() + " "
    }

    fun yesButtonClick(){
        // Get the current word displayed
        val currentWord = findViewById<TextView>(R.id.wordTextView).text.toString()

        // Check if the entered word matches the current random word
        if (currentWord.equals(ContextHolder.currentWord, ignoreCase = true)) {
            // Award points
            awardPoints(10)

            currentWordCount++

            if (currentWordCount<totalWords){
                // Display a new random word
                val difficulty = getSavedDifficulty()
                displayRandomWord(difficulty)
            } else {
                Toast.makeText(this, "You've completed $totalWords words!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For example, decrement the score or show a hint
            // Here, we're just displaying a toast message
            Toast.makeText(this,"Incorrect Answer! Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    fun noButtonClick(){
        val difficulty = getSavedDifficulty()

        currentWordCount++

        if(currentWordCount < totalWords){
            displayRandomWord(difficulty)
        } else {
            Toast.makeText(this, "You've completed $totalWords words!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun awardPoints(points: Int) {
        // Implement your scoring logic here
        // For example, update the user's score or perform other actions
        // In this example, we're just displaying a toast message
        userScore += points
        Toast.makeText(this,"You earned $points points!", Toast.LENGTH_SHORT).show()
    }

    private fun onSubmit(){
        val timestamp = System.currentTimeMillis()
        val difficulty = getSavedDifficulty()
    }

    private fun displayRandomWord(difficulty: Difficulty) {
        val randomWord = getRandomWord(difficulty)
        findViewById<TextView>(R.id.wordTextView).text = randomWord
    }
}

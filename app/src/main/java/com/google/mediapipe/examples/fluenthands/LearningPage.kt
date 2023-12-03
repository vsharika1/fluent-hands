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

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.fluenthands.databinding.ActivityLearningPageBinding
import com.google.mediapipe.examples.fluenthands.logic.ContextHolder

class LearningPage : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityLearningPageBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var backButton: Button
    private val letterDrawables = arrayOf(
        R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
        R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j,
        R.drawable.k, R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o,
        R.drawable.p, R.drawable.q, R.drawable.r, R.drawable.s, R.drawable.t,
        R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
        R.drawable.z
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityLearningPageBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }
        setImage(0)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
    fun setImage(index: Int) {
        val imageView = findViewById<ImageView>(R.id.camera_overlay)
        if (index in letterDrawables.indices) {
            imageView.setImageResource(letterDrawables[index])
        } else {
            imageView.setImageResource(R.drawable.fluenthands) // Default or fallback drawable
        }
    }


    override fun onBackPressed() {
        finish()
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
}

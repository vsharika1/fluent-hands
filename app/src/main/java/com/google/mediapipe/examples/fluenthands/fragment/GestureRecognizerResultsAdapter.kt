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
package com.google.mediapipe.examples.fluenthands.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.fluenthands.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.examples.fluenthands.logic.ContextHolder
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min

class GestureRecognizerResultsAdapter : RecyclerView.Adapter<GestureRecognizerResultsAdapter.ResultViewHolder>() {
    companion object {
        private const val DEFAULT_DISPLAY_VALUE = "--"
    }

    private var gestureCategories: MutableList<Category?> = mutableListOf()
    private var maxDisplayItemCount: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun refreshDisplayedResults(newCategories: List<Category>?) {
        resetGestureCategories()
        if (newCategories != null) {
            sortAndPopulateCategories(newCategories)
            notifyDataSetChanged()
        }
    }

    private fun resetGestureCategories() {
        gestureCategories = MutableList(maxDisplayItemCount) { null }
    }

    private fun sortAndPopulateCategories(categories: List<Category>) {
        val sortedCategories = categories.sortedByDescending { it.score() }
        val itemsToDisplay = min(sortedCategories.size, maxDisplayItemCount)
        for (i in 0 until itemsToDisplay) {
            gestureCategories[i] = sortedCategories[i]
        }
    }

    fun adjustDisplayItemCount(newSize: Int) {
        maxDisplayItemCount = newSize
        initializeCategoryList()
    }

    private fun initializeCategoryList() {
        gestureCategories = MutableList(maxDisplayItemCount) { null }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return instantiateViewHolderFrom(parent)
    }

    private fun instantiateViewHolderFrom(parent: ViewGroup): ResultViewHolder {
        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        associateViewHolderWithData(holder, position)
    }

    private fun associateViewHolderWithData(holder: ResultViewHolder, position: Int) {
        gestureCategories[position]?.let { category ->
            holder.applyCategoryData(category.categoryName(), category.score())
        }
    }

    override fun getItemCount(): Int = gestureCategories.size

    inner class ResultViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun applyCategoryData(label: String?, score: Float?) {
            displayLabel(label)
            displayScore(score)
            displayCurrentText()
        }

        private fun displayLabel(label: String?) {
            binding.tvLabel.text = label ?: DEFAULT_DISPLAY_VALUE
        }

        private fun displayScore(score: Float?) {
            binding.tvScore.text = score?.let { formatScore(it) } ?: DEFAULT_DISPLAY_VALUE
        }

        private fun formatScore(score: Float): String {
            return String.format(Locale.US, "%.2f", score)
        }

        private fun displayCurrentText() {
            binding.textLabel.text = ContextHolder.currentWord
        }
    }
}


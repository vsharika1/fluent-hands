package com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.impl

import android.util.Log
import com.google.mediapipe.examples.gesturerecognizer.logic.model.GestureWrapper
import com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.DynamicGestureRecognizer


class UpMovementRecognizer : DynamicGestureRecognizer {
    override fun checkHandMovement(gestureList: List<GestureWrapper>, landmarkIndex: Int): Boolean {
        if (gestureList.size < minLength)
            return false

        val movementValues = mutableListOf<Float>()
        var currentIndex = gestureList.size - 1

        while (currentIndex >= 1) {
            val currentMovement = comparePositions(gestureList[currentIndex], gestureList[currentIndex - 1], landmarkIndex)
            Log.i("checkHandMovement", "$currentMovement between indexed $currentIndex and ${currentIndex - 1}")
            movementValues.add(currentMovement)
            currentIndex--
        }

        return movementValues.all { it > movementThreshold }
    }

    private fun comparePositions(
        first: GestureWrapper, second: GestureWrapper, landmarkIndex: Int
    ): Float {
        return second.getLandmarkArray()[landmarkIndex].y() - first.getLandmarkArray()[landmarkIndex].y()
    }
}
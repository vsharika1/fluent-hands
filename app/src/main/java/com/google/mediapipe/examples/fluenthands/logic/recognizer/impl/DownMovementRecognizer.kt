package com.google.mediapipe.examples.fluenthands.logic.recognizer.impl

import android.util.Log
import com.google.mediapipe.examples.fluenthands.logic.model.GestureWrapper
import com.google.mediapipe.examples.fluenthands.logic.recognizer.DynamicGestureRecognizer


class DownMovementRecognizer : DynamicGestureRecognizer {
    override fun checkHandMovement(gestureList: List<GestureWrapper>, landmarkIndex: Int): Boolean {
        if (gestureList.size < minLength)
            return false
        val movementList = mutableListOf<Float>()
        var currentIndex = minLength - 1
        while (currentIndex >= 1) {
            val currentMovement = comparePositions(gestureList[currentIndex], gestureList[currentIndex - 1], landmarkIndex)
            Log.i("checkHandMovement", "$currentMovement between indexed $currentIndex and ${currentIndex - 1}")
            movementList.add(currentMovement)
            currentIndex--
        }
        return movementList.all { it > movementThreshold }
    }


    private fun comparePositions(
        first: GestureWrapper, second: GestureWrapper, landmarkIndex: Int
    ): Float {
        return first.getLandmarkArray()[landmarkIndex].y() - second.getLandmarkArray()[landmarkIndex].y()
    }
}
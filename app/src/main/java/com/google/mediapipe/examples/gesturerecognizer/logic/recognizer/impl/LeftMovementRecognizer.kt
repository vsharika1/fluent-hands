package com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.impl

import android.util.Log
import com.google.mediapipe.examples.gesturerecognizer.logic.model.GestureWrapper
import com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.DynamicGestureRecognizer

class LeftMovementRecognizer : DynamicGestureRecognizer {
    override fun checkHandMovement(gestureList: List<GestureWrapper>, landmarkIndex: Int): Boolean {
        if (gestureList.size < minLength)
            return false
        val movementValues = mutableListOf<Float>()
        var currentIndex = gestureList.size - 1
        while (currentIndex >= 1) {
            val displacement = comparePositions(
                gestureList[currentIndex],
                gestureList[currentIndex - 1],
                landmarkIndex
            )
            Log.i(
                "checkHandMovement",
                "$displacement between indexed $currentIndex and ${currentIndex - 1}"
            )
            movementValues.add(displacement)
            currentIndex--
        }
        return movementValues.all { it > movementThreshold }
    }


    private fun comparePositions(
        first: GestureWrapper, second: GestureWrapper, landmarkIndex: Int
    ): Float {
        return second.getLandmarkArray()[landmarkIndex].x() - first.getLandmarkArray()[landmarkIndex].x()
    }
}
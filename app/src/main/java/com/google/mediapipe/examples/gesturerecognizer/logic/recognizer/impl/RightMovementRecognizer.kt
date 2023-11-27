package com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.impl

import android.util.Log
import com.google.mediapipe.examples.gesturerecognizer.logic.model.GestureWrapper
import com.google.mediapipe.examples.gesturerecognizer.logic.recognizer.DynamicGestureRecognizer

class RightMovementRecognizer : DynamicGestureRecognizer {
    override fun checkHandMovement(gestureList: List<GestureWrapper>, landmarkIndex: Int): Boolean {
        if (gestureList.size < minLength)
            return false

        val movementValues = mutableListOf<Float>()
        var positionIndex = gestureList.size - 1

        while (positionIndex >= 1) {
            val displacement = comparePositions(gestureList[positionIndex], gestureList[positionIndex - 1], landmarkIndex)
            Log.i("checkHandMovement", "$displacement between indexed $positionIndex and ${positionIndex - 1}")
            movementValues.add(displacement)
            positionIndex--
        }

        return movementValues.all { it > movementThreshold }
    }


    private fun comparePositions(
        first: GestureWrapper, second: GestureWrapper, landmarkIndex: Int
    ): Float {
        return first.getLandmarkArray()[landmarkIndex].x() - second.getLandmarkArray()[landmarkIndex].x()
    }
}

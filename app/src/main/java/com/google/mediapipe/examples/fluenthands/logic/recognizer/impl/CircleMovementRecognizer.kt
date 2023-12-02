package com.google.mediapipe.examples.fluenthands.logic.recognizer.impl

import android.util.Log
import com.google.mediapipe.examples.fluenthands.logic.model.GestureWrapper
import com.google.mediapipe.examples.fluenthands.logic.recognizer.DynamicGestureRecognizer

class CircleMovementRecognizer : DynamicGestureRecognizer {
    override fun checkHandMovement(gestureList: List<GestureWrapper>, landmarkIndex: Int): Boolean {
        if (gestureList.size < minLength)
            return false
        val movementValues = mutableListOf<Float>()
        var index = minLength - 1
        while (index >= 1) {
            val displacement = comparePositions(gestureList[index], gestureList[index - 1], landmarkIndex)
            Log.i("checkHandMovement", "$displacement between indexed $index and ${index - 1}")
            movementValues.add(displacement)
            index--
        }
        return movementValues.all { it > movementThreshold }
    }

    private fun comparePositions(
        first: GestureWrapper, second: GestureWrapper, landmarkIndex: Int
    ): Float {
        return first.getLandmarkArray()[landmarkIndex].y() - second.getLandmarkArray()[landmarkIndex].y()
    }

}
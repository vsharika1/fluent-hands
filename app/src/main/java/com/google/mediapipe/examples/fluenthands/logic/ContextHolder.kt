package com.google.mediapipe.examples.fluenthands.logic

import android.util.Log
import com.google.mediapipe.examples.fluenthands.logic.model.GestureWrapper
import com.google.mediapipe.examples.fluenthands.logic.recognizer.impl.DownMovementRecognizer
import com.google.mediapipe.examples.fluenthands.logic.recognizer.impl.LeftMovementRecognizer
import com.google.mediapipe.examples.fluenthands.logic.recognizer.impl.RightMovementRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult


object ContextHolder {
    private const val GESTURE_MAX_CAPACITY = 10
    private const val CONTEXT_HOLDER_TAG = "ContextHolder"

    private val dynamicSignCandidatesMap = mapOf(
        "A" to "Ą",
        "C" to "Ć",
        "D" to "D",
        "E" to "Ę",
        "F" to "F",
        "G" to "G",
        "H" to "H",
        "I" to "J",
        "K" to "K",
        "L" to "Ł",
        "N" to "Ń",
        "O" to "Ó",
        "R" to "RZ",
        "S" to "Ś",
        "Z" to "Ż/Ź"
    )
    private val labelsArray = mutableListOf<String>()
    private val gesturesArray = ArrayDeque<GestureWrapper>()
    var currentWord: String = ""

    fun processGestureResult(result: GestureRecognizerResult) {
        Log.i(CONTEXT_HOLDER_TAG, "$gesturesArray")

        if (result.gestures().isEmpty()) {
            return
        }

        val gesture = GestureWrapper(result.gestures(), result.landmarks())
        addGesture(gesture)

        if (isGestureMatching(gesture).not()) {
            gesturesArray.clear()
            labelsArray.clear()
        }

        when {
            checkIfDynamic(gesture.getCategory()).not() -> {
                Log.i(CONTEXT_HOLDER_TAG, "static sign")
                gesturesArray.clear()
                logAppendingLabel(gesture.getCategory())
            }
            gesturesArray.isNotEmpty() -> {
                Log.i(CONTEXT_HOLDER_TAG, "possible dynamic sign")
                Log.i(CONTEXT_HOLDER_TAG, "current sign matches most")
                recognizeAndMatchGesture(gesture.getCategory())
            }
        }
    }

    private fun appendDynamicLetterToCurrentWord(label: String) {
        logAppendingLabel(label, labelsArray.size, 4)
    }

    private fun logAppendingLabel(label: String, divider: Int = 2, threshold: Int = 10) {
        logLabelInfo(label)

        labelsArray.add(label)

        if (labelsArray.size < threshold) {
            return
        }

        determineAndAppendMostCommonLabel(divider)
    }

    private fun logLabelInfo(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "appending $label")
        Log.i(CONTEXT_HOLDER_TAG, "array $labelsArray")
        Log.i(CONTEXT_HOLDER_TAG, "gestures array $gesturesArray")
    }

    private fun determineAndAppendMostCommonLabel(divider: Int) {
        val frequencyMap = labelsArray.groupingBy { it }.eachCount()
        val appearancesThreshold = labelsArray.size / divider

        frequencyMap.filterValues { it >= appearancesThreshold }
            .maxByOrNull { it.value }?.key
            ?.takeIf { it.isNotBlank() }
            ?.let { appendToCurrentWord(it) }

        labelsArray.clear()
        gesturesArray.clear()
    }

    private fun appendToCurrentWord(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "appending $label ???")
        currentWord += label
    }


    private fun recognizeAndMatchGesture(label: String) {
        logMatchingDynamicGesture(label)
        val operatingArray = determineOperatingArray()

        when (label) {
            "N", "O", "C", "S" -> processGestureWithDownMovement(label, operatingArray)
            "L" -> processGestureWithRightMovement(label, operatingArray)
            "H" -> processGestureForH(label, operatingArray)
            "I" -> processGestureForI(label, operatingArray)
            else -> logAppendingLabel(label, 3, 15)
        }
    }

    private fun logMatchingDynamicGesture(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "matching dynamic gesture\nLetter = $label")
        Log.i(CONTEXT_HOLDER_TAG, "full array: $gesturesArray")
    }

    private fun determineOperatingArray(): List<GestureWrapper> =
        if (gesturesArray.size > 4) gesturesArray.take(4) else gesturesArray

    private fun processGestureWithDownMovement(label: String, operatingArray: List<GestureWrapper>) {
        val movement = DownMovementRecognizer().checkHandMovement(operatingArray)
        if (movement) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord(it) }
        } else {
            logAppendingLabel(label)
        }
        logArraySlice(operatingArray)
    }

    private fun processGestureWithRightMovement(label: String, operatingArray: List<GestureWrapper>) {
        val movement = RightMovementRecognizer().checkHandMovement(operatingArray)
        if (movement) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord(it) }
        } else {
            logAppendingLabel(label)
        }
    }

    private fun processGestureForH(label: String, operatingArray: List<GestureWrapper>) {
        if (DownMovementRecognizer().checkHandMovement(operatingArray)) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord(it) }
        }
    }

    private fun processGestureForI(label: String, operatingArray: List<GestureWrapper>) {
        val downMovement = DownMovementRecognizer().checkHandMovement(operatingArray)
        val leftMovement = LeftMovementRecognizer().checkHandMovement(operatingArray)
        if (downMovement && leftMovement) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord("J") }
        } else {
            logAppendingLabel(label)
        }
    }

    private fun logArraySlice(operatingArray: List<GestureWrapper>) {
        Log.i(CONTEXT_HOLDER_TAG, "slice: $operatingArray")
    }


    private fun isGestureMatching(candidate: GestureWrapper): Boolean {
        val count = gesturesArray.count { it.getCategory() == candidate.getCategory() }
        return count > gesturesArray.size - (GESTURE_MAX_CAPACITY / 4)
    }

    private fun checkIfDynamic(label: String): Boolean {
        return label in dynamicSignCandidatesMap
    }

    private fun addGesture(gesture: GestureWrapper) {
        gesturesArray.addLast(gesture)
        if (gesturesArray.size > GESTURE_MAX_CAPACITY) {
            Log.i(CONTEXT_HOLDER_TAG, "overflow popping")
            gesturesArray.removeFirst()
        }
        if (gesturesArray.none { it.getCategory() == gesture.getCategory() }) {
            gesturesArray.clear()
        }
    }
}
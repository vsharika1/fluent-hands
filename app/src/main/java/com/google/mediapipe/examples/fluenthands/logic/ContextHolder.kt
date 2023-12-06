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

    // Map for dynamic sign language candidates
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

    // Arrays to store gesture labels and their wrappers
    private val labelsArray = mutableListOf<String>()
    private val gesturesArray = ArrayDeque<GestureWrapper>()
    var currentWord: String = ""

    // Processes a new gesture result
    fun processGestureResult(result: GestureRecognizerResult) {
        Log.i(CONTEXT_HOLDER_TAG, "$gesturesArray")

        // Return if no gestures are detected
        if (result.gestures().isEmpty()) {
            return
        }

        // Create a new gesture wrapper and add it to the queue
        val gesture = GestureWrapper(result.gestures(), result.landmarks())
        addGesture(gesture)

        // Clear arrays if the gesture does not match the expected pattern
        if (isGestureMatching(gesture).not()) {
            gesturesArray.clear()
            labelsArray.clear()
        }

        // Process the gesture based on its type (dynamic or static)
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

    // Appends a dynamic letter to the current word based on the provided label
    private fun appendDynamicLetterToCurrentWord(label: String) {
        logAppendingLabel(label, labelsArray.size, 4)
    }

    // Logs information about the label being appended and determines the most common label
    private fun logAppendingLabel(label: String, divider: Int = 2, threshold: Int = 10) {
        logLabelInfo(label)

        labelsArray.add(label)

        // Only process if the labels array has reached a certain threshold
        if (labelsArray.size < threshold) {
            return
        }

        determineAndAppendMostCommonLabel(divider)
    }

    // Logs detailed information about the label being appended
    private fun logLabelInfo(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "appending $label")
        Log.i(CONTEXT_HOLDER_TAG, "array $labelsArray")
        Log.i(CONTEXT_HOLDER_TAG, "gestures array $gesturesArray")
    }

    // Determines and appends the most common label from the labels array
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

    // Appends the determined label to the current word
    private fun appendToCurrentWord(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "appending $label ???")
        currentWord += label
    }


    // Recognizes and processes a gesture based on its label
    private fun recognizeAndMatchGesture(label: String) {
        logMatchingDynamicGesture(label)
        val operatingArray = determineOperatingArray()

        // Processes gestures with specific movements based on their labels
        when (label) {
            "N", "O", "C", "S" -> processGestureWithDownMovement(label, operatingArray)
            "L" -> processGestureWithRightMovement(label, operatingArray)
            "H" -> processGestureForH(label, operatingArray)
            "I" -> processGestureForI(label, operatingArray)
            else -> logAppendingLabel(label, 3, 15)
        }
    }


    // Logs information about matching dynamic gestures
    private fun logMatchingDynamicGesture(label: String) {
        Log.i(CONTEXT_HOLDER_TAG, "matching dynamic gesture\nLetter = $label")
        Log.i(CONTEXT_HOLDER_TAG, "full array: $gesturesArray")
    }

    // Determines the operating array for gesture processing
    private fun determineOperatingArray(): List<GestureWrapper> =
        if (gesturesArray.size > 4) gesturesArray.take(4) else gesturesArray

    // Processes gestures with a down movement
    private fun processGestureWithDownMovement(label: String, operatingArray: List<GestureWrapper>) {
        val movement = DownMovementRecognizer().checkHandMovement(operatingArray)
        if (movement) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord(it) }
        } else {
            logAppendingLabel(label)
        }
        logArraySlice(operatingArray)
    }

    // Processes gestures with a right movement
    private fun processGestureWithRightMovement(label: String, operatingArray: List<GestureWrapper>) {
        val movement = RightMovementRecognizer().checkHandMovement(operatingArray)
        if (movement) {
            dynamicSignCandidatesMap[label]?.let { appendDynamicLetterToCurrentWord(it) }
        } else {
            logAppendingLabel(label)
        }
    }

    // Process gestures for alphabets
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


    // Determines if a gesture matches the expected pattern
    private fun isGestureMatching(candidate: GestureWrapper): Boolean {
        val count = gesturesArray.count { it.getCategory() == candidate.getCategory() }
        return count > gesturesArray.size - (GESTURE_MAX_CAPACITY / 4)
    }

    // Checks if the label corresponds to a dynamic gesture
    private fun checkIfDynamic(label: String): Boolean {
        return label in dynamicSignCandidatesMap
    }

    // Adds a new gesture to the queue and maintains its size
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
package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.exercise.detector.BicepCurlDetector
import com.reptile.gymtracker.exercise.detector.DeadliftDetector
import com.reptile.gymtracker.exercise.detector.ExercisePhaseDetector
import com.reptile.gymtracker.exercise.detector.ExercisePhase
import com.reptile.gymtracker.exercise.detector.LungeDetector
import com.reptile.gymtracker.exercise.detector.PushUpDetector
import com.reptile.gymtracker.exercise.detector.RepCounterResult
import com.reptile.gymtracker.exercise.detector.ShoulderPressDetector
import com.reptile.gymtracker.exercise.detector.SquatDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RepCounterState(
    val exerciseType: ExerciseType,
    val repCount: Int,
    val setNumber: Int,
    val currentPhase: ExercisePhase,
    val confidence: Float,
    val lastRepTimestampMs: Long
)

class RepCounter {
    private val detectors: Map<ExerciseType, ExercisePhaseDetector> = mapOf(
        ExerciseType.SQUAT to SquatDetector(),
        ExerciseType.PUSH_UP to PushUpDetector(),
        ExerciseType.BICEP_CURL to BicepCurlDetector(),
        ExerciseType.SHOULDER_PRESS to ShoulderPressDetector(),
        ExerciseType.DEADLIFT to DeadliftDetector(),
        ExerciseType.LUNGE to LungeDetector()
    )

    private var currentExercise: ExerciseType = ExerciseType.UNKNOWN
    private var setNumber: Int = 1
    private var lastRepTimestampMs: Long = 0L

    private val _stateFlow = MutableStateFlow(
        RepCounterState(ExerciseType.UNKNOWN, 0, 1, ExercisePhase.NEUTRAL, 0f, 0L)
    )
    val stateFlow: StateFlow<RepCounterState> = _stateFlow

    fun processFrame(pose: DetectedPose, detectedExercise: ExerciseType) {
        if (detectedExercise != ExerciseType.UNKNOWN && detectedExercise != currentExercise) {
            currentExercise = detectedExercise
        }

        val detector = detectors[currentExercise]
        if (detector == null) {
            _stateFlow.value = RepCounterState(
                currentExercise, 0, setNumber, ExercisePhase.NEUTRAL, 0f, 0L
            )
            return
        }

        val result: RepCounterResult = detector.processFrame(pose)
        if (result.repJustCompleted) {
            lastRepTimestampMs = pose.timestampMs
        }

        _stateFlow.value = RepCounterState(
            exerciseType = currentExercise,
            repCount = result.repCount,
            setNumber = setNumber,
            currentPhase = result.currentPhase,
            confidence = result.confidence,
            lastRepTimestampMs = lastRepTimestampMs
        )
    }

    fun startNewSet() {
        detectors[currentExercise]?.reset()
        setNumber++
        _stateFlow.value = _stateFlow.value.copy(repCount = 0, setNumber = setNumber)
    }

    fun resetForNewSession() {
        detectors.values.forEach { it.reset() }
        currentExercise = ExerciseType.UNKNOWN
        setNumber = 1
        lastRepTimestampMs = 0L
        _stateFlow.value = RepCounterState(ExerciseType.UNKNOWN, 0, 1, ExercisePhase.NEUTRAL, 0f, 0L)
    }

    fun getCurrentRepCount(): Int = detectors[currentExercise]?.getRepCount() ?: 0
    fun getCurrentSetNumber(): Int = setNumber
    fun getCurrentExercise(): ExerciseType = currentExercise
}

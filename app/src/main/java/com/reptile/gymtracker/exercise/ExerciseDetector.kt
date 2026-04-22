package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.data.model.ExerciseType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseDetector @Inject constructor() {
    private val classifier = ExerciseClassifier()
    val repCounter = RepCounter()
    private var lockedExercise: ExerciseType? = null

    fun processFrame(pose: DetectedPose): RepCounterState {
        val detectedExercise = lockedExercise ?: classifier.classify(pose)
        repCounter.processFrame(pose, detectedExercise)
        return repCounter.stateFlow.value
    }

    fun lockExercise(type: ExerciseType) {
        lockedExercise = type
        repCounter.forceExercise(type)
        classifier.reset()
    }

    fun unlockExercise() {
        lockedExercise = null
        classifier.reset()
    }

    fun isExerciseLocked(): Boolean = lockedExercise != null

    fun adjustRepCount(delta: Int) = repCounter.adjustRepCount(delta)

    fun startNewSet() = repCounter.startNewSet()

    fun resetForNewSession() {
        lockedExercise = null
        classifier.reset()
        repCounter.resetForNewSession()
    }

    fun getCurrentExercise(): ExerciseType = repCounter.getCurrentExercise()
    fun getCurrentRepCount(): Int = repCounter.getCurrentRepCount()
    fun getCurrentSetNumber(): Int = repCounter.getCurrentSetNumber()
}

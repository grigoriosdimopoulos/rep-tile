package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.data.model.ExerciseType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseDetector @Inject constructor() {
    private val classifier = ExerciseClassifier()
    val repCounter = RepCounter()

    fun processFrame(pose: DetectedPose): RepCounterState {
        val detectedExercise = classifier.classify(pose)
        repCounter.processFrame(pose, detectedExercise)
        return repCounter.stateFlow.value
    }

    fun startNewSet() = repCounter.startNewSet()

    fun resetForNewSession() {
        classifier.reset()
        repCounter.resetForNewSession()
    }

    fun getCurrentExercise(): ExerciseType = repCounter.getCurrentExercise()
    fun getCurrentRepCount(): Int = repCounter.getCurrentRepCount()
    fun getCurrentSetNumber(): Int = repCounter.getCurrentSetNumber()
}

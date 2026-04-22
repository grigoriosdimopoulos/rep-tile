package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose

enum class ExercisePhase { NEUTRAL, DOWN, UP, EXTENDED }

data class RepCounterResult(
    val repCount: Int,
    val currentPhase: ExercisePhase,
    val repJustCompleted: Boolean,
    val confidence: Float
)

abstract class ExercisePhaseDetector {
    protected var currentPhase: ExercisePhase = ExercisePhase.NEUTRAL
    protected var repCount: Int = 0
    protected var consecutivePhaseFrames: Int = 0
    protected var lastRepTimestampMs: Long = 0L
    protected val minRepIntervalMs: Long = 500L
    protected val debounceFrames: Int = 3

    abstract fun processFrame(pose: DetectedPose): RepCounterResult

    protected fun tryCountRep(timestampMs: Long): Boolean {
        val now = timestampMs
        if (now - lastRepTimestampMs >= minRepIntervalMs) {
            repCount++
            lastRepTimestampMs = now
            return true
        }
        return false
    }

    fun adjustRepCount(delta: Int) {
        repCount = maxOf(0, repCount + delta)
    }

    fun reset() {
        currentPhase = ExercisePhase.NEUTRAL
        repCount = 0
        consecutivePhaseFrames = 0
        lastRepTimestampMs = 0L
    }

    fun currentRepCount(): Int = repCount
}

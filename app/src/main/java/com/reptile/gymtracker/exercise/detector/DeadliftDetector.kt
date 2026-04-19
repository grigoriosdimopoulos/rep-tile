package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

class DeadliftDetector : ExercisePhaseDetector() {

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val leftShoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
        val leftHip = pose.landmark(LandmarkType.LEFT_HIP)
        val leftKnee = pose.landmark(LandmarkType.LEFT_KNEE)
        val rightShoulder = pose.landmark(LandmarkType.RIGHT_SHOULDER)
        val rightHip = pose.landmark(LandmarkType.RIGHT_HIP)
        val rightKnee = pose.landmark(LandmarkType.RIGHT_KNEE)

        if (leftShoulder == null || leftHip == null || leftKnee == null ||
            rightShoulder == null || rightHip == null || rightKnee == null
        ) {
            return RepCounterResult(repCount, currentPhase, false, 0f)
        }

        val confidence = pose.avgConfidence(
            listOf(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE,
                LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE)
        )
        if (confidence < 0.5f) {
            return RepCounterResult(repCount, currentPhase, false, confidence)
        }

        val leftHipAngle = AngleCalculator.angleDegrees(leftShoulder, leftHip, leftKnee)
        val rightHipAngle = AngleCalculator.angleDegrees(rightShoulder, rightHip, rightKnee)
        val avgHipAngle = (leftHipAngle + rightHipAngle) / 2f

        var repJustCompleted = false

        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                // Hinged forward: hip angle decreases
                if (avgHipAngle < 100f) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                // Stand up: hip extends
                if (avgHipAngle > 160f) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.NEUTRAL
                        consecutivePhaseFrames = 0
                        repJustCompleted = tryCountRep(pose.timestampMs)
                    }
                } else consecutivePhaseFrames = 0
            }
            else -> currentPhase = ExercisePhase.NEUTRAL
        }

        return RepCounterResult(repCount, currentPhase, repJustCompleted, confidence)
    }
}

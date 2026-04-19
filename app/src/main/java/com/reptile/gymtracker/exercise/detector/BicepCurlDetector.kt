package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

class BicepCurlDetector : ExercisePhaseDetector() {

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val leftShoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
        val leftElbow = pose.landmark(LandmarkType.LEFT_ELBOW)
        val leftWrist = pose.landmark(LandmarkType.LEFT_WRIST)
        val rightShoulder = pose.landmark(LandmarkType.RIGHT_SHOULDER)
        val rightElbow = pose.landmark(LandmarkType.RIGHT_ELBOW)
        val rightWrist = pose.landmark(LandmarkType.RIGHT_WRIST)

        if (leftShoulder == null || leftElbow == null || leftWrist == null ||
            rightShoulder == null || rightElbow == null || rightWrist == null
        ) {
            return RepCounterResult(repCount, currentPhase, false, 0f)
        }

        val confidence = pose.avgConfidence(
            listOf(LandmarkType.LEFT_ELBOW, LandmarkType.RIGHT_ELBOW,
                LandmarkType.LEFT_WRIST, LandmarkType.RIGHT_WRIST)
        )
        if (confidence < 0.5f) {
            return RepCounterResult(repCount, currentPhase, false, confidence)
        }

        val leftElbowAngle = AngleCalculator.angleDegrees(leftShoulder, leftElbow, leftWrist)
        val rightElbowAngle = AngleCalculator.angleDegrees(rightShoulder, rightElbow, rightWrist)
        val avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2f

        var repJustCompleted = false

        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                // Down: arm extended
                if (avgElbowAngle > 150f) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                // Curl up: arm flexed
                if (avgElbowAngle < 60f) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.UP
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.UP -> {
                // Return down: count rep
                if (avgElbowAngle > 140f) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
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

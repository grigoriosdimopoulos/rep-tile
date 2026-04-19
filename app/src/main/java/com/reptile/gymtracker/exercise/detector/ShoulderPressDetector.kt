package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

class ShoulderPressDetector : ExercisePhaseDetector() {

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
            listOf(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW,
                LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW)
        )
        if (confidence < 0.5f) {
            return RepCounterResult(repCount, currentPhase, false, confidence)
        }

        val leftElbowAngle = AngleCalculator.angleDegrees(leftShoulder, leftElbow, leftWrist)
        val rightElbowAngle = AngleCalculator.angleDegrees(rightShoulder, rightElbow, rightWrist)
        val avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2f

        // Wrists above shoulders = pressed up
        val wristsAboveShoulders = leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y

        var repJustCompleted = false

        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                // Starting position: elbows bent ~90°, wrists at shoulder level
                if (avgElbowAngle < 110f && !wristsAboveShoulders) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                // Press up: arms extend, wrists above shoulders
                if (avgElbowAngle > 155f && wristsAboveShoulders) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.UP
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.UP -> {
                // Lower back down
                if (avgElbowAngle < 110f && !wristsAboveShoulders) {
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

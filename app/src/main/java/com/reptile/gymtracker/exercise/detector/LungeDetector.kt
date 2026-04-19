package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator
import kotlin.math.abs

class LungeDetector : ExercisePhaseDetector() {

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val leftHip = pose.landmark(LandmarkType.LEFT_HIP)
        val leftKnee = pose.landmark(LandmarkType.LEFT_KNEE)
        val leftAnkle = pose.landmark(LandmarkType.LEFT_ANKLE)
        val rightHip = pose.landmark(LandmarkType.RIGHT_HIP)
        val rightKnee = pose.landmark(LandmarkType.RIGHT_KNEE)
        val rightAnkle = pose.landmark(LandmarkType.RIGHT_ANKLE)

        if (leftHip == null || leftKnee == null || leftAnkle == null ||
            rightHip == null || rightKnee == null || rightAnkle == null
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

        val leftKneeAngle = AngleCalculator.angleDegrees(leftHip, leftKnee, leftAnkle)
        val rightKneeAngle = AngleCalculator.angleDegrees(rightHip, rightKnee, rightAnkle)
        val minKneeAngle = minOf(leftKneeAngle, rightKneeAngle)
        val kneeAngleDiff = abs(leftKneeAngle - rightKneeAngle)
        val isAsymmetric = kneeAngleDiff > 30f

        var repJustCompleted = false

        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                if (minKneeAngle < 100f && isAsymmetric) {
                    consecutivePhaseFrames++
                    if (consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                if (leftKneeAngle > 155f && rightKneeAngle > 155f) {
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

package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

// elbow angle: low = weight at shoulder (start/rest), high = arms extended overhead
// rest = LOW → countOnHigh = false; rep counted when weight returns to shoulder
class ShoulderPressDetector : ExercisePhaseDetector() {

    override val countOnHigh = false

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val lShoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
        val lElbow = pose.landmark(LandmarkType.LEFT_ELBOW)
        val lWrist = pose.landmark(LandmarkType.LEFT_WRIST)
        val rShoulder = pose.landmark(LandmarkType.RIGHT_SHOULDER)
        val rElbow = pose.landmark(LandmarkType.RIGHT_ELBOW)
        val rWrist = pose.landmark(LandmarkType.RIGHT_WRIST)

        if (lShoulder == null || lElbow == null || lWrist == null ||
            rShoulder == null || rElbow == null || rWrist == null) {
            return RepCounterResult(repCount, currentPhase, false, 0f)
        }

        val confidence = pose.avgConfidence(listOf(
            LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW,
            LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW
        ))
        if (confidence < 0.4f) return RepCounterResult(repCount, currentPhase, false, confidence)

        val avgElbowAngle = (AngleCalculator.angleDegrees(lShoulder, lElbow, lWrist) +
                             AngleCalculator.angleDegrees(rShoulder, rElbow, rWrist)) / 2f

        val counted = trackAngle(avgElbowAngle, pose.timestampMs)
        return RepCounterResult(repCount, currentPhase, counted, confidence)
    }
}

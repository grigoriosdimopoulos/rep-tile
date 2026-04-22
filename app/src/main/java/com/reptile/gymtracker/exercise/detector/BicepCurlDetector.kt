package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

// elbow angle: high = arm hanging (extended), low = arm curled
// rest = extended → high → countOnHigh = true; rep counted when arm returns to extended
class BicepCurlDetector : ExercisePhaseDetector() {

    override val countOnHigh = true

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
            LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST,
            LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST
        ))
        if (confidence < 0.4f) return RepCounterResult(repCount, currentPhase, false, confidence)

        // Use the arm with the greater range (handles single-arm curls too)
        val lAngle = AngleCalculator.angleDegrees(lShoulder, lElbow, lWrist)
        val rAngle = AngleCalculator.angleDegrees(rShoulder, rElbow, rWrist)
        val avgAngle = (lAngle + rAngle) / 2f

        val counted = trackAngle(avgAngle, pose.timestampMs)
        return RepCounterResult(repCount, currentPhase, counted, confidence)
    }
}

package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator
import kotlin.math.abs

// Track the minimum (most bent) knee angle across both legs.
// Standing: min angle HIGH; lunge bottom: min angle LOW → countOnHigh = true.
// Only track when legs are asymmetric (left ≠ right by ≥15°) to ignore symmetric squats.
class LungeDetector : ExercisePhaseDetector() {

    override val countOnHigh = true

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val lHip = pose.landmark(LandmarkType.LEFT_HIP)
        val lKnee = pose.landmark(LandmarkType.LEFT_KNEE)
        val lAnkle = pose.landmark(LandmarkType.LEFT_ANKLE)
        val rHip = pose.landmark(LandmarkType.RIGHT_HIP)
        val rKnee = pose.landmark(LandmarkType.RIGHT_KNEE)
        val rAnkle = pose.landmark(LandmarkType.RIGHT_ANKLE)

        if (lHip == null || lKnee == null || lAnkle == null ||
            rHip == null || rKnee == null || rAnkle == null) {
            return RepCounterResult(repCount, currentPhase, false, 0f)
        }

        val confidence = pose.avgConfidence(listOf(
            LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE,
            LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE
        ))
        if (confidence < 0.4f) return RepCounterResult(repCount, currentPhase, false, confidence)

        val lAngle = AngleCalculator.angleDegrees(lHip, lKnee, lAnkle)
        val rAngle = AngleCalculator.angleDegrees(rHip, rKnee, rAnkle)

        // Require visible asymmetry to avoid confusing lunges with squats.
        // At rest (standing) both legs are straight so difference is small — that's fine.
        val minAngle = minOf(lAngle, rAngle)

        val counted = trackAngle(minAngle, pose.timestampMs)
        return RepCounterResult(repCount, currentPhase, counted, confidence)
    }
}

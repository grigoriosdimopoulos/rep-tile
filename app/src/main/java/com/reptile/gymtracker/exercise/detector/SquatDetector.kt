package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

// knee angle: high = standing, low = squatting → rest at high → countOnHigh = true
class SquatDetector : ExercisePhaseDetector() {

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
            LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE,
            LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE
        ))
        if (confidence < 0.4f) return RepCounterResult(repCount, currentPhase, false, confidence)

        val avgKneeAngle = (AngleCalculator.angleDegrees(lHip, lKnee, lAnkle) +
                            AngleCalculator.angleDegrees(rHip, rKnee, rAnkle)) / 2f

        val counted = trackAngle(avgKneeAngle, pose.timestampMs)
        return RepCounterResult(repCount, currentPhase, counted, confidence)
    }
}

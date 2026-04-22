package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.exercise.AngleCalculator

// hip angle: high = standing (hip extended), low = hinged forward
// rest = standing → high → countOnHigh = true; rep counted on lockout
class DeadliftDetector : ExercisePhaseDetector() {

    override val countOnHigh = true

    override fun processFrame(pose: DetectedPose): RepCounterResult {
        val lShoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
        val lHip = pose.landmark(LandmarkType.LEFT_HIP)
        val lKnee = pose.landmark(LandmarkType.LEFT_KNEE)
        val rShoulder = pose.landmark(LandmarkType.RIGHT_SHOULDER)
        val rHip = pose.landmark(LandmarkType.RIGHT_HIP)
        val rKnee = pose.landmark(LandmarkType.RIGHT_KNEE)

        if (lShoulder == null || lHip == null || lKnee == null ||
            rShoulder == null || rHip == null || rKnee == null) {
            return RepCounterResult(repCount, currentPhase, false, 0f)
        }

        val confidence = pose.avgConfidence(listOf(
            LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE,
            LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE
        ))
        if (confidence < 0.4f) return RepCounterResult(repCount, currentPhase, false, confidence)

        val avgHipAngle = (AngleCalculator.angleDegrees(lShoulder, lHip, lKnee) +
                           AngleCalculator.angleDegrees(rShoulder, rHip, rKnee)) / 2f

        val counted = trackAngle(avgHipAngle, pose.timestampMs)
        return RepCounterResult(repCount, currentPhase, counted, confidence)
    }
}

package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.data.model.ExerciseType
import kotlin.math.abs

class ExerciseClassifier {
    private val frameBuffer = ArrayDeque<DetectedPose>(30)
    private val windowSize = 30

    fun classify(pose: DetectedPose): ExerciseType {
        frameBuffer.addLast(pose)
        if (frameBuffer.size > windowSize) frameBuffer.removeFirst()
        if (frameBuffer.size < 5) return ExerciseType.UNKNOWN
        return scoreAllExercises()
    }

    private fun scoreAllExercises(): ExerciseType {
        val scores = mutableMapOf<ExerciseType, Float>()
        scores[ExerciseType.SQUAT] = scoreSquat()
        scores[ExerciseType.PUSH_UP] = scorePushUp()
        scores[ExerciseType.BICEP_CURL] = scoreBicepCurl()
        scores[ExerciseType.SHOULDER_PRESS] = scoreShoulderPress()
        scores[ExerciseType.DEADLIFT] = scoreDeadlift()
        scores[ExerciseType.LUNGE] = scoreLunge()

        val best = scores.maxByOrNull { it.value }
        return if ((best?.value ?: 0f) > 0.3f) best!!.key else ExerciseType.UNKNOWN
    }

    private fun scoreSquat(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_HIP) &&
            pose.isVisible(LandmarkType.LEFT_KNEE) &&
            pose.isVisible(LandmarkType.LEFT_ANKLE)
        }
        if (relevant.size < 3) return 0f

        val kneeAngles = relevant.mapNotNull { pose ->
            val hip = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val knee = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            val ankle = pose.landmark(LandmarkType.LEFT_ANKLE) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(hip, knee, ankle)
        }
        if (kneeAngles.isEmpty()) return 0f

        val range = (kneeAngles.maxOrNull() ?: 0f) - (kneeAngles.minOrNull() ?: 0f)
        val hasBodyVertical = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            if (shoulder == null || hip == null) false
            else AngleCalculator.verticalAngleDegrees(shoulder, hip) < 25f
        }
        return if (range > 30f && hasBodyVertical) (range / 120f).coerceAtMost(1f) else 0f
    }

    private fun scorePushUp(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_SHOULDER) &&
            pose.isVisible(LandmarkType.LEFT_ELBOW)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val elbow = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val wrist = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(shoulder, elbow, wrist)
        }
        if (elbowAngles.isEmpty()) return 0f

        val range = (elbowAngles.maxOrNull() ?: 0f) - (elbowAngles.minOrNull() ?: 0f)
        val isHorizontal = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            if (shoulder == null || hip == null) false
            else AngleCalculator.verticalAngleDegrees(shoulder, hip) > 60f
        }
        return if (range > 40f && isHorizontal) (range / 100f).coerceAtMost(1f) else 0f
    }

    private fun scoreBicepCurl(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_ELBOW) && pose.isVisible(LandmarkType.LEFT_WRIST)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val elbow = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val wrist = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(shoulder, elbow, wrist)
        }
        if (elbowAngles.isEmpty()) return 0f

        val range = (elbowAngles.maxOrNull() ?: 0f) - (elbowAngles.minOrNull() ?: 0f)
        val isBodyVertical = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            if (shoulder == null || hip == null) false
            else AngleCalculator.verticalAngleDegrees(shoulder, hip) < 20f
        }
        val hasShoulderStable = relevant.size >= 5 && run {
            val shoulderYValues = relevant.mapNotNull { it.landmark(LandmarkType.LEFT_SHOULDER)?.y }
            if (shoulderYValues.size < 3) false
            else (shoulderYValues.maxOrNull()!! - shoulderYValues.minOrNull()!!) < 0.05f
        }
        return if (range > 60f && isBodyVertical && hasShoulderStable)
            (range / 130f).coerceAtMost(1f) else 0f
    }

    private fun scoreShoulderPress(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_SHOULDER) && pose.isVisible(LandmarkType.LEFT_ELBOW)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val elbow = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val wrist = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(shoulder, elbow, wrist)
        }
        val hasWristsAbove = relevant.any { pose ->
            val wrist = pose.landmark(LandmarkType.LEFT_WRIST)
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            wrist != null && shoulder != null && wrist.y < shoulder.y
        }
        if (elbowAngles.isEmpty()) return 0f
        val range = (elbowAngles.maxOrNull() ?: 0f) - (elbowAngles.minOrNull() ?: 0f)
        return if (range > 40f && hasWristsAbove) (range / 100f).coerceAtMost(1f) else 0f
    }

    private fun scoreDeadlift(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_HIP) && pose.isVisible(LandmarkType.LEFT_SHOULDER)
        }
        if (relevant.size < 3) return 0f

        val hipAngles = relevant.mapNotNull { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val hip = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val knee = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(shoulder, hip, knee)
        }
        if (hipAngles.isEmpty()) return 0f
        val range = (hipAngles.maxOrNull() ?: 0f) - (hipAngles.minOrNull() ?: 0f)
        val isBodyHorizontal = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            if (shoulder == null || hip == null) false
            else AngleCalculator.verticalAngleDegrees(shoulder, hip) > 45f
        }
        return if (range > 50f && isBodyHorizontal) (range / 120f).coerceAtMost(1f) else 0f
    }

    private fun scoreLunge(): Float {
        val relevant = frameBuffer.filter { pose ->
            pose.isVisible(LandmarkType.LEFT_KNEE) && pose.isVisible(LandmarkType.RIGHT_KNEE)
        }
        if (relevant.size < 3) return 0f

        val asymmetry = relevant.mapNotNull { pose ->
            val lHip = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val lKnee = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            val lAnkle = pose.landmark(LandmarkType.LEFT_ANKLE) ?: return@mapNotNull null
            val rHip = pose.landmark(LandmarkType.RIGHT_HIP) ?: return@mapNotNull null
            val rKnee = pose.landmark(LandmarkType.RIGHT_KNEE) ?: return@mapNotNull null
            val rAnkle = pose.landmark(LandmarkType.RIGHT_ANKLE) ?: return@mapNotNull null
            val leftAngle = AngleCalculator.angleDegrees(lHip, lKnee, lAnkle)
            val rightAngle = AngleCalculator.angleDegrees(rHip, rKnee, rAnkle)
            abs(leftAngle - rightAngle)
        }.average().toFloat()

        return if (asymmetry > 35f) (asymmetry / 90f).coerceAtMost(1f) else 0f
    }

    fun reset() {
        frameBuffer.clear()
    }
}

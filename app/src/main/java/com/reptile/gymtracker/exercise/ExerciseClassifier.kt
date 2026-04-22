package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.data.model.ExerciseType
import kotlin.math.abs

class ExerciseClassifier {
    private val frameBuffer = ArrayDeque<DetectedPose>(45)
    private val windowSize = 45

    // Hysteresis: once we have a confident classification keep it until
    // confidence drops below HYSTERESIS_THRESHOLD (avoids flickering).
    private var lastClassification: ExerciseType = ExerciseType.UNKNOWN
    private val confirmThreshold = 0.22f   // score needed to adopt a new classification
    private val hysteresisThreshold = 0.12f // score below which we revert to UNKNOWN

    fun classify(pose: DetectedPose): ExerciseType {
        frameBuffer.addLast(pose)
        if (frameBuffer.size > windowSize) frameBuffer.removeFirst()
        if (frameBuffer.size < 3) return lastClassification

        val scores = mapOf(
            ExerciseType.SQUAT to scoreSquat(),
            ExerciseType.PUSH_UP to scorePushUp(),
            ExerciseType.BICEP_CURL to scoreBicepCurl(),
            ExerciseType.SHOULDER_PRESS to scoreShoulderPress(),
            ExerciseType.DEADLIFT to scoreDeadlift(),
            ExerciseType.LUNGE to scoreLunge()
        )

        val best = scores.maxByOrNull { it.value }!!

        lastClassification = when {
            // Enough confidence to adopt a new classification
            best.value >= confirmThreshold -> best.key
            // Keep current classification if it still scores reasonably
            lastClassification != ExerciseType.UNKNOWN &&
                    (scores[lastClassification] ?: 0f) >= hysteresisThreshold -> lastClassification
            // Drop to UNKNOWN
            else -> ExerciseType.UNKNOWN
        }
        return lastClassification
    }

    // ── per-exercise scoring ──────────────────────────────────────────────────

    private fun scoreSquat(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_HIP) &&
            it.isVisible(LandmarkType.LEFT_KNEE) &&
            it.isVisible(LandmarkType.LEFT_ANKLE)
        }
        if (relevant.size < 3) return 0f

        val kneeAngles = relevant.mapNotNull { pose ->
            val hip = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val knee = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            val ankle = pose.landmark(LandmarkType.LEFT_ANKLE) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(hip, knee, ankle)
        }
        if (kneeAngles.size < 3) return 0f

        val range = kneeAngles.max() - kneeAngles.min()
        val bodyVertical = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            shoulder != null && hip != null &&
                    AngleCalculator.verticalAngleDegrees(shoulder, hip) < 35f
        }
        return if (range > 20f && bodyVertical) (range / 100f).coerceAtMost(1f) else 0f
    }

    private fun scorePushUp(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_SHOULDER) && it.isVisible(LandmarkType.LEFT_ELBOW)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val s = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val e = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val w = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(s, e, w)
        }
        if (elbowAngles.size < 3) return 0f

        val range = elbowAngles.max() - elbowAngles.min()
        val isHorizontal = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            shoulder != null && hip != null &&
                    AngleCalculator.verticalAngleDegrees(shoulder, hip) > 55f
        }
        return if (range > 30f && isHorizontal) (range / 90f).coerceAtMost(1f) else 0f
    }

    private fun scoreBicepCurl(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_ELBOW) && it.isVisible(LandmarkType.LEFT_WRIST)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val s = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val e = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val w = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(s, e, w)
        }
        if (elbowAngles.size < 3) return 0f

        val range = elbowAngles.max() - elbowAngles.min()
        val bodyVertical = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            shoulder != null && hip != null &&
                    AngleCalculator.verticalAngleDegrees(shoulder, hip) < 25f
        }
        // Shoulder stability: shoulder y should not move much
        val shoulderYRange = relevant.mapNotNull { it.landmark(LandmarkType.LEFT_SHOULDER)?.y }
            .let { ys -> if (ys.size >= 3) ys.max() - ys.min() else 1f }
        return if (range > 45f && bodyVertical && shoulderYRange < 0.08f)
            (range / 120f).coerceAtMost(1f) else 0f
    }

    private fun scoreShoulderPress(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_SHOULDER) && it.isVisible(LandmarkType.LEFT_ELBOW)
        }
        if (relevant.size < 3) return 0f

        val elbowAngles = relevant.mapNotNull { pose ->
            val s = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val e = pose.landmark(LandmarkType.LEFT_ELBOW) ?: return@mapNotNull null
            val w = pose.landmark(LandmarkType.LEFT_WRIST) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(s, e, w)
        }
        if (elbowAngles.size < 3) return 0f

        val range = elbowAngles.max() - elbowAngles.min()
        val hasWristsAbove = relevant.any { pose ->
            val wrist = pose.landmark(LandmarkType.LEFT_WRIST)
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            wrist != null && shoulder != null && wrist.y < shoulder.y
        }
        return if (range > 35f && hasWristsAbove) (range / 90f).coerceAtMost(1f) else 0f
    }

    private fun scoreDeadlift(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_HIP) && it.isVisible(LandmarkType.LEFT_SHOULDER)
        }
        if (relevant.size < 3) return 0f

        val hipAngles = relevant.mapNotNull { pose ->
            val s = pose.landmark(LandmarkType.LEFT_SHOULDER) ?: return@mapNotNull null
            val h = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val k = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            AngleCalculator.angleDegrees(s, h, k)
        }
        if (hipAngles.size < 3) return 0f

        val range = hipAngles.max() - hipAngles.min()
        val hasHorizontalPhase = relevant.any { pose ->
            val shoulder = pose.landmark(LandmarkType.LEFT_SHOULDER)
            val hip = pose.landmark(LandmarkType.LEFT_HIP)
            shoulder != null && hip != null &&
                    AngleCalculator.verticalAngleDegrees(shoulder, hip) > 40f
        }
        return if (range > 40f && hasHorizontalPhase) (range / 110f).coerceAtMost(1f) else 0f
    }

    private fun scoreLunge(): Float {
        val relevant = frameBuffer.filter {
            it.isVisible(LandmarkType.LEFT_KNEE) && it.isVisible(LandmarkType.RIGHT_KNEE)
        }
        if (relevant.size < 3) return 0f

        val asymmetry = relevant.mapNotNull { pose ->
            val lh = pose.landmark(LandmarkType.LEFT_HIP) ?: return@mapNotNull null
            val lk = pose.landmark(LandmarkType.LEFT_KNEE) ?: return@mapNotNull null
            val la = pose.landmark(LandmarkType.LEFT_ANKLE) ?: return@mapNotNull null
            val rh = pose.landmark(LandmarkType.RIGHT_HIP) ?: return@mapNotNull null
            val rk = pose.landmark(LandmarkType.RIGHT_KNEE) ?: return@mapNotNull null
            val ra = pose.landmark(LandmarkType.RIGHT_ANKLE) ?: return@mapNotNull null
            abs(AngleCalculator.angleDegrees(lh, lk, la) - AngleCalculator.angleDegrees(rh, rk, ra))
        }.average().toFloat()

        return if (asymmetry > 28f) (asymmetry / 80f).coerceAtMost(1f) else 0f
    }

    fun reset() {
        frameBuffer.clear()
        lastClassification = ExerciseType.UNKNOWN
    }
}

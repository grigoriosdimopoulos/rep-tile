package com.reptile.gymtracker.exercise.detector

import com.reptile.gymtracker.camera.model.DetectedPose

enum class ExercisePhase { NEUTRAL, DOWN, UP, EXTENDED }

data class RepCounterResult(
    val repCount: Int,
    val currentPhase: ExercisePhase,
    val repJustCompleted: Boolean,
    val confidence: Float
)

/**
 * Adaptive rep counter — no fixed angle thresholds.
 *
 * Algorithm:
 *  1. EMA-smooth the raw angle signal to remove per-frame jitter.
 *  2. Track a decaying peak (max) and valley (min) that follow the signal
 *     slowly downward / upward so they always represent "recent best".
 *  3. A phase transition fires when the smoothed angle moves more than
 *     TRANSITION_FRACTION of the observed range away from the peak/valley.
 *  4. A rep is counted once both transitions complete (high→low→high or
 *     low→high→low depending on the exercise).
 *
 * This adapts automatically to every person's range of motion and camera
 * angle — nothing is hard-coded in degrees.
 */
abstract class ExercisePhaseDetector {

    // ── state visible to subclasses ──────────────────────────────────────────
    protected var currentPhase: ExercisePhase = ExercisePhase.NEUTRAL
    protected var repCount: Int = 0
    protected var consecutivePhaseFrames: Int = 0

    // ── adaptive signal tracking (private) ───────────────────────────────────
    private var emaAngle: Float = Float.NaN
    private var peakAngle: Float = Float.NaN   // decays slowly toward current
    private var valleyAngle: Float = Float.NaN // decays slowly toward current
    private var lastRepTimestampMs: Long = 0L

    // ── tuneable constants ────────────────────────────────────────────────────
    private val emaAlpha = 0.35f          // smoothing (higher = faster but noisier)
    private val peakValleyDecay = 0.997f  // half-life ≈ 230 frames ≈ 7-8 s at 30 fps
    private val transitionFraction = 0.38f // fraction of range that triggers phase change
    private val minRangeOfMotion = 18f    // degrees — ignore tiny fidgets
    private val minRepIntervalMs = 380L   // max rep frequency guard
    protected val debounceFrames = 2      // consecutive frames needed to confirm phase

    /**
     * true  → rest position has HIGH angle (squat, push-up, curl, deadlift, lunge)
     *          rep = high → low → high
     * false → rest position has LOW angle (shoulder press)
     *          rep = low → high → low
     */
    protected open val countOnHigh: Boolean = true

    // ── public API ───────────────────────────────────────────────────────────

    abstract fun processFrame(pose: DetectedPose): RepCounterResult

    fun adjustRepCount(delta: Int) {
        repCount = maxOf(0, repCount + delta)
    }

    fun reset() {
        currentPhase = if (countOnHigh) ExercisePhase.NEUTRAL else ExercisePhase.DOWN
        repCount = 0
        consecutivePhaseFrames = 0
        lastRepTimestampMs = 0L
        emaAngle = Float.NaN
        peakAngle = Float.NaN
        valleyAngle = Float.NaN
    }

    fun currentRepCount(): Int = repCount

    // ── core adaptive tracking ────────────────────────────────────────────────

    /**
     * Call once per frame with the raw joint angle and the frame timestamp.
     * Returns true on the frame a rep is confirmed.
     */
    protected fun trackAngle(rawAngle: Float, timestampMs: Long): Boolean {
        // 1. EMA smoothing
        emaAngle = if (emaAngle.isNaN()) rawAngle
                   else emaAngle * (1f - emaAlpha) + rawAngle * emaAlpha

        // 2. Adaptive peak/valley — rise instantly to new max/min, decay slowly otherwise
        peakAngle = if (peakAngle.isNaN()) emaAngle
                    else maxOf(emaAngle, peakAngle * peakValleyDecay + emaAngle * (1f - peakValleyDecay))
        valleyAngle = if (valleyAngle.isNaN()) emaAngle
                      else minOf(emaAngle, valleyAngle * peakValleyDecay + emaAngle * (1f - peakValleyDecay))

        val range = peakAngle - valleyAngle
        if (range < minRangeOfMotion) return false

        val threshold = range * transitionFraction

        return if (countOnHigh) detectHighToLow(threshold, timestampMs)
               else detectLowToHigh(threshold, timestampMs)
    }

    // high (NEUTRAL) → low (DOWN) → high (NEUTRAL) + count
    private fun detectHighToLow(threshold: Float, timestampMs: Long): Boolean {
        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                if (emaAngle < peakAngle - threshold) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                if (emaAngle > valleyAngle + threshold) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.NEUTRAL
                        consecutivePhaseFrames = 0
                        return tryCountRep(timestampMs)
                    }
                } else consecutivePhaseFrames = 0
            }
            else -> currentPhase = ExercisePhase.NEUTRAL
        }
        return false
    }

    // low (DOWN) → high (UP) → low (DOWN) + count
    private fun detectLowToHigh(threshold: Float, timestampMs: Long): Boolean {
        when (currentPhase) {
            ExercisePhase.DOWN -> {
                if (emaAngle > valleyAngle + threshold) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.UP
                        consecutivePhaseFrames = 0
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.UP -> {
                if (emaAngle < peakAngle - threshold) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                        return tryCountRep(timestampMs)
                    }
                } else consecutivePhaseFrames = 0
            }
            else -> currentPhase = ExercisePhase.DOWN
        }
        return false
    }

    private fun tryCountRep(timestampMs: Long): Boolean {
        if (timestampMs - lastRepTimestampMs >= minRepIntervalMs) {
            repCount++
            lastRepTimestampMs = timestampMs
            return true
        }
        return false
    }
}

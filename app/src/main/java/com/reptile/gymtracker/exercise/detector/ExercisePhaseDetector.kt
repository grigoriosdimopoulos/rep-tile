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
 * Adaptive rep counter — calibrates to the person's own range of motion.
 *
 * Key invariant that prevents micro-movements from counting:
 *   A rep is only counted when the angle traversed ≥ TRANSITION_FRACTION of the
 *   observed range on the WAY DOWN *and* ≥ MIN_DEPTH_FRACTION of the full
 *   peak→valley span is actually reached during the DOWN phase.
 *   A casual weight-shift or stance adjustment is typically 20-35 % of the full
 *   squat/press range — both guards together raise the bar well above that.
 */
abstract class ExercisePhaseDetector {

    // ── subclass-visible state ────────────────────────────────────────────────
    protected var currentPhase: ExercisePhase = ExercisePhase.NEUTRAL
    protected var repCount: Int = 0
    protected var consecutivePhaseFrames: Int = 0

    // ── adaptive signal ───────────────────────────────────────────────────────
    private var emaAngle: Float = Float.NaN
    private var peakAngle: Float = Float.NaN
    private var valleyAngle: Float = Float.NaN
    private var lastRepTimestampMs: Long = 0L

    // Depth tracking: how deep did the angle actually go during the DOWN phase?
    private var downPhaseMinAngle: Float = Float.NaN   // for countOnHigh
    private var upPhaseMaxAngle: Float = Float.NaN     // for !countOnHigh
    private var framesInActivePhase: Int = 0            // min dwell guard

    // ── constants ─────────────────────────────────────────────────────────────
    private val emaAlpha = 0.25f           // more smoothing → less jitter
    private val peakValleyDecay = 0.998f   // peak/valley half-life ≈ 350 frames ≈ ~12 s
    private val transitionFraction = 0.55f // 55 % of range to enter/exit DOWN phase
    private val minDepthFraction = 0.60f   // must reach 60 % into range to count the rep
    private val minDwellFrames = 4         // stay in DOWN/UP for ≥ 4 frames (~130 ms)
    private val minRangeOfMotion = 25f     // ignore anything smaller than 25 °
    private val minRepIntervalMs = 500L
    protected val debounceFrames = 3

    /** Override in subclasses to choose which direction is the "rest" position. */
    protected open val countOnHigh: Boolean = true

    // ── public API ────────────────────────────────────────────────────────────

    abstract fun processFrame(pose: DetectedPose): RepCounterResult

    fun adjustRepCount(delta: Int) { repCount = maxOf(0, repCount + delta) }

    fun reset() {
        currentPhase = if (countOnHigh) ExercisePhase.NEUTRAL else ExercisePhase.DOWN
        repCount = 0
        consecutivePhaseFrames = 0
        lastRepTimestampMs = 0L
        emaAngle = Float.NaN
        peakAngle = Float.NaN
        valleyAngle = Float.NaN
        downPhaseMinAngle = Float.NaN
        upPhaseMaxAngle = Float.NaN
        framesInActivePhase = 0
    }

    fun currentRepCount(): Int = repCount

    // ── core tracking ─────────────────────────────────────────────────────────

    protected fun trackAngle(rawAngle: Float, timestampMs: Long): Boolean {
        // 1. EMA smoothing
        emaAngle = if (emaAngle.isNaN()) rawAngle
                   else emaAngle * (1f - emaAlpha) + rawAngle * emaAlpha

        // 2. Adaptive watermarks
        peakAngle = if (peakAngle.isNaN()) emaAngle
                    else maxOf(emaAngle,
                        peakAngle * peakValleyDecay + emaAngle * (1f - peakValleyDecay))
        valleyAngle = if (valleyAngle.isNaN()) emaAngle
                      else minOf(emaAngle,
                          valleyAngle * peakValleyDecay + emaAngle * (1f - peakValleyDecay))

        val range = peakAngle - valleyAngle
        if (range < minRangeOfMotion) return false

        val trigger = range * transitionFraction      // to enter opposite phase
        val requiredDepth = range * minDepthFraction  // min excursion to count a rep

        return if (countOnHigh) detectHighToLow(trigger, requiredDepth, timestampMs)
               else detectLowToHigh(trigger, requiredDepth, timestampMs)
    }

    // ── high→low→high (squat, push-up, curl, deadlift, lunge) ────────────────

    private fun detectHighToLow(trigger: Float, requiredDepth: Float, timestampMs: Long): Boolean {
        when (currentPhase) {
            ExercisePhase.NEUTRAL -> {
                if (emaAngle < peakAngle - trigger) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                        framesInActivePhase = 0
                        downPhaseMinAngle = emaAngle
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.DOWN -> {
                framesInActivePhase++
                // Track how deep we actually went
                if (emaAngle < downPhaseMinAngle) downPhaseMinAngle = emaAngle

                if (emaAngle > valleyAngle + trigger) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        val wentDeepEnough = downPhaseMinAngle <= peakAngle - requiredDepth
                        val heldLongEnough = framesInActivePhase >= minDwellFrames
                        currentPhase = ExercisePhase.NEUTRAL
                        consecutivePhaseFrames = 0
                        framesInActivePhase = 0
                        downPhaseMinAngle = Float.NaN
                        if (wentDeepEnough && heldLongEnough) return tryCountRep(timestampMs)
                    }
                } else consecutivePhaseFrames = 0
            }
            else -> currentPhase = ExercisePhase.NEUTRAL
        }
        return false
    }

    // ── low→high→low (shoulder press) ────────────────────────────────────────

    private fun detectLowToHigh(trigger: Float, requiredDepth: Float, timestampMs: Long): Boolean {
        when (currentPhase) {
            ExercisePhase.DOWN -> {
                if (emaAngle > valleyAngle + trigger) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        currentPhase = ExercisePhase.UP
                        consecutivePhaseFrames = 0
                        framesInActivePhase = 0
                        upPhaseMaxAngle = emaAngle
                    }
                } else consecutivePhaseFrames = 0
            }
            ExercisePhase.UP -> {
                framesInActivePhase++
                if (emaAngle > upPhaseMaxAngle) upPhaseMaxAngle = emaAngle

                if (emaAngle < peakAngle - trigger) {
                    if (++consecutivePhaseFrames >= debounceFrames) {
                        val wentHighEnough = upPhaseMaxAngle >= valleyAngle + requiredDepth
                        val heldLongEnough = framesInActivePhase >= minDwellFrames
                        currentPhase = ExercisePhase.DOWN
                        consecutivePhaseFrames = 0
                        framesInActivePhase = 0
                        upPhaseMaxAngle = Float.NaN
                        if (wentHighEnough && heldLongEnough) return tryCountRep(timestampMs)
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

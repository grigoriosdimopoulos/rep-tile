package com.reptile.gymtracker.exercise

import com.reptile.gymtracker.camera.model.PoseLandmarkPoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object AngleCalculator {

    fun angleDegrees(
        pointA: PoseLandmarkPoint,
        vertex: PoseLandmarkPoint,
        pointC: PoseLandmarkPoint
    ): Float {
        val ax = pointA.x - vertex.x
        val ay = pointA.y - vertex.y
        val cx = pointC.x - vertex.x
        val cy = pointC.y - vertex.y

        val dot = ax * cx + ay * cy
        val magA = sqrt(ax * ax + ay * ay)
        val magC = sqrt(cx * cx + cy * cy)

        if (magA == 0f || magC == 0f) return 0f
        val cosAngle = (dot / (magA * magC)).coerceIn(-1f, 1f)
        return Math.toDegrees(Math.acos(cosAngle.toDouble())).toFloat()
    }

    fun verticalAngleDegrees(
        pointA: PoseLandmarkPoint,
        pointB: PoseLandmarkPoint
    ): Float {
        val dx = pointB.x - pointA.x
        val dy = pointB.y - pointA.y
        return Math.toDegrees(atan2(abs(dx).toDouble(), abs(dy).toDouble())).toFloat()
    }

    fun horizontalAngleDegrees(
        pointA: PoseLandmarkPoint,
        pointB: PoseLandmarkPoint
    ): Float {
        val dx = pointB.x - pointA.x
        val dy = pointB.y - pointA.y
        return Math.toDegrees(atan2(abs(dy).toDouble(), abs(dx).toDouble())).toFloat()
    }

    fun distance(a: PoseLandmarkPoint, b: PoseLandmarkPoint): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        return sqrt(dx * dx + dy * dy)
    }
}

package com.reptile.gymtracker.camera.model

data class PoseLandmarkPoint(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val confidence: Float = 0f
)

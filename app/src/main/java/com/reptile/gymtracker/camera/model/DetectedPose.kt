package com.reptile.gymtracker.camera.model

data class DetectedPose(
    val landmarks: Map<LandmarkType, PoseLandmarkPoint>,
    val timestampMs: Long,
    val confidence: Float
) {
    fun landmark(type: LandmarkType): PoseLandmarkPoint? = landmarks[type]

    fun isVisible(type: LandmarkType, threshold: Float = 0.5f): Boolean =
        (landmarks[type]?.confidence ?: 0f) >= threshold

    fun avgConfidence(types: List<LandmarkType>): Float {
        val valid = types.mapNotNull { landmarks[it]?.confidence }
        return if (valid.isEmpty()) 0f else valid.average().toFloat()
    }
}

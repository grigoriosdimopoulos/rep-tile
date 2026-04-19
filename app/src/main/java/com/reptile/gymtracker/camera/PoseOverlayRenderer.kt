package com.reptile.gymtracker.camera

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.camera.model.SKELETON_CONNECTIONS

object PoseOverlayRenderer {

    private val highConfidenceColor = Color(0xFFA3E635)
    private val lowConfidenceColor = Color(0x80A3E635)
    private val lineColor = Color(0x99A3E635)

    fun render(
        drawScope: DrawScope,
        pose: DetectedPose,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        with(drawScope) {
            // Draw skeleton connections
            for ((start, end) in SKELETON_CONNECTIONS) {
                val startLm = pose.landmark(start) ?: continue
                val endLm = pose.landmark(end) ?: continue
                if (startLm.confidence < 0.3f || endLm.confidence < 0.3f) continue

                val alpha = ((startLm.confidence + endLm.confidence) / 2f).coerceIn(0f, 1f)
                drawLine(
                    color = lineColor.copy(alpha = alpha),
                    start = Offset(startLm.x * canvasWidth, startLm.y * canvasHeight),
                    end = Offset(endLm.x * canvasWidth, endLm.y * canvasHeight),
                    strokeWidth = 3f
                )
            }

            // Draw landmark dots
            for ((type, lm) in pose.landmarks) {
                if (lm.confidence < 0.3f) continue
                val color = if (lm.confidence >= 0.7f) highConfidenceColor else lowConfidenceColor
                val x = lm.x * canvasWidth
                val y = lm.y * canvasHeight
                val radius = if (isKeyJoint(type)) 8f else 5f

                drawCircle(color = color, radius = radius, center = Offset(x, y))
                if (isKeyJoint(type)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = radius + 2f,
                        center = Offset(x, y),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }

    private fun isKeyJoint(type: LandmarkType): Boolean = type in setOf(
        LandmarkType.LEFT_SHOULDER, LandmarkType.RIGHT_SHOULDER,
        LandmarkType.LEFT_ELBOW, LandmarkType.RIGHT_ELBOW,
        LandmarkType.LEFT_WRIST, LandmarkType.RIGHT_WRIST,
        LandmarkType.LEFT_HIP, LandmarkType.RIGHT_HIP,
        LandmarkType.LEFT_KNEE, LandmarkType.RIGHT_KNEE,
        LandmarkType.LEFT_ANKLE, LandmarkType.RIGHT_ANKLE
    )
}

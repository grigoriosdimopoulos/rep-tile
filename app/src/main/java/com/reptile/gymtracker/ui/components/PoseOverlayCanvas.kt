package com.reptile.gymtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.reptile.gymtracker.camera.PoseOverlayRenderer
import com.reptile.gymtracker.camera.model.DetectedPose

@Composable
fun PoseOverlayCanvas(
    pose: DetectedPose?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        pose ?: return@Canvas
        PoseOverlayRenderer.render(this, pose, size.width, size.height)
    }
}

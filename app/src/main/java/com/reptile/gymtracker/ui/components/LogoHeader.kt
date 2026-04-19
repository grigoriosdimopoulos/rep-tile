package com.reptile.gymtracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.GreenAccentDark
import com.reptile.gymtracker.ui.theme.PurpleContainer
import com.reptile.gymtracker.ui.theme.PurplePrimary
import com.reptile.gymtracker.ui.theme.SurfaceDark

@Composable
fun LogoHeader(
    modifier: Modifier = Modifier,
    logoSize: Int = 80
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val pupilRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pupilRotation"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(logoSize.dp)) {
            drawSnakeEye(pupilRotation, glowAlpha)
        }
        Text(
            text = "rep-tile",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = (logoSize * 0.35f).sp,
                letterSpacing = 2.sp,
                color = GreenAccent
            )
        )
    }
}

private fun DrawScope.drawSnakeEye(pupilRotation: Float, glowAlpha: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f * 0.92f

    // Outer dark background
    drawCircle(color = SurfaceDark, radius = r, center = Offset(cx, cy))

    // Glow ring
    drawCircle(
        color = PurplePrimary.copy(alpha = glowAlpha * 0.7f),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.05f)
    )

    // Green iris
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GreenAccent, GreenAccentDark),
            center = Offset(cx, cy),
            radius = r * 0.75f
        ),
        radius = r * 0.75f,
        center = Offset(cx, cy)
    )

    // Slit pupil (vertical oval)
    rotate(degrees = pupilRotation, pivot = Offset(cx, cy)) {
        val pupilPath = Path().apply {
            val pw = r * 0.22f
            val ph = r * 0.6f
            moveTo(cx, cy - ph)
            cubicTo(cx + pw, cy - ph * 0.6f, cx + pw, cy + ph * 0.6f, cx, cy + ph)
            cubicTo(cx - pw, cy + ph * 0.6f, cx - pw, cy - ph * 0.6f, cx, cy - ph)
            close()
        }
        drawPath(pupilPath, color = SurfaceDark)
        drawPath(
            pupilPath,
            brush = Brush.verticalGradient(
                colors = listOf(PurplePrimary.copy(alpha = 0.5f), Color.Transparent),
                startY = cy - r * 0.6f,
                endY = cy
            )
        )
    }

    // Specular highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = r * 0.15f,
        center = Offset(cx - r * 0.25f, cy - r * 0.3f)
    )
}

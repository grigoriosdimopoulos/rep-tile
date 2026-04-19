package com.reptile.gymtracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.OnSurfaceVariant

@Composable
fun RepCounterDisplay(
    repCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = repCount,
            transitionSpec = {
                slideInVertically { height -> -height } togetherWith
                    slideOutVertically { height -> height }
            },
            label = "repCounter"
        ) { count ->
            Text(
                text = count.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Black,
                color = GreenAccent,
                lineHeight = 96.sp
            )
        }
        Text(
            text = "REPS",
            style = MaterialTheme.typography.labelLarge,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

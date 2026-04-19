package com.reptile.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.reptile.gymtracker.ui.theme.PurpleContainer
import com.reptile.gymtracker.ui.theme.PurpleLight

@Composable
fun ExerciseChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = PurpleLight,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PurpleContainer.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

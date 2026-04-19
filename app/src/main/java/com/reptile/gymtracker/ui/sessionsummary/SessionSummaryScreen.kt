package com.reptile.gymtracker.ui.sessionsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reptile.gymtracker.core.util.toFormattedDate
import com.reptile.gymtracker.core.util.toFormattedDuration
import com.reptile.gymtracker.ui.theme.BackgroundDark
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.SurfaceDark
import com.reptile.gymtracker.ui.theme.SurfaceVariant
import kotlin.math.roundToInt

@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onDone: () -> Unit,
    viewModel: SessionSummaryViewModel = hiltViewModel()
) {
    val sessionWithSets by viewModel.sessionWithSets.collectAsStateWithLifecycle()

    LaunchedEffect(sessionId) {
        viewModel.load(sessionId)
    }

    val session = sessionWithSets?.session
    val exerciseSummaries = remember(sessionWithSets) {
        viewModel.getExerciseSummaries()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Session Summary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (session != null) {
                Text(
                    text = "${session.startTimestamp.toFormattedDate()} · ${session.durationSeconds.toFormattedDuration()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Stat cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Sets",
                    value = (sessionWithSets?.totalSets ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Reps",
                    value = (sessionWithSets?.totalReps ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Cal",
                    value = (session?.totalCalories?.roundToInt() ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (exerciseSummaries.isNotEmpty()) {
            item {
                Text(
                    text = "EXERCISES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(exerciseSummaries) { summary ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = summary.exerciseType.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${summary.sets} sets · ${summary.reps} reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${summary.calories.roundToInt()} cal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenAccent
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent,
                    contentColor = BackgroundDark
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = GreenAccent,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

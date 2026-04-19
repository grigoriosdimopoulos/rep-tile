package com.reptile.gymtracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reptile.gymtracker.ui.components.ConfirmDialog
import com.reptile.gymtracker.ui.components.SessionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val filteredSessions by viewModel.filteredSessions.collectAsStateWithLifecycle()
    val selectedTimeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }

    sessionToDelete?.let { id ->
        ConfirmDialog(
            title = "Delete Session",
            message = "This action cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteSession(id)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top bar
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // Time filter chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedTimeFilter == filter,
                    onClick = { viewModel.timeFilter.value = filter },
                    label = {
                        Text(
                            when (filter) {
                                TimeFilter.THIS_WEEK -> "This Week"
                                TimeFilter.THIS_MONTH -> "This Month"
                                TimeFilter.ALL_TIME -> "All Time"
                            }
                        )
                    }
                )
            }
        }

        if (filteredSessions.isEmpty()) {
            Text(
                text = "No sessions found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSessions, key = { it.session.id }) { sws ->
                    SessionCard(
                        sessionWithSets = sws,
                        onClick = { onSessionClick(sws.session.id) },
                        onDelete = { sessionToDelete = sws.session.id }
                    )
                }
            }
        }
    }
}

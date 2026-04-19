package com.reptile.gymtracker.ui.mainmenu

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.ui.components.ConfirmDialog
import com.reptile.gymtracker.ui.components.LogoHeader
import com.reptile.gymtracker.ui.components.SessionCard
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.BackgroundDark

@Composable
fun MainMenuScreen(
    onNavigateToSession: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSessionSummary: (Long) -> Unit = {},
    viewModel: MainMenuViewModel = hiltViewModel()
) {
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val newSessionId by viewModel.newSessionId.collectAsStateWithLifecycle()
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(newSessionId) {
        newSessionId?.let { id ->
            viewModel.clearNewSessionId()
            onNavigateToSession(id)
        }
    }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoHeader(logoSize = 80)
            }
        }

        item {
            Button(
                onClick = { viewModel.startNewSession() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent,
                    contentColor = BackgroundDark
                )
            ) {
                Text(
                    text = "+ NEW SESSION",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("History")
                }
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Settings")
                }
            }
        }

        if (recentSessions.isEmpty()) {
            item {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "No sessions yet.\nTap New Session to get started.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            item {
                Text(
                    text = "RECENT SESSIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(recentSessions, key = { it.session.id }) { sessionWithSets ->
                SessionCard(
                    sessionWithSets = sessionWithSets,
                    onClick = { onNavigateToSessionSummary(sessionWithSets.session.id) },
                    onDelete = { sessionToDelete = sessionWithSets.session.id }
                )
            }
        }
    }
}

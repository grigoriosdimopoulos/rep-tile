package com.reptile.gymtracker.ui.activesession

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import dagger.hilt.android.EntryPointAccessors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.reptile.gymtracker.camera.CameraManager
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.ui.components.ConfirmDialog
import com.reptile.gymtracker.ui.components.PoseOverlayCanvas
import com.reptile.gymtracker.ui.components.RepCounterDisplay
import com.reptile.gymtracker.ui.theme.BackgroundDark
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.SurfaceDark
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CameraManagerEntryPoint {
    fun cameraManager(): CameraManager
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveSessionScreen(
    sessionId: Long,
    onSessionEnded: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var showEndSessionDialog by remember { mutableStateOf(false) }
    var showExerciseSelector by remember { mutableStateOf(false) }

    val cameraManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            CameraManagerEntryPoint::class.java
        ).cameraManager()
    }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId)
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.isEnded) {
        if (uiState.isEnded) {
            cameraManager.unbind()
            onSessionEnded(sessionId)
        }
    }

    if (showEndSessionDialog) {
        ConfirmDialog(
            title = "End Session",
            message = "Your current set will be saved and the session will end.",
            confirmLabel = "End Session",
            onConfirm = {
                showEndSessionDialog = false
                viewModel.endSession()
            },
            onDismiss = { showEndSessionDialog = false }
        )
    }

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            current = uiState.detectedExercise,
            isLocked = uiState.isExerciseLocked,
            onSelect = { type ->
                viewModel.lockExercise(type)
                showExerciseSelector = false
            },
            onAutoDetect = {
                viewModel.unlockExercise()
                showExerciseSelector = false
            },
            onDismiss = { showExerciseSelector = false }
        )
    }

    if (!cameraPermission.status.isGranted) {
        CameraPermissionScreen(
            onGrant = { cameraPermission.launchPermissionRequest() },
            onBack = onNavigateBack
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraManager.bindCamera(lifecycleOwner, previewView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        PoseOverlayCanvas(
            pose = uiState.currentPose,
            modifier = Modifier.fillMaxSize()
        )

        // Timer badge (top right)
        Text(
            text = formatDuration(uiState.elapsedSeconds),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .background(SurfaceDark.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Bottom control panel
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = SurfaceDark.copy(alpha = 0.93f)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Exercise name row — tappable to select/lock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showExerciseSelector = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.detectedExercise.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = GreenAccent
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = if (uiState.isExerciseLocked) Icons.Default.Lock
                                          else Icons.Default.Edit,
                            contentDescription = if (uiState.isExerciseLocked) "Locked" else "Select exercise",
                            tint = GreenAccent.copy(alpha = if (uiState.isExerciseLocked) 1f else 0.55f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Set ${uiState.setNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Rep counter with manual ± buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.adjustReps(-1) },
                        enabled = uiState.repCount > 0,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove rep",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RepCounterDisplay(
                        repCount = uiState.repCount,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { viewModel.adjustReps(1) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(GreenAccent.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add rep",
                            tint = GreenAccent
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.pauseResume() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow
                            else Icons.Default.Pause,
                            contentDescription = if (uiState.isPaused) "Resume" else "Pause"
                        )
                    }
                    Button(
                        onClick = { viewModel.endSet() },
                        modifier = Modifier.weight(2f),
                        enabled = uiState.repCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent,
                            contentColor = BackgroundDark
                        )
                    ) {
                        Text("End Set", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { showEndSessionDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "End Session")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSelectorDialog(
    current: ExerciseType,
    isLocked: Boolean,
    onSelect: (ExerciseType) -> Unit,
    onAutoDetect: () -> Unit,
    onDismiss: () -> Unit
) {
    val exercises = remember {
        ExerciseType.entries.filter { it != ExerciseType.UNKNOWN }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Select Exercise",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(exercises) { type ->
                        val selected = isLocked && current == type
                        if (selected) {
                            Button(
                                onClick = { onSelect(type) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = BackgroundDark
                                )
                            ) {
                                Text(
                                    type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(onClick = { onSelect(type) }) {
                                Text(type.displayName, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onAutoDetect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Auto-detect")
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionScreen(onGrant: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera permission is required to track your workouts.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onGrant) { Text("Grant Permission") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Go Back") }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

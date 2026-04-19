package com.reptile.gymtracker.ui.activesession

import android.Manifest
import android.app.Activity
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.hilt.android.EntryPointAccessors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.reptile.gymtracker.camera.CameraManager
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.detectedExercise.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenAccent
                    )
                    Text(
                        text = "Set ${uiState.setNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RepCounterDisplay(repCount = uiState.repCount)

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

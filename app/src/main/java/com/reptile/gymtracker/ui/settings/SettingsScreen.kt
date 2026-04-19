package com.reptile.gymtracker.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reptile.gymtracker.data.export.ImportResult
import com.reptile.gymtracker.data.export.ImportStrategy
import com.reptile.gymtracker.data.model.ActivityLevel
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.UserProfile
import com.reptile.gymtracker.ui.components.ConfirmDialog
import com.reptile.gymtracker.ui.theme.BackgroundDark
import com.reptile.gymtracker.ui.theme.GreenAccent
import com.reptile.gymtracker.ui.theme.SurfaceDark
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showImportStrategyDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Profile form state
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var age by remember(profile) { mutableIntStateOf(profile?.ageYears ?: 25) }
    var weightKg by remember(profile) { mutableFloatStateOf(profile?.weightKg ?: 70f) }
    var heightCm by remember(profile) { mutableFloatStateOf(profile?.heightCm ?: 170f) }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: Gender.OTHER) }
    var activityLevel by remember(profile) {
        mutableStateOf(profile?.activityLevel ?: ActivityLevel.MODERATE)
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportStrategyDialog = true
        }
    }

    // Handle export URI
    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export workout data"))
            viewModel.clearExportUri()
        }
    }

    // Handle import result
    LaunchedEffect(importResult) {
        importResult?.let { result ->
            val msg = when (result) {
                is ImportResult.Success -> "Imported ${result.sessionsImported} sessions"
                is ImportResult.Failure -> "Import failed: ${result.reason}"
                ImportResult.VersionMismatch -> "File version not supported"
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearImportResult()
        }
    }

    if (showImportStrategyDialog) {
        ConfirmDialog(
            title = "Import Strategy",
            message = "Merge adds to existing data. Replace deletes all existing data first.",
            confirmLabel = "Merge",
            dismissLabel = "Replace",
            onConfirm = {
                pendingImportUri?.let { viewModel.importData(it, ImportStrategy.MERGE) }
                showImportStrategyDialog = false
            },
            onDismiss = {
                pendingImportUri?.let { viewModel.importData(it, ImportStrategy.REPLACE) }
                showImportStrategyDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile section
            SectionHeader("Profile")
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Age slider
                    Column {
                        Text("Age: $age", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = age.toFloat(),
                            onValueChange = { age = it.roundToInt() },
                            valueRange = 13f..100f,
                            steps = 86
                        )
                    }

                    // Weight slider
                    Column {
                        Text(
                            "Weight: ${weightKg.roundToInt()} kg",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = weightKg,
                            onValueChange = { weightKg = it },
                            valueRange = 30f..250f
                        )
                    }

                    // Height slider
                    Column {
                        Text(
                            "Height: ${heightCm.roundToInt()} cm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = heightCm,
                            onValueChange = { heightCm = it },
                            valueRange = 100f..250f
                        )
                    }

                    // Gender
                    Text("Gender", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Gender.entries.forEach { g ->
                            FilterButton(
                                label = g.displayName,
                                selected = gender == g,
                                onClick = { gender = g }
                            )
                        }
                    }

                    // Activity level
                    Text("Activity Level", style = MaterialTheme.typography.bodyMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ActivityLevel.entries.forEach { level ->
                            FilterButton(
                                label = level.displayName,
                                selected = activityLevel == level,
                                onClick = { activityLevel = level },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                UserProfile(
                                    name = name,
                                    ageYears = age,
                                    weightKg = weightKg,
                                    heightCm = heightCm,
                                    gender = gender,
                                    activityLevel = activityLevel
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent,
                            contentColor = BackgroundDark
                        )
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Units section
            SectionHeader("Units")
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use Imperial (lbs/ft)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = preferences.useImperialUnits,
                        onCheckedChange = { viewModel.setImperialUnits(it) }
                    )
                }
            }

            // Data section
            SectionHeader("Data")
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportData() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Data")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Data")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun FilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent,
                contentColor = BackgroundDark
            )
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

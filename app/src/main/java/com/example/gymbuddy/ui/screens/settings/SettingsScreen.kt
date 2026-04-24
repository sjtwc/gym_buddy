package com.example.gymbuddy.ui.screens.settings

import android.content.Intent
import android.database.Cursor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.data.local.NotificationPreferences
import com.example.gymbuddy.provider.UserStatsContract
import com.example.gymbuddy.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUnitsDialog by remember { mutableStateOf(false) }
    var showRestTimerDialog by remember { mutableStateOf(false) }
    var showStreakReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingsSection(title = "Workout")
            
            SettingsItem(
                icon = Icons.Default.FitnessCenter,
                title = "Default Rest Timer",
                subtitle = "${uiState.defaultRestTimerSeconds} seconds",
                onClick = { showRestTimerDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.Speed,
                title = "Weight Unit",
                subtitle = if (uiState.weightUnit == "kg") "Kilograms (kg)" else "Pounds (lb)",
                onClick = { showUnitsDialog = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "Notifications")
            
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Streak Reminders",
                subtitle = if (uiState.streakReminderEnabled) {
                    "Daily at ${uiState.streakReminderTime}"
                } else {
                    "Off"
                },
                onClick = { showStreakReminderDialog = true }
            )
            
            SettingsItemWithSwitch(
                icon = Icons.Default.Timer,
                title = "Rest Timer Notifications",
                subtitle = if (uiState.restTimerNotificationsEnabled) "On" else "Off",
                checked = uiState.restTimerNotificationsEnabled,
                onCheckedChange = { viewModel.setRestTimerNotificationsEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "Calendar")
            
            if (uiState.isCalendarConnected) {
                SettingsItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Google Account",
                    subtitle = uiState.googleAccountEmail ?: "",
                    onClick = { viewModel.signOutFromCalendar() }
                )
                
                SettingsItemWithSwitch(
                    icon = Icons.Default.Sync,
                    title = "Auto-sync",
                    subtitle = if (uiState.autoSyncEnabled) "On" else "Off",
                    checked = uiState.autoSyncEnabled,
                    onCheckedChange = { viewModel.setAutoSyncEnabled(it) }
                )
                
                SettingsItem(
                    icon = Icons.Default.Search,
                    title = "Find Free Time",
                    subtitle = "Find available gym slots",
                    onClick = { viewModel.showFreeTimeDialog() }
                )
                
                SettingsItem(
                    icon = Icons.Default.CloudSync,
                    title = "Sync Now",
                    subtitle = if (uiState.isSyncing) "Syncing..." else "Export schedule to calendar",
                    onClick = { if (!uiState.isSyncing) viewModel.syncToCalendar() }
                )
            } else {
                SettingsItem(
                    icon = Icons.Default.Login,
                    title = "Connect Google Calendar",
                    subtitle = "Sign in to sync workouts",
                    onClick = { viewModel.signInToCalendar() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "Your Stats")
            
            UserStatsCard()

SettingsItem(
                icon = Icons.Default.Refresh,
                title = "Reset Records",
                subtitle = "Clear all personal and predicted records",
                onClick = { viewModel.showResetConfirmation() }

            )

            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "Export to Health Connect",
                subtitle = when {
                    uiState.isExportingHealth -> "Exporting..."
                    uiState.isHealthConnectAvailable -> "Sync workouts to Health app"
                    else -> "Requires Health Connect app"
                },
                onClick = { 
                    if (!uiState.isHealthConnectAvailable) {
                        viewModel.exportToHealthConnect()
                    } else if (!uiState.isExportingHealth) {
                        viewModel.exportToHealthConnect()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "1.13.1",
                onClick = { }
            )
            
            SettingsItem(
                icon = Icons.Default.Code,
                title = "Open Source Licenses",
                subtitle = "View third-party licenses",
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showUnitsDialog) {
        UnitsSelectionDialog(
            currentUnit = if (uiState.weightUnit == "kg") "Kilograms (kg)" else "Pounds (lb)",
            onDismiss = { showUnitsDialog = false },
            onSelect = { unit ->
                viewModel.setWeightUnit(if (unit.contains("kg")) "kg" else "lb")
                showUnitsDialog = false
            }
        )
    }
    
    if (showRestTimerDialog) {
        RestTimerSelectionDialog(
            currentSeconds = uiState.defaultRestTimerSeconds,
            onDismiss = { showRestTimerDialog = false },
            onSelect = { seconds ->
                viewModel.setDefaultRestTimer(seconds)
                showRestTimerDialog = false
            }
        )
    }
    
    if (showStreakReminderDialog) {
        StreakReminderTimeDialog(
            currentHour = uiState.streakReminderHour,
            currentMinute = uiState.streakReminderMinute,
            isEnabled = uiState.streakReminderEnabled,
            onDismiss = { showStreakReminderDialog = false },
            onSave = { hour, minute, enabled ->
                viewModel.setStreakReminderTime(hour, minute, enabled)
                showStreakReminderDialog = false
            }
        )
    }

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetConfirmation() },
            title = { Text("Reset All Records?") },
            text = {
                Text("This will permanently delete all personal records (best 1RM, 2RM, etc.) and predicted records for all exercises. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetAllRecords() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideResetConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    uiState.lastSyncResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSyncResult() },
            title = { Text("Calendar Sync") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSyncResult() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = NeonTeal,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary
            )
        }
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonTeal,
                    checkedTrackColor = NeonTeal.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun UnitsSelectionDialog(
    currentUnit: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var selected by remember { mutableStateOf(currentUnit) }
    val units = listOf("Kilograms (kg)", "Pounds (lb)")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Weight Unit", color = TextPrimary)
        },
        text = {
            Column {
                units.forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = unit }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == unit,
                            onClick = { selected = unit },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(unit, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) {
                Text("Save", color = NeonTeal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun RestTimerSelectionDialog(
    currentSeconds: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var selected by remember { mutableIntStateOf(currentSeconds) }
    val options = listOf(30, 60, 90, 120, 180, 240, 300)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Default Rest Timer", color = TextPrimary)
        },
        text = {
            Column {
                options.forEach { seconds ->
                    val label = when (seconds) {
                        30 -> "30 seconds"
                        60 -> "1 minute"
                        90 -> "1 minute 30 seconds"
                        120 -> "2 minutes"
                        180 -> "3 minutes"
                        240 -> "4 minutes"
                        300 -> "5 minutes"
                        else -> "$seconds seconds"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = seconds }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == seconds,
                            onClick = { selected = seconds },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) {
                Text("Save", color = NeonTeal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakReminderTimeDialog(
    currentHour: Int,
    currentMinute: Int,
    isEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Boolean) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(currentHour) }
    var selectedMinute by remember { mutableIntStateOf(currentMinute) }
    var enabled by remember { mutableStateOf(isEnabled) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Streak Reminders", color = TextPrimary)
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Reminders",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonTeal,
                            checkedTrackColor = NeonTeal.copy(alpha = 0.5f)
                        )
                    )
                }
                
                if (enabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Reminder Time (24-hour)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePicker(
                            initialHour = selectedHour,
                            initialMinute = selectedMinute,
                            onTimeSelected = { hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedHour, selectedMinute, enabled) }) {
                Text("Save", color = NeonTeal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )
    
    TimePicker(
        state = timePickerState,
        colors = TimePickerDefaults.colors(
            clockDialColor = DarkSurfaceElevated,
            selectorColor = NeonTeal,
            containerColor = DarkSurfaceElevated
        )
    )
    
    LaunchedEffect(timePickerState) {
        onTimeSelected(timePickerState.hour, timePickerState.minute)
    }
}

@Composable
fun UserStatsCard() {
    val context = LocalContext.current
    var totalWorkouts by remember { mutableIntStateOf(0) }
    var totalVolume by remember { mutableFloatStateOf(0f) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var title by remember { mutableStateOf("Novice") }
    var personalRecordsCount by remember { mutableIntStateOf(0) }
    var topExercises by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            // Fetch workout stats
            val workoutStatsUri = UserStatsContract.WorkoutStats.CONTENT_URI
            val workoutCursor: Cursor? = context.contentResolver.query(
                workoutStatsUri,
                null, null, null, null
            )
            workoutCursor?.use {
                if (it.moveToFirst()) {
                    totalWorkouts = it.getInt(it.getColumnIndexOrThrow(UserStatsContract.WorkoutStats.COLUMN_TOTAL_WORKOUTS))
                    totalVolume = it.getFloat(it.getColumnIndexOrThrow(UserStatsContract.WorkoutStats.COLUMN_TOTAL_VOLUME))
                }
            }

            // Fetch daily summary for streak, level, title
            val dailySummaryUri = UserStatsContract.DailySummary.CONTENT_URI
            val dailyCursor: Cursor? = context.contentResolver.query(
                dailySummaryUri,
                null, null, null, null
            )
            dailyCursor?.use {
                if (it.moveToFirst()) {
                    currentStreak = it.getInt(it.getColumnIndexOrThrow(UserStatsContract.DailySummary.COLUMN_CURRENT_STREAK))
                    level = it.getInt(it.getColumnIndexOrThrow(UserStatsContract.DailySummary.COLUMN_LEVEL))
                    title = it.getString(it.getColumnIndexOrThrow(UserStatsContract.DailySummary.COLUMN_TITLE)) ?: "Novice"
                }
            }

            val recordsUri = UserStatsContract.PersonalRecords.CONTENT_URI
            val recordsCursor: Cursor? = context.contentResolver.query(
                recordsUri,
                null, null, null, null
            )
            recordsCursor?.use {
                personalRecordsCount = it.count
            }

            val exerciseStatsUri = UserStatsContract.ExerciseStats.CONTENT_URI
            val exerciseCursor: Cursor? = context.contentResolver.query(
                exerciseStatsUri,
                null, null, null, null
            )
            exerciseCursor?.use {
                val exercises = mutableListOf<Pair<String, Float>>()
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndexOrThrow(UserStatsContract.ExerciseStats.COLUMN_EXERCISE_NAME))
                    val bestWeight = it.getFloat(it.getColumnIndexOrThrow(UserStatsContract.ExerciseStats.COLUMN_BEST_WEIGHT))
                    exercises.add(name to bestWeight)
                }
                topExercises = exercises.sortedByDescending { it.second }.take(5)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = {
                        val statsText = buildShareText(
                            totalWorkouts, totalVolume, currentStreak, level, title, personalRecordsCount, topExercises
                        )
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, statsText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Stats"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Stats",
                        tint = NeonTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.FitnessCenter,
                    value = totalWorkouts.toString(),
                    label = "Workouts"
                )
                StatItem(
                    icon = Icons.Default.Scale,
                    value = if (totalVolume > 1000) {
                        String.format("%.1fk", totalVolume / 1000)
                    } else {
                        totalVolume.toInt().toString()
                    },
                    label = "Volume"
                )
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    value = personalRecordsCount.toString(),
                    label = "Records"
                )
            }

            if (topExercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = TextTertiary.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Top Weights",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                topExercises.forEach { (name, weight) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${weight.toInt()} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal
                        )
                    }
                }
            }
        }
    }
}

private fun buildShareText(
    totalWorkouts: Int,
    totalVolume: Float,
    currentStreak: Int,
    level: Int,
    title: String,
    personalRecordsCount: Int,
    topExercises: List<Pair<String, Float>>
): String {
    val volumeText = if (totalVolume > 1000) {
        String.format("%.1fk kg", totalVolume / 1000)
    } else {
        "${totalVolume.toInt()} kg"
    }

    val streakText = if (currentStreak > 0) "\n🔥 Current Streak: $currentStreak days" else ""
    val levelText = "\n⭐ Level $level • $title"
    
    val topWeightsText = if (topExercises.isNotEmpty()) {
        "\n🏆 Top Weights:\n" + topExercises.joinToString("\n") { (name, weight) ->
            "• $name: ${weight.toInt()} kg"
        }
    } else ""

    return """
        💪 My GymBuddy Stats
        $streakText
        $levelText
        
        📊 Total Workouts: $totalWorkouts
        ⚖️ Total Volume: $volumeText  
        🏅 Personal Records: $personalRecordsCount
        $topWeightsText
        
        Shared via GymBuddy
    """.trimIndent()
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonTeal,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
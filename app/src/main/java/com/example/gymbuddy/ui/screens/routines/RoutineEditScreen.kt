package com.example.gymbuddy.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.domain.model.RoutineExercise
import com.example.gymbuddy.domain.model.RoutineExerciseTimer
import com.example.gymbuddy.ui.components.ConfigureRestTimerDialog
import com.example.gymbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditScreen(
    routineId: Long,
    navController: NavController,
    viewModel: RoutineEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var editingExerciseIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(routineId) {
        if (routineId > 0) {
            viewModel.loadRoutine(routineId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (routineId > 0) "Edit Routine" else "Create Routine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Routine Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.routineName.ifEmpty { "New Routine" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.routineName.isEmpty()) TextTertiary else NeonTeal
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.exercises.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No exercises added",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            itemsIndexed(uiState.exercises) { index, exercise ->
                RoutineExerciseCard(
                    exercise = exercise,
                    onConfigureTimer = { editingExerciseIndex = index },
                    onRemove = { viewModel.removeExercise(index) }
                )
            }

            item {
                Button(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercise", color = NeonTeal)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Button(
            onClick = { viewModel.saveRoutine() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Save Routine",
                color = DarkBackground,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            onDismiss = { showExercisePicker = false },
            onSelect = { exercise ->
                viewModel.addExercise(exercise)
                showExercisePicker = false
            }
        )
    }

    if (showRenameDialog) {
        RenameDialog(
            currentName = uiState.routineName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                viewModel.updateRoutineName(name)
                showRenameDialog = false
            }
        )
    }

    editingExerciseIndex?.let { index ->
        val exercise = uiState.exercises[index]
        ConfigureRoutineTimerDialog(
            timers = exercise.timers,
            onDismiss = { editingExerciseIndex = null },
            onSave = { timers ->
                viewModel.updateExerciseTimers(index, timers)
                editingExerciseIndex = null
            }
        )
    }
}

@Composable
fun RoutineExerciseCard(
    exercise: RoutineExercise,
    onConfigureTimer: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configure Timer") },
                            onClick = {
                                showMenu = false
                                onConfigureTimer()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Timer, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove", color = ErrorRed) },
                            onClick = {
                                showMenu = false
                                onRemove()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Timer: ${formatTimerDisplay(exercise.timers)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
fun formatTimerDisplay(timers: List<RoutineExerciseTimer>): String {
    val workTimer = timers.find { it.type == SetType.WORK }?.durationSeconds ?: 0
    val mins = workTimer / 60
    val secs = workTimer % 60
    return if (workTimer > 0) "$mins:${secs.toString().padStart(2, '0')}" else "Not set"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (com.example.gymbuddy.domain.model.Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Routine") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfigureRoutineTimerDialog(
    timers: List<RoutineExerciseTimer>,
    onDismiss: () -> Unit,
    onSave: (List<RoutineExerciseTimer>) -> Unit
) {
    var editedTimers by remember { mutableStateOf(timers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Timers") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(editedTimers.size) { index ->
                    val timer = editedTimers[index]
                    TimerInputRow(
                        timer = timer,
                        onDurationChange = { newDuration ->
                            editedTimers = editedTimers.toMutableList().apply {
                                this[index] = timer.copy(durationSeconds = newDuration)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedTimers) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimerInputRow(
    timer: RoutineExerciseTimer,
    onDurationChange: (Int) -> Unit
) {
    var timerText by remember(timer.durationSeconds) {
        mutableStateOf(formatTimerSeconds(timer.durationSeconds))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timer.type.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = timerText,
            onValueChange = { newValue ->
                timerText = newValue
                val parts = newValue.split(":")
                val seconds = when (parts.size) {
                    2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
                    1 -> parts[0].toIntOrNull() ?: 0
                    else -> 0
                }
                onDurationChange(seconds)
            },
            modifier = Modifier.width(80.dp),
            singleLine = true,
            placeholder = { Text("0:00") }
        )
    }
}

fun formatTimerSeconds(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
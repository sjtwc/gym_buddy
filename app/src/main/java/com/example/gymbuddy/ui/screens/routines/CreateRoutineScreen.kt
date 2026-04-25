package com.example.gymbuddy.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.RoutineSetData
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.ui.screens.exercise.ExercisePickerPage
import com.example.gymbuddy.ui.theme.*
import com.example.gymbuddy.ui.components.common.SetTypeColors
import com.example.gymbuddy.ui.components.common.SetTypeButton as SetTypeButtonComposable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateRoutineScreen(
    navController: NavController,
    routineId: Long = 0,
    viewModel: CreateRoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    LaunchedEffect(routineId) {
        if (routineId > 0) {
            viewModel.loadRoutine(routineId)
        } else {
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = if (routineId > 0) "Edit Routine" else "Create Routine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.routineName,
                onValueChange = { viewModel.updateRoutineName(it) },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            var typeExpanded by remember { mutableStateOf(false) }
            val muscleOptions = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body")

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
                modifier = Modifier.weight(0.8f)
            ) {
                OutlinedTextField(
                    value = uiState.routineTypes.firstOrNull() ?: "Type",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.routineTypes.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.clearRoutineTypes() },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear type",
                                        modifier = Modifier.size(14.dp),
                                        tint = TextSecondary
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        }
                    },
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal
                    )
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    muscleOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(type, style = MaterialTheme.typography.bodyMedium)
                                    if (type in uiState.routineTypes) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(16.dp),
                                            tint = NeonTeal
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.toggleRoutineType(type)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.estimatedMinutes.toString(),
                onValueChange = { viewModel.updateEstimatedMinutes(it.toIntOrNull() ?: 60) },
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { Text("Min", style = MaterialTheme.typography.labelSmall) }
            )
        }

        if (uiState.routineTypes.size > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+ ${uiState.routineTypes.drop(1).joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonTeal
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showExercisePicker = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Exercises")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏋️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No exercises added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add exercises to build your routine",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.exercises) { index, exercise ->
                    RoutineExerciseCard(
                        exerciseName = exercise.name,
                        sets = uiState.exerciseSets[index],
                        bodyFocus = uiState.exerciseBodyFocus[index],
                        timers = uiState.exerciseTimers[index],
                        onUpdateSets = { viewModel.updateExerciseSets(index, it) },
                        onUpdateBodyFocus = { viewModel.updateExerciseBodyFocus(index, it) },
                        onUpdateTimers = { viewModel.updateExerciseTimers(index, it) },
                        onRemove = { viewModel.removeExercise(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        var isSaving by remember { mutableStateOf(false) }

        Button(
            onClick = {
                isSaving = true
                viewModel.saveRoutine { success ->
                    isSaving = false
                    if (success) {
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
            shape = RoundedCornerShape(16.dp),
            enabled = uiState.routineName.isNotBlank() && uiState.exercises.isNotEmpty() && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DarkBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "SAVE ROUTINE",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showExercisePicker) {
        val alreadyAddedIds = uiState.exercises.map { it.id }.toSet()
        ExercisePickerPage(
            onDismiss = { showExercisePicker = false },
            onAddExercises = { exercises ->
                viewModel.addExercises(exercises)
                showExercisePicker = false
            },
            alreadyAddedExerciseIds = alreadyAddedIds
        )
    }
}

@Composable
fun RoutineExerciseCard(
    exerciseName: String,
    sets: List<RoutineSetData>,
    bodyFocus: String,
    timers: List<com.example.gymbuddy.domain.model.RoutineExerciseTimer>,
    onUpdateSets: (List<RoutineSetData>) -> Unit,
    onUpdateBodyFocus: (String) -> Unit,
    onUpdateTimers: (List<com.example.gymbuddy.domain.model.RoutineExerciseTimer>) -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showBodyFocusDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    if (showBodyFocusDialog) {
        BodyFocusDialog(
            currentBodyFocus = bodyFocus,
            onDismiss = { showBodyFocusDialog = false },
            onConfirm = {
                onUpdateBodyFocus(it)
                showBodyFocusDialog = false
            }
        )
    }

    if (showTimerDialog) {
        ConfigureTimerDialog(
            currentTimers = timers,
            onDismiss = { showTimerDialog = false },
            onConfirm = {
                onUpdateTimers(it)
                showTimerDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Body Focus") },
                            onClick = {
                                showMenu = false
                                showBodyFocusDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, null, tint = TextSecondary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Configure Timer") },
                            onClick = {
                                showMenu = false
                                showTimerDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Timer, null, tint = NeonCyan) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            onClick = {
                                showMenu = false
                                onRemove()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) }
                        )
                    }
                }
            }

            if (bodyFocus.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NeonTeal.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = bodyFocus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            RoutineSetHeader()

            sets.forEachIndexed { setIndex, set ->
                RoutineSetRow(
                    set = set,
                    setIndex = setIndex,
                    onUpdateSet = { newSet ->
                        val newSets = sets.toMutableList()
                        newSets[setIndex] = newSet
                        onUpdateSets(newSets)
                    },
                    onDeleteSet = {
                        val newSets = sets.filterIndexed { idx, _ -> idx != setIndex }
                        onUpdateSets(newSets)
                    }
                )

                val timerForSet = timers.find { it.type == set.setType }
                if (timerForSet != null && timerForSet.durationSeconds > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    TimerPreviewRow(
                        durationSeconds = timerForSet.durationSeconds,
                        onDelete = {
                            val newTimers = timers.map {
                                if (it.type == set.setType) it.copy(durationSeconds = 0) else it
                            }
                            onUpdateTimers(newTimers)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            TextButton(
                onClick = {
                    val newSets = sets + RoutineSetData(
                        setNumber = sets.size + 1,
                        reps = "",
                        weight = null,
                        setType = SetType.NORMAL
                    )
                    onUpdateSets(newSets)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set", color = NeonTeal, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun RoutineSetHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Set", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(48.dp))
        Text("Weight", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text("Reps", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(60.dp))
        Spacer(modifier = Modifier.size(32.dp))
    }
}

@Composable
fun RoutineSetRow(
    set: RoutineSetData,
    setIndex: Int,
    onUpdateSet: (RoutineSetData) -> Unit,
    onDeleteSet: () -> Unit
) {
    var weightText by remember(set.weight) { mutableStateOf(set.weight?.toString() ?: "") }
    var repsText by remember(set.reps) { mutableStateOf(set.reps ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetTypeBadge(
                setType = set.setType,
                setNumber = set.setNumber,
                onToggle = { newType -> onUpdateSet(set.copy(setType = newType)) },
                modifier = Modifier.width(48.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(DarkSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, TextTertiary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = weightText,
                    onValueChange = { newValue ->
                        weightText = newValue
                        if (newValue.isEmpty()) {
                            onUpdateSet(set.copy(weight = null))
                        } else {
                            newValue.toFloatOrNull()?.let { w -> onUpdateSet(set.copy(weight = w)) }
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (weightText.isEmpty()) {
                    Text("kg", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(44.dp)
                    .background(DarkSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, TextTertiary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = repsText,
                    onValueChange = { newValue ->
                        repsText = newValue
                        onUpdateSet(set.copy(reps = newValue))
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (repsText.isEmpty()) {
                    Text("reps", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }

            IconButton(
                onClick = onDeleteSet,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SetTypeBadge(
    setType: SetType,
    setNumber: Int,
    onToggle: (SetType) -> Unit,
    modifier: Modifier = Modifier
) {
    com.example.gymbuddy.ui.components.common.SetTypeButton(
        setType = setType,
        setNumber = setNumber,
        onToggle = onToggle,
        modifier = modifier
    )
}

@Composable
fun BodyFocusDialog(
    currentBodyFocus: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedBodyFocus by remember { mutableStateOf(currentBodyFocus) }

    val bodyFocusOptions = listOf("Chest", "Back", "Shoulders", "Arms", "Core", "Legs", "Full Body")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Body Focus") },
        text = {
            Column {
                bodyFocusOptions.forEach { focus ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBodyFocus = focus }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedBodyFocus == focus,
                            onClick = { selectedBodyFocus = focus }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(focus)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedBodyFocus) }) {
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
fun RestTimerDialog(
    currentSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedSeconds by remember { mutableStateOf(currentSeconds) }
    val timerOptions = listOf(30, 45, 60, 90, 120, 180)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rest Timer") },
        text = {
            Column {
                Text(
                    text = "Set rest duration between sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    timerOptions.chunked(3).forEach { column ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            column.forEach { seconds ->
                                val minutes = seconds / 60
                                val secs = seconds % 60
                                val label = if (secs == 0) "${minutes}m" else "${minutes}m ${secs}s"
                                Surface(
                                    onClick = { selectedSeconds = seconds },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedSeconds == seconds) NeonTeal else DarkSurfaceElevated,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = if (selectedSeconds == seconds) DarkBackground else TextPrimary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = selectedSeconds.toString(),
                    onValueChange = { selectedSeconds = it.toIntOrNull() ?: selectedSeconds },
                    label = { Text("Custom (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
confirmButton = {
            TextButton(onClick = { onConfirm(selectedSeconds) }) {
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
fun ConfigureTimerDialog(
    currentTimers: List<com.example.gymbuddy.domain.model.RoutineExerciseTimer>,
    onDismiss: () -> Unit,
    onConfirm: (List<com.example.gymbuddy.domain.model.RoutineExerciseTimer>) -> Unit
) {
    var normalTime by remember { mutableStateOf("01:30") }
    var warmupTime by remember { mutableStateOf("01:00") }
    var workTime by remember { mutableStateOf("01:30") }
    var dropTime by remember { mutableStateOf("01:00") }
    var failureTime by remember { mutableStateOf("01:30") }

    LaunchedEffect(currentTimers) {
        normalTime = currentTimers.find { it.type == SetType.NORMAL }?.let { formatTimeToInput(it.durationSeconds) } ?: "01:30"
        warmupTime = currentTimers.find { it.type == SetType.WARMUP }?.let { formatTimeToInput(it.durationSeconds) } ?: "01:00"
        workTime = currentTimers.find { it.type == SetType.WORK }?.let { formatTimeToInput(it.durationSeconds) } ?: "01:30"
        dropTime = currentTimers.find { it.type == SetType.DROP }?.let { formatTimeToInput(it.durationSeconds) } ?: "01:00"
        failureTime = currentTimers.find { it.type == SetType.FAILURE }?.let { formatTimeToInput(it.durationSeconds) } ?: "01:30"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Timers", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TimerInputRow(
                    label = "Normal",
                    value = normalTime,
                    onValueChange = { normalTime = it }
                )
                TimerInputRow(
                    label = "Warmup",
                    value = warmupTime,
                    onValueChange = { warmupTime = it }
                )
                TimerInputRow(
                    label = "Work",
                    value = workTime,
                    onValueChange = { workTime = it }
                )
                TimerInputRow(
                    label = "Drop",
                    value = dropTime,
                    onValueChange = { dropTime = it }
                )
                TimerInputRow(
                    label = "Failure",
                    value = failureTime,
                    onValueChange = { failureTime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timers = listOf(
                        com.example.gymbuddy.domain.model.RoutineExerciseTimer(SetType.NORMAL, parseTimeInput(normalTime)),
                        com.example.gymbuddy.domain.model.RoutineExerciseTimer(SetType.WARMUP, parseTimeInput(warmupTime)),
                        com.example.gymbuddy.domain.model.RoutineExerciseTimer(SetType.WORK, parseTimeInput(workTime)),
                        com.example.gymbuddy.domain.model.RoutineExerciseTimer(SetType.DROP, parseTimeInput(dropTime)),
                        com.example.gymbuddy.domain.model.RoutineExerciseTimer(SetType.FAILURE, parseTimeInput(failureTime))
                    )
                    onConfirm(timers)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
private fun TimerInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == ':' }
                if (filtered.length <= 5) {
                    if (filtered.length == 2 && !filtered.contains(":")) {
                        onValueChange(filtered + ":")
                    } else {
                        onValueChange(filtered)
                    }
                }
            },
            modifier = Modifier.width(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = { Text("mm:ss", color = TextTertiary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                cursorColor = NeonCyan
            )
        )
    }
}

@Composable
fun TimerPreviewRow(
    durationSeconds: Int,
    onDelete: () -> Unit
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeText = if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"

    Card(
        colors = CardDefaults.cardColors(containerColor = NeonCyan.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = NeonCyan.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "REST",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove timer",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatTimeToInput(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun parseTimeInput(input: String): Int {
    val parts = input.split(":")
    return if (parts.size == 2) {
        (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    } else {
        input.toIntOrNull() ?: 0
    }
}

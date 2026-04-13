package com.example.gymbuddy.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.domain.model.RoutineExercise
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(48.dp))
                    Text("Muscle", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(60.dp))
                    Text("Sets", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text("Reps", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(56.dp))
                    Spacer(modifier = Modifier.width(32.dp))
                }
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
                RoutineExerciseRow(
                    exercise = exercise,
                    onUpdateSets = { viewModel.updateExerciseSets(index, it) },
                    onUpdateReps = { viewModel.updateExerciseReps(index, it) },
                    onUpdateBodyPart = { viewModel.updateExerciseBodyPart(index, it) },
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
}

@Composable
fun RoutineExerciseRow(
    exercise: RoutineExercise,
    onUpdateSets: (Int) -> Unit,
    onUpdateReps: (String) -> Unit,
    onUpdateBodyPart: (String) -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var setsText by remember(exercise.targetSets) { mutableStateOf(exercise.targetSets.toString()) }
    var repsText by remember(exercise.targetReps) { mutableStateOf(exercise.targetReps) }

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
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(36.dp)
                    .background(getSetTypeColor(exercise.setType).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = exercise.exercise.targetMuscle.take(3).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = getSetTypeColor(exercise.setType)
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core").forEach { muscle ->
                        DropdownMenuItem(
                            text = { Text(muscle) },
                            onClick = {
                                onUpdateBodyPart(muscle)
                                showMenu = false
                            }
                        )
                    }
                    HorizontalDivider()
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

            Text(
                text = exercise.exercise.targetMuscle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(60.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(DarkSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, TextTertiary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = setsText,
                    onValueChange = { newValue ->
                        setsText = newValue
                        newValue.toIntOrNull()?.let { onUpdateSets(it) }
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
            }

            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(36.dp)
                    .background(DarkSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, TextTertiary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = repsText,
                    onValueChange = { newValue ->
                        repsText = newValue
                        onUpdateReps(newValue)
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
            }

            Text(
                text = exercise.exercise.name.take(3),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

@Composable
private fun getSetTypeColor(setType: SetType): androidx.compose.ui.graphics.Color {
    return when (setType) {
        SetType.NORMAL -> NeonTeal
        SetType.WORK -> NeonCyan
        SetType.WARMUP -> WarningOrange
        SetType.DROP -> NeonPurple
        SetType.FAILURE -> NeonPink
    }
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
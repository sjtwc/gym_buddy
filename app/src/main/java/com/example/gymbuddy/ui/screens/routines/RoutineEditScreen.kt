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
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.RoutineExercise as DomainRoutineExercise
import com.example.gymbuddy.domain.model.RoutineSetData
import com.example.gymbuddy.domain.model.SetType
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
    var editingExerciseIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(routineId) {
        if (routineId > 0) {
            viewModel.loadRoutine(routineId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
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
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.saveRoutine() }) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = NeonTeal)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RoutineNameInput(
                    name = uiState.routineName,
                    onNameChange = { viewModel.updateRoutineName(it) }
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
                    EmptyRoutineCard(onAddClick = { showExercisePicker = true })
                }
            }

            itemsIndexed(uiState.exercises) { index, exercise ->
                RoutineExerciseCard(
                    exercise = exercise,
                    onUpdateSets = { sets -> viewModel.updateExerciseSets(index, sets) },
                    onRemove = { viewModel.removeExercise(index) },
                    onSetBodyFocus = { bodyFocus -> viewModel.updateExerciseBodyFocus(index, bodyFocus) }
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
                text = "SAVE ROUTINE",
                color = DarkBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
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
}

@Composable
private fun RoutineNameInput(
    name: String,
    onNameChange: (String) -> Unit
) {
    var nameText by remember { mutableStateOf(name) }

    LaunchedEffect(name) {
        nameText = name
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, TextTertiary, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = nameText,
            onValueChange = {
                nameText = it
                onNameChange(it)
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = TextPrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (nameText.isEmpty()) {
            Text(
                text = "Routine Name",
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyRoutineCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No exercises added",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
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

@Composable
private fun RoutineExerciseCard(
    exercise: DomainRoutineExercise,
    onUpdateSets: (List<RoutineSetData>) -> Unit,
    onRemove: () -> Unit,
    onSetBodyFocus: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showBodyFocusDialog by remember { mutableStateOf(false) }

    val sets = remember(exercise) {
        exercise.sets.ifEmpty {
            List(exercise.targetSets) { i ->
                RoutineSetData(
                    setNumber = i + 1,
                    reps = exercise.targetReps,
                    weight = null,
                    setType = SetType.NORMAL
                )
            }
        }
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (exercise.bodyFocus.isNotEmpty()) {
                        Text(
                            text = exercise.bodyFocus,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonTeal
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Body Focus") },
                            onClick = {
                                showMenu = false
                                showBodyFocusDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Sets") },
                            onClick = {
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(48.dp))
                Text("Reps", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                Text("Weight", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(72.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            sets.forEachIndexed { index, set ->
                RoutineSetRow(
                    set = set,
                    onUpdateSet = { updatedSet ->
                        val newSets = sets.toMutableList().apply {
                            this[index] = updatedSet
                        }
                        onUpdateSets(newSets)
                    }
                )
                if (index < sets.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    if (showBodyFocusDialog) {
        BodyFocusDialog(
            currentFocus = exercise.bodyFocus,
            onDismiss = { showBodyFocusDialog = false },
            onSelect = { focus ->
                onSetBodyFocus(focus)
                showBodyFocusDialog = false
            }
        )
    }
}

@Composable
private fun RoutineSetRow(
    set: RoutineSetData,
    onUpdateSet: (RoutineSetData) -> Unit
) {
    var repsText by remember(set.reps) { mutableStateOf(set.reps) }
    var weightText by remember(set.weight) { mutableStateOf(set.weight?.toString() ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoutineSetTypeButton(
            setType = set.setType,
            setNumber = set.setNumber,
            onToggle = { onUpdateSet(set.copy(setType = it)) },
            modifier = Modifier.width(48.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
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
                Text("Reps", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }

        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp)
                .background(DarkSurface, RoundedCornerShape(4.dp))
                .border(1.dp, TextTertiary, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = weightText,
                onValueChange = { newValue ->
                    weightText = newValue
                    newValue.toFloatOrNull()?.let { onUpdateSet(set.copy(weight = it)) }
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
                Text("kg", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RoutineSetTypeButton(
    setType: SetType,
    setNumber: Int,
    onToggle: (SetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    val displayText = if (setType == SetType.NORMAL) setNumber.toString() else setType.abbreviation
    val backgroundColor = when (setType) {
        SetType.NORMAL -> DarkSurfaceElevated
        SetType.WORK -> NeonTeal.copy(alpha = 0.2f)
        SetType.WARMUP -> WarningOrange.copy(alpha = 0.2f)
        SetType.DROP -> NeonCyan.copy(alpha = 0.2f)
        SetType.FAILURE -> NeonPurple.copy(alpha = 0.2f)
    }
    val textColor = when (setType) {
        SetType.NORMAL -> TextPrimary
        SetType.WORK -> NeonTeal
        SetType.WARMUP -> WarningOrange
        SetType.DROP -> NeonCyan
        SetType.FAILURE -> NeonPurple
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { showDropdown = true },
            shape = RoundedCornerShape(4.dp),
            color = backgroundColor
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = { Text("Normal") },
                onClick = {
                    onToggle(SetType.NORMAL)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Warmup") },
                onClick = {
                    onToggle(SetType.WARMUP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Drop Set") },
                onClick = {
                    onToggle(SetType.DROP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Failure") },
                onClick = {
                    onToggle(SetType.FAILURE)
                    showDropdown = false
                }
            )
        }
    }
}

@Composable
private fun BodyFocusDialog(
    currentFocus: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var selected by remember { mutableStateOf(currentFocus) }

    val bodyParts = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Body Focus", color = TextPrimary)
        },
        text = {
            Column {
                bodyParts.forEach { part ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = part }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == part,
                            onClick = { selected = part },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(part, color = TextPrimary)
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
private fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Select Exercise", color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Use the Exercise Library to add exercises",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

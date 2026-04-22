package com.example.gymbuddy.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.ScheduledWorkout
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*
import java.util.Calendar

@Composable
fun WorkoutScreen(
    navController: NavController,
    sessionManager: WorkoutSessionManager,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Start Workout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Start Workout Button
        Button(
            onClick = { viewModel.startQuickWorkout(sessionManager) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "START QUICK WORKOUT",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Schedule Section
        WeeklyScheduleSection(
            scheduledWorkouts = uiState.scheduledWorkouts,
            suggestedSchedule = uiState.suggestedSchedule,
            routines = uiState.routines,
            isExpanded = "weeklySchedule" in uiState.expandedSections,
            dayNames = dayNames,
            onToggleExpand = { viewModel.toggleSection("weeklySchedule") },
            onGenerateSuggested = { viewModel.generateSuggestedSchedule() },
            onApplySuggested = { viewModel.applySuggestedSchedule() },
            onClearSchedule = { viewModel.clearWeekSchedule() },
            onSaveDay = { dayOfWeek, routineId, isRestDay ->
                viewModel.saveScheduledWorkout(dayOfWeek, routineId, isRestDay)
            },
            onStartWorkout = { routine ->
                viewModel.startWorkoutWithRoutine(routine, sessionManager)
            },
            getRoutineById = { id -> viewModel.getRoutineById(id) },
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Routines",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = { navController.navigate(Screen.CreateRoutine.route) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Routine",
                    tint = NeonTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.routines.isEmpty()) {
            EmptyRoutinesState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.routines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onStart = { viewModel.startWorkoutWithRoutine(routine, sessionManager) },
                        onEdit = { navController.navigate(Screen.CreateRoutine.createRoute(routine.id)) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyScheduleSection(
    scheduledWorkouts: List<ScheduledWorkout>,
    suggestedSchedule: List<ScheduledWorkout>,
    routines: List<Routine>,
    isExpanded: Boolean,
    dayNames: List<String>,
    onToggleExpand: () -> Unit,
    onGenerateSuggested: () -> Unit,
    onApplySuggested: () -> Unit,
    onClearSchedule: () -> Unit,
    onSaveDay: (Int, Long?, Boolean) -> Unit,
    onStartWorkout: (Routine) -> Unit,
    getRoutineById: suspend (Long) -> Routine?,
    viewModel: WorkoutViewModel
) {
    var showDayPicker by remember { mutableStateOf<Int?>(null) }
    var selectedRoutine by remember { mutableStateOf<Routine?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = NeonTeal
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onGenerateSuggested,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Auto-Suggest", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = onClearSchedule,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TextTertiary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (suggestedSchedule.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NeonTeal.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Suggested Schedule",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonTeal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                suggestedSchedule.forEach { scheduled ->
                                    val dayName = dayNames.getOrNull(scheduled.dayOfWeek - 1) ?: ""
                                    val routineName = routines.find { it.id == scheduled.routineId }?.name
                                    Text(
                                        text = "$dayName: ${if (scheduled.isRestDay) "Rest" else routineName ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onApplySuggested,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Apply Schedule", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    val currentDayOfWeek = getCurrentDayOfWeek()
                    val closestThreeDays = (0..2).map { offset ->
                        ((currentDayOfWeek - 1 + offset) % 7) + 1
                    }
                    val filteredWorkouts = scheduledWorkouts.filter { it.dayOfWeek in closestThreeDays }

                    filteredWorkouts.forEach { scheduled ->
                        val dayName = dayNames.getOrNull(scheduled.dayOfWeek - 1) ?: "Day ${scheduled.dayOfWeek}"
                        val routineName = routines.find { it.id == scheduled.routineId }?.name
                        val isToday = scheduled.dayOfWeek == currentDayOfWeek

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDayPicker = scheduled.dayOfWeek }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isToday) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = NeonTeal.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Today",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonTeal
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (scheduled.isRestDay) "Rest Day" else routineName ?: "Not scheduled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (scheduled.isRestDay) TextTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!scheduled.isRestDay && scheduled.routineId != null) {
                                val routine = routines.find { it.id == scheduled.routineId }
                                if (routine != null) {
                                    TextButton(
                                        onClick = { onStartWorkout(routine) },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Start", color = NeonTeal)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDayPicker != null) {
        DayPickerDialog(
            dayOfWeek = showDayPicker!!,
            dayName = dayNames.getOrNull(showDayPicker!! - 1) ?: "Day ${showDayPicker}",
            routines = routines,
            currentRoutineId = scheduledWorkouts.find { it.dayOfWeek == showDayPicker }?.routineId,
            isRestDay = scheduledWorkouts.find { it.dayOfWeek == showDayPicker }?.isRestDay ?: false,
            onDismiss = { showDayPicker = null },
            onSelect = { routineId, isRestDay ->
                onSaveDay(showDayPicker!!, routineId, isRestDay)
                showDayPicker = null
            }
        )
    }
}

private fun getCurrentDayOfWeek(): Int {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }
}

@Composable
fun DayPickerDialog(
    dayOfWeek: Int,
    dayName: String,
    routines: List<Routine>,
    currentRoutineId: Long?,
    isRestDay: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Long?, Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$dayName - Select Workout") },
        text = {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null, true) }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = isRestDay && currentRoutineId == null,
                            onClick = { onSelect(null, true) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rest Day", style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
                items(routines) { routine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(routine.id, false) }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = routine.id == currentRoutineId && !isRestDay,
                            onClick = { onSelect(routine.id, false) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(routine.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                routine.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
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
fun RoutineCard(
    routine: Routine,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Routine") },
            text = { Text("Are you sure you want to delete \"${routine.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextSecondary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (routine.type) {
                    "Push" -> NeonCyan.copy(alpha = 0.2f)
                    "Pull" -> NeonPurple.copy(alpha = 0.2f)
                    "Legs" -> NeonTeal.copy(alpha = 0.2f)
                    else -> TextTertiary.copy(alpha = 0.2f)
                }
            ) {
                Text(
                    text = routine.type,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (routine.type) {
                        "Push" -> NeonCyan
                        "Pull" -> NeonPurple
                        "Legs" -> NeonTeal
                        else -> TextTertiary
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Est. ${routine.estimatedMinutes} min",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyRoutinesState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📋", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No routines available",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
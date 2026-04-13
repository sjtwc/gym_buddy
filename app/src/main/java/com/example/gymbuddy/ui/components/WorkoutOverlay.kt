package com.example.gymbuddy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymbuddy.domain.model.*
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.screens.exercise.ExercisePickerPage
import com.example.gymbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutOverlay(
    modifier: Modifier = Modifier,
    sessionManager: WorkoutSessionManager,
    isVisible: Boolean = false,
    onExpandToggle: () -> Unit = {},
    onCollapse: () -> Unit = {}
) {
    val isExpanded by sessionManager.isExpanded.collectAsState()
    val currentSession by sessionManager.currentSession.collectAsState()
    val elapsedTime by sessionManager.elapsedTime.collectAsState()
    val isResting by sessionManager.isResting.collectAsState()
    val restTime by sessionManager.restTimeRemaining.collectAsState()
    val isTimerMinimized by sessionManager.isTimerMinimized.collectAsState()
    val timerTotalTime by sessionManager.timerTotalTime.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isExpanded) Modifier.fillMaxHeight()
                    else Modifier.height(56.dp)
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 20 && isExpanded) {
                            onCollapse()
                        }
                    }
                },
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(
                topStart = if (isExpanded) 0.dp else 16.dp,
                topEnd = if (isExpanded) 0.dp else 16.dp,
                bottomStart = if (isExpanded) 0.dp else 16.dp,
                bottomEnd = if (isExpanded) 0.dp else 16.dp
            )
        ) {
            if (isExpanded) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ExpandedOverlay(
                        session = currentSession,
                        sessionManager = sessionManager,
                        elapsedTime = elapsedTime,
                        isResting = isResting,
                        restTime = restTime,
                        isTimerMinimized = isTimerMinimized,
                        timerTotalTime = timerTotalTime,
                        onCollapse = onCollapse,
                        onAddExercise = { sessionManager.showExercisePicker() },
                        onRemoveExercise = { sessionManager.removeExercise(it) },
                        onSwapExercise = { sessionManager.swapExercise(it) },
                        onAddSet = { sessionManager.addSet(it) },
                        onUpdateSet = { exerciseIndex, setIndex, set -> sessionManager.updateSet(exerciseIndex, setIndex, set) },
                        onDeleteSet = { exerciseIndex, setIndex -> sessionManager.deleteSet(exerciseIndex, setIndex) },
                        onToggleSetType = { exerciseIndex, setIndex, type -> sessionManager.toggleSetTypeForSet(exerciseIndex, setIndex, type) },
                        onCompleteSet = { exerciseIndex, setIndex -> sessionManager.completeSet(exerciseIndex, setIndex) },
                        onAddRestTimer = { type, duration -> sessionManager.addRestTimer(type, duration) },
                        onUpdateRestTimer = { exIdx, type, duration -> sessionManager.updateRestTimer(exIdx, type, duration) },
                        onDeleteRestTimer = { type -> sessionManager.deleteRestTimer(type) },
                        onFinishWorkout = { sessionManager.finishWorkout() },
                        onCancelWorkout = { sessionManager.cancelWorkout() },
                        onUpdateWorkoutName = { sessionManager.updateWorkoutName(it) },
                        onEditStartTime = { sessionManager.updateStartTime(it) },
                        onEnableRestTimer = { sessionManager.enableRestTimer(it) },
                        onMinimizeTimer = { sessionManager.minimizeTimer() },
                        onRestoreTimer = { sessionManager.restoreTimer() },
                        onAdjustTimerTime = { sessionManager.adjustTimerTime(it) }
                    )
                    
                    if (isResting && !isTimerMinimized) {
                        RestTimerPopup(
                            remainingSeconds = restTime,
                            totalSeconds = timerTotalTime,
                            onMinimize = { sessionManager.minimizeTimer() },
                            onAdjustTime = { sessionManager.adjustTimerTime(it) },
                            onClose = { sessionManager.adjustTimerTime(-restTime) },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            } else {
                CollapsedOverlayBar(
                    session = currentSession,
                    elapsedTime = elapsedTime,
                    onExpand = onExpandToggle
                )
            }
        }
    }
}

@Composable
fun CollapsedOverlayBar(
    session: WorkoutSession?,
    elapsedTime: Long,
    onExpand: () -> Unit
) {
    val exerciseName = session?.currentExercise?.exercise?.name ?: "Starting..."
    val setProgress = if (session != null && session.exercises.isNotEmpty()) {
        "${session.currentExerciseIndex + 1}/${session.exercises.size} exercises"
    } else {
        "..."
    }

    val elapsedStr = formatTime(elapsedTime.toInt())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onExpand() },
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🏋️", fontSize = 20.sp)
                Column {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$setProgress • ⏱ $elapsedStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand",
                tint = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedOverlay(
    session: WorkoutSession?,
    sessionManager: WorkoutSessionManager,
    elapsedTime: Long,
    isResting: Boolean,
    restTime: Int,
    isTimerMinimized: Boolean,
    timerTotalTime: Int,
    onCollapse: () -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onSwapExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onUpdateSet: (Int, Int, WorkoutSetData) -> Unit,
    onDeleteSet: (Int, Int) -> Unit,
    onToggleSetType: (Int, Int, SetType) -> Unit,
    onCompleteSet: (Int, Int) -> Unit,
    onAddRestTimer: (SetType, Int) -> Unit,
    onUpdateRestTimer: (Int, SetType, Int) -> Unit,
    onDeleteRestTimer: (SetType) -> Unit,
    onFinishWorkout: () -> Unit,
    onCancelWorkout: () -> Unit,
    onUpdateWorkoutName: (String) -> Unit,
    onEditStartTime: (Long) -> Unit,
    onEnableRestTimer: (Int) -> Unit,
    onMinimizeTimer: () -> Unit,
    onRestoreTimer: () -> Unit,
    onAdjustTimerTime: (Int) -> Unit
) {
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(TextTertiary, RoundedCornerShape(2.dp))
                    .clickable { onCollapse() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HeaderSection(
            session = session,
            elapsedTime = elapsedTime,
            onFinishWorkout = onFinishWorkout,
            onShowSettingsMenu = { showSettingsMenu = true }
        )

        if (showNameDialog) {
            EditNameDialog(
                currentName = session?.workoutName ?: "",
                onDismiss = { showNameDialog = false },
                onConfirm = { newName ->
                    onUpdateWorkoutName(newName)
                    showNameDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (session?.exercises?.isEmpty() != false) {
            EmptyExerciseState()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(session.exercises) { exerciseIndex, exerciseSession ->
                    ExerciseCard(
                        exerciseSession = exerciseSession,
                        exerciseIndex = exerciseIndex,
                        onAddSet = { onAddSet(exerciseIndex) },
                        onUpdateSet = { setIndex, set -> onUpdateSet(exerciseIndex, setIndex, set) },
                        onDeleteSet = { setIndex -> onDeleteSet(exerciseIndex, setIndex) },
                        onToggleSetType = { setIdx, type -> onToggleSetType(exerciseIndex, setIdx, type) },
                        onCompleteSet = onCompleteSet,
                        onRemoveExercise = { onRemoveExercise(exerciseIndex) },
                        onSwapExercise = { onSwapExercise(exerciseIndex) },
                        onAddRestTimer = { type, duration -> onAddRestTimer(type, duration) },
                        onUpdateRestTimer = { _, type, duration -> onUpdateRestTimer(exerciseIndex, type, duration) },
                        onDeleteRestTimer = { type -> onDeleteRestTimer(type) },
                        isResting = isResting,
                        isTimerMinimized = isTimerMinimized,
                        restTime = restTime,
                        timerTotalTime = timerTotalTime,
                        onEnableRestTimer = onEnableRestTimer,
                        onRestoreTimer = onRestoreTimer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ActionButtonsRow(
            onAddExercise = { showExercisePicker = true },
            onCancelWorkout = onCancelWorkout
        )
    }

    if (showExercisePicker) {
        ExercisePickerPage(
            onDismiss = { showExercisePicker = false },
            onAddExercises = { selected ->
                selected.forEach { exercise ->
                    sessionManager.addExercise(exercise)
                }
                showExercisePicker = false
            }
        )
    }
}

@Composable
fun HeaderSection(
    session: WorkoutSession?,
    elapsedTime: Long,
    onFinishWorkout: () -> Unit,
    onShowSettingsMenu: () -> Unit
) {
    val elapsedStr = formatTime(elapsedTime.toInt())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session?.workoutName ?: "Workout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = session?.workoutDateFormatted ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = session?.workoutStartTimeFormatted ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Row {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Name") },
                        onClick = {
                            showMenu = false
                            onShowSettingsMenu()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextSecondary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Start Time") },
                        onClick = {
                            showMenu = false
                            onShowSettingsMenu()
                        },
                        leadingIcon = { Icon(Icons.Default.Schedule, null, tint = TextSecondary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Photo") },
                        onClick = {
                            showMenu = false
                            onShowSettingsMenu()
                        },
                        leadingIcon = { Icon(Icons.Default.PhotoCamera, null, tint = TextSecondary) }
                    )
                }
            }
            Button(
                onClick = onFinishWorkout,
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Finish", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Workout Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout Name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
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
fun EmptyExerciseState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🏋️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No exercises yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use the Add Exercise button below",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
    }
}

@Composable
fun ExerciseCard(
    exerciseSession: WorkoutExerciseSession,
    exerciseIndex: Int,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, WorkoutSetData) -> Unit,
    onDeleteSet: (Int) -> Unit,
    onToggleSetType: (Int, SetType) -> Unit,
    onCompleteSet: (Int, Int) -> Unit,
    onRemoveExercise: () -> Unit,
    onSwapExercise: () -> Unit,
    onAddRestTimer: (SetType, Int) -> Unit,
    onUpdateRestTimer: (Int, SetType, Int) -> Unit,
    onDeleteRestTimer: (SetType) -> Unit,
    isResting: Boolean = false,
    isTimerMinimized: Boolean = false,
    restTime: Int = 0,
    timerTotalTime: Int = 0,
    onEnableRestTimer: (Int) -> Unit = {},
    onRestoreTimer: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

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
                    text = exerciseSession.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = TextSecondary)
                    }
                    if (showMenu) {
                        val hasAnyTimer = exerciseSession.restTimers.any { it.durationSeconds > 0 }
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove Exercise") },
                                onClick = {
                                    showMenu = false
                                    onRemoveExercise()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Swap Exercise") },
                                onClick = {
                                    showMenu = false
                                    onSwapExercise()
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(if (exerciseSession.isRestTimerEnabled) "Timer Enabled" else "Add Rest Timer")
                                },
                                onClick = {
                                    showMenu = false
                                    onEnableRestTimer(exerciseIndex)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SetHeader()

            exerciseSession.sets.forEachIndexed { setIndex, set ->
                val previousSetCompleted = if (setIndex == 0) true else exerciseSession.sets[setIndex - 1].isCompleted
                SwipeableSetRow(
                    onDelete = { onDeleteSet(setIndex) }
                ) {
                    SetRow(
                        set = set,
                        previousData = if (set.setType == SetType.WORK) exerciseSession.previousData else null,
                        onUpdateSet = { newSet -> onUpdateSet(setIndex, newSet) },
                        onDeleteSet = { onDeleteSet(setIndex) },
                        onToggleSetType = { type -> onToggleSetType(setIndex, type) },
                        onCompleteSet = onCompleteSet,
                        setIndex = setIndex,
                        exerciseIndex = exerciseIndex,
                        isEnabled = previousSetCompleted
                    )
                }
                
                if (exerciseSession.isRestTimerEnabled) {
                    val currentSet = exerciseSession.sets[setIndex]
                    val timerDuration = exerciseSession.restTimers.find { 
                        (it.type == currentSet.setType || it.type.abbreviation == currentSet.setType.abbreviation) 
                    }?.durationSeconds ?: 0
                    
                    if (timerDuration > 0) {
                        when {
                            isResting && isTimerMinimized -> {
                                TimerRow(
                                    remainingSeconds = restTime,
                                    totalSeconds = timerTotalTime,
                                    onClick = onRestoreTimer
                                )
                            }
                            isResting && !isTimerMinimized -> {
                            }
                            else -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = NeonCyan.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatTime(timerDuration),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonCyan.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, tint = NeonTeal)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set", color = NeonTeal)
            }
        }
    }

}

@Composable
fun SetHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Set", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.8f))
        Text("Previous", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1.2f))
        Text("Weight", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text("Reps", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.8f))
        Text("✓", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.5f))
    }
}

@Composable
fun SwipeableSetRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    
    val stage1Threshold = -50f
    val stage2Threshold = -120f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < stage2Threshold) {
                            isDeleting = true
                            onDelete()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (offsetX < stage1Threshold) ErrorRed else DarkSurfaceElevated)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (offsetX < stage1Threshold) {
                Text(
                    text = "Delete",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .background(DarkSurface)
        ) {
            content()
        }
    }
}

@Composable
fun SetRow(
    set: WorkoutSetData,
    previousData: Pair<Double, Int>?,
    onUpdateSet: (WorkoutSetData) -> Unit,
    onDeleteSet: () -> Unit,
    onToggleSetType: (SetType) -> Unit,
    onCompleteSet: (Int, Int) -> Unit,
    setIndex: Int = 0,
    exerciseIndex: Int = 0,
    isEnabled: Boolean = true
) {
    var weightText by remember(set.weight) { mutableStateOf(set.weight?.toString() ?: "") }
    var repsText by remember(set.reps) { mutableStateOf(set.reps?.toString() ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetTypeButton(
            setType = set.setType,
            setNumber = set.setNumber,
            onToggle = onToggleSetType,
            modifier = Modifier.weight(0.8f)
        )

        Text(
            text = if (previousData != null) "${previousData.first}kg x ${previousData.second}" else "-",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier
                .weight(1.2f)
                .clickable(enabled = false) { }
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = {
                weightText = it
                it.toDoubleOrNull()?.let { w -> onUpdateSet(set.copy(weight = w)) }
            },
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonTeal,
                unfocusedBorderColor = TextTertiary
            )
        )

        OutlinedTextField(
            value = repsText,
            onValueChange = {
                repsText = it
                it.toIntOrNull()?.let { r -> onUpdateSet(set.copy(reps = r)) }
            },
            modifier = Modifier.weight(0.8f),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonTeal,
                unfocusedBorderColor = TextTertiary
            )
        )

        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = { if (isEnabled) {
                val updatedSet = set.copy(isCompleted = it)
                onUpdateSet(updatedSet)
                if (it) onCompleteSet(exerciseIndex, setIndex)
            } },
            enabled = isEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = NeonTeal,
                uncheckedColor = TextTertiary,
                disabledCheckedColor = TextTertiary.copy(alpha = 0.5f),
                disabledUncheckedColor = TextTertiary.copy(alpha = 0.3f)
            ),
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
fun SetTypeButton(
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
                text = { Text("Normal (${setNumber})") },
                onClick = {
                    onToggle(SetType.NORMAL)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Warmup Set", color = WarningOrange) },
                onClick = {
                    onToggle(SetType.WARMUP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Drop Set", color = NeonCyan) },
                onClick = {
                    onToggle(SetType.DROP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Failure Set", color = NeonPurple) },
                onClick = {
                    onToggle(SetType.FAILURE)
                    showDropdown = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestTimerConfigRow(
    timer: RestTimer,
    onUpdate: (Int) -> Unit,
    onDelete: () -> Unit,
    isConfigured: Boolean = false
) {
    val timerLabel = when (timer.type) {
        SetType.WARMUP -> "Warmup Set Timer"
        SetType.WORK -> "Work Set Timer"
        SetType.DROP -> "Drop Set Timer"
        SetType.NORMAL -> "Timer"
        SetType.FAILURE -> "Failure Set Timer"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timerLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = NeonCyan
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(timer.durationSeconds),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                modifier = Modifier.clickable { onUpdate(timer.durationSeconds + 30) }
            )
            Text(
                text = if (isConfigured) "Edit" else "Add",
                style = MaterialTheme.typography.bodySmall,
                color = if (isConfigured) NeonTeal else TextSecondary,
                modifier = Modifier.clickable { 
                    if (!isConfigured) onUpdate(timer.durationSeconds) 
                }
            )
        }
    }
}

@Composable
fun ActionButtonsRow(
    onAddExercise: () -> Unit,
    onCancelWorkout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onAddExercise,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonTeal)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Exercise")
        }
        OutlinedButton(
            onClick = onCancelWorkout,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange)
        ) {
            Text("Cancel Training", color = WarningOrange)
        }
    }
}

@Composable
fun RestTimerCard(
    restTime: Int,
    onAddTime: (Int) -> Unit,
    onSkip: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Rest Timer", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
            Text(
                text = formatTime(restTime),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onAddTime(-15) }) {
                    Text("-15s", color = TextSecondary)
                }
                TextButton(onClick = { onAddTime(15) }) {
                    Text("+15s", color = NeonTeal)
                }
                TextButton(onClick = onSkip) {
                    Text("Skip", color = WarningOrange)
                }
            }
        }
    }
}

@Composable
fun RestTimerDialog(
    currentTime: Int,
    onDismiss: () -> Unit,
    onConfirm: (SetType, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(SetType.WORK) }
    var timerText by remember { mutableStateOf(formatTime(currentTime)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rest Timer") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SetType.entries.forEach { type ->
                        SetTypeChip(
                            type = type,
                            isSelected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = timerText,
                    onValueChange = { timerText = it },
                    label = { Text("Duration (mm:ss)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parts = timerText.split(":")
                val seconds = if (parts.size == 2) {
                    (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
                } else {
                    timerText.toIntOrNull() ?: 90
                }
                onConfirm(selectedType, seconds)
            }) {
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
fun SetTypeChip(
    type: SetType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        when (type) {
            SetType.NORMAL -> NeonTeal
            SetType.WORK -> NeonTeal
            SetType.WARMUP -> WarningOrange
            SetType.DROP -> NeonCyan
            SetType.FAILURE -> NeonPurple
        }
    } else TextTertiary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor.copy(alpha = if (isSelected) 1f else 0.2f)
    ) {
        Text(
            text = type.abbreviation,
            color = if (isSelected) DarkBackground else TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onSelectExercises: (List<Exercise>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedExercises by remember { mutableStateOf(setOf<Exercise>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Exercises") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select multiple exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelectExercises(selectedExercises.toList()) }) {
                Text("Add (${selectedExercises.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
package com.example.gymbuddy.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.data.repository.DailyVolume
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                MuscleFocusCard(
                    muscleFocusList = uiState.muscleFocusList,
                    onGoalClick = { muscle, goal ->
                        viewModel.updateMuscleGoal(muscle, goal)
                    }
                )
            }
            item {
                VolumeChartCard(dailyVolumes = uiState.dailyVolumes)
            }
            item {
                PersonalRecordsCard(personalRecords = uiState.personalRecords)
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Workout History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            if (uiState.workouts.isEmpty()) {
                item {
                    EmptyWorkoutHistoryCard()
                }
            } else {
                items(uiState.workouts) { workout ->
                    WorkoutHistoryCard(
                        workout = workout,
                        onClick = { navController.navigate(Screen.Exercise.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleFocusCard(
    muscleFocusList: List<MuscleFocus>,
    onGoalClick: (String, Float) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedMuscle by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Muscle Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = {
                        selectedMuscle = ""
                        showGoalDialog = true
                    }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Set Goals", tint = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(muscleFocusList) { muscleFocus ->
                    MuscleProgressItem(
                        muscleFocus = muscleFocus,
                        onClick = {
                            selectedMuscle = muscleFocus.muscleGroup
                            showGoalDialog = true
                        }
                    )
                }
            }
        }
    }
    
    if (showGoalDialog) {
        MuscleGoalDialog(
            muscleFocusList = muscleFocusList,
            selectedMuscle = selectedMuscle,
            onDismiss = { showGoalDialog = false },
            onSave = { muscle, goal ->
                onGoalClick(muscle, goal)
            }
        )
    }
}

@Composable
fun MuscleProgressItem(
    muscleFocus: MuscleFocus,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = muscleFocus.muscleGroup,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { muscleFocus.progress },
                modifier = Modifier.size(48.dp),
                color = if (muscleFocus.progress >= 1f) SuccessGreen else NeonTeal,
                trackColor = ProgressBarBackground,
                strokeWidth = 4.dp
            )
            Text(
                text = "${(muscleFocus.progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${muscleFocus.currentVolume.toInt()} kg",
            style = MaterialTheme.typography.labelSmall,
            color = NeonTeal
        )
    }
}

@Composable
fun MuscleGoalDialog(
    muscleFocusList: List<MuscleFocus>,
    selectedMuscle: String,
    onDismiss: () -> Unit,
    onSave: (String, Float) -> Unit
) {
    var selected by remember { mutableStateOf(selectedMuscle.ifEmpty { "Chest" }) }
    var goalText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Set Weekly Volume Goal", color = TextPrimary)
        },
        text = {
            Column {
                Text("Select muscle group:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(muscleFocusList) { muscle ->
                        val isSelected = selected == muscle.muscleGroup
                        Surface(
                            color = if (isSelected) NeonTeal.copy(alpha = 0.2f) else DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selected = muscle.muscleGroup }
                                .padding(4.dp),
                            border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
                        ) {
                            Text(
                                text = muscle.muscleGroup,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) NeonTeal else TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Goal (kg/week)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextTertiary,
                        focusedLabelColor = NeonTeal,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = NeonTeal
                    )
                )
                
                if (goalText.isNotEmpty()) {
                    val goal = goalText.toFloatOrNull() ?: 0f
                    val current = muscleFocusList.find { it.muscleGroup == selected }?.currentVolume ?: 0f
                    val progress = if (goal > 0) (current / goal * 100).roundToInt() else 0
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current: ${current.toInt()} kg (${progress}% of goal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    goalText.toFloatOrNull()?.let { goal ->
                        onSave(selected, goal)
                    }
                    onDismiss()
                }
            ) {
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
fun VolumeChartCard(dailyVolumes: List<DailyVolume>) {
    var selectedPoint by remember { mutableStateOf<DailyVolume?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Volume (Last 10 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                VolumeLineChart(
                    dailyVolumes = dailyVolumes,
                    onPointClick = { selectedPoint = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            selectedPoint?.let { point ->
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                Text(
                    text = "${dateFormat.format(Date(point.date))}: ${point.volume.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonTeal,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            val totalVolume = dailyVolumes.sumOf { it.volume.toDouble() }.toFloat()
            val avgVolume = if (dailyVolumes.isNotEmpty()) totalVolume / dailyVolumes.size else 0f
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${totalVolume.toInt()} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonTeal)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Daily Avg", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${avgVolume.toInt()} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = XpGold)
                }
            }
        }
    }
}

@Composable
fun VolumeLineChart(
    dailyVolumes: List<DailyVolume>,
    onPointClick: (DailyVolume) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVolume = dailyVolumes.maxOfOrNull { it.volume }?.takeIf { it > 0 } ?: 1f
    
    Canvas(
        modifier = modifier.clickable {
            if (dailyVolumes.isNotEmpty()) {
                onPointClick(dailyVolumes.last())
            }
        }
    ) {
        val width = this.size.width
        val height = this.size.height
        val padding = 16.dp.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        if (dailyVolumes.isEmpty()) return@Canvas
        
        val points = dailyVolumes.mapIndexed { index, dv ->
            val x = padding + (index.toFloat() / (dailyVolumes.size - 1).coerceAtLeast(1)) * chartWidth
            val y = padding + chartHeight - (dv.volume / maxVolume) * chartHeight
            Offset(x, y)
        }
        
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
            drawPath(path, NeonTeal, style = Stroke(width = 3.dp.toPx()))
            
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = NeonTeal,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = DarkSurfaceElevated,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun PersonalRecordsCard(personalRecords: List<PersonalRecordItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Personal Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (personalRecords.isEmpty()) {
                Text("No records yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                personalRecords.forEach { pr ->
                    PRItem(pr = pr)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PRItem(pr: PersonalRecordItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(XpGold.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🏆", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pr.exerciseName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(pr.date)),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${pr.weight.toInt()} kg × ${pr.reps}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = XpGold
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SuccessGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = pr.type,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(workout: Workout, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(workout.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${workout.duration} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonTeal
                )
                if (workout.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Completed",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyWorkoutHistoryCard() {
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
            Text(text = "📋", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No workout history yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Complete a workout to see it here",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}
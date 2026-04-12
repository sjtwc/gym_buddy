package com.example.gymbuddy.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
            // Stats Section
            item {
                MuscleHeatMapCard()
            }
            item {
                VolumeChartCard()
            }
            item {
                PersonalRecordsCard()
            }
            
            // Workout History Section
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

@Composable
fun MuscleHeatMapCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Muscle Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf("Chest" to 85, "Back" to 70, "Legs" to 60).forEach { (muscle, progress) ->
                    MuscleProgressItem(muscle, progress)
                }
            }
        }
    }
}

@Composable
fun MuscleProgressItem(muscle: String, progress: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = muscle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.size(48.dp),
            color = NeonTeal,
            trackColor = ProgressBarBackground,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun VolumeChartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weekly Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("12,500 kg", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = NeonTeal)
            Text("+15% from last week", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
        }
    }
}

@Composable
fun PersonalRecordsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Personal Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            listOf(
                "Bench Press" to "100kg x 5",
                "Squat" to "140kg x 3",
                "Deadlift" to "160kg x 2"
            ).forEach { (exercise, record) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(exercise, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(record, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = XpGold)
                }
            }
        }
    }
}
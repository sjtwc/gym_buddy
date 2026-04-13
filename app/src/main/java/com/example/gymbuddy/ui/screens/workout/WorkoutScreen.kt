package com.example.gymbuddy.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*

@Composable
fun WorkoutScreen(
    navController: NavController,
    sessionManager: WorkoutSessionManager,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                        onStart = { viewModel.startWorkoutWithRoutine(routine, sessionManager) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineCard(routine: Routine, onStart: () -> Unit) {
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
                    maxLines = 1
                )
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
                text = "${routine.exercises.size} exercises • ${routine.estimatedMinutes} min",
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
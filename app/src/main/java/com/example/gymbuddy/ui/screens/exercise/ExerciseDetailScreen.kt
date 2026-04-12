package com.example.gymbuddy.ui.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.ui.theme.*

@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    navController: NavController,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        uiState.exercise?.let { exercise ->
            ExerciseHeader(exercise = exercise, onBack = { navController.popBackStack() })
            ExerciseInstructions(instructions = exercise.instructions)
            TargetMuscles(muscle = exercise.targetMuscle, secondary = exercise.secondaryMuscles)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExerciseHeader(exercise: Exercise, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        
        Text(text = "🏋️", fontSize = 80.sp)
        
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = NeonTeal.copy(alpha = 0.2f)
        ) {
            Text(
                text = exercise.equipmentType,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = NeonTeal
            )
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = exercise.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun ExerciseInstructions(instructions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Instructions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            instructions.forEachIndexed { index, instruction ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "${index + 1}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = instruction, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun TargetMuscles(muscle: String, secondary: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Target Muscles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = NeonTeal.copy(alpha = 0.2f)) {
                Text(text = muscle, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = NeonTeal, fontWeight = FontWeight.Bold)
            }
            if (secondary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Secondary: ${secondary.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}
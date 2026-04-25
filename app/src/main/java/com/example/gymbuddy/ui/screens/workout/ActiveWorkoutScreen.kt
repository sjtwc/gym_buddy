package com.csci3310.gymbuddy.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.csci3310.gymbuddy.ui.theme.*

@Composable
fun ActiveWorkoutScreen(
    navController: NavController
) {
    var selectedExercise by remember { mutableStateOf("Bench Press") }
    var currentSet by remember { mutableStateOf(1) }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("60") }
    var isRestTimerActive by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableStateOf(90) }
    
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
            Text(
                text = "Active Workout",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedExercise,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chest - Barbell",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SetTableHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SetRow(
            setNumber = currentSet,
            previousWeight = "60",
            previousReps = "10",
            reps = reps,
            weight = weight,
            onRepsChange = { reps = it },
            onWeightChange = { weight = it },
            onComplete = {
                currentSet++
                isRestTimerActive = true
            }
        )
        
        if (isRestTimerActive) {
            Spacer(modifier = Modifier.height(16.dp))
            RestTimerDisplay(
                timeRemaining = restTimeRemaining,
                onDismiss = { isRestTimerActive = false }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        PlateCalculatorButton()
    }
}

@Composable
fun SetTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("SET", style = MaterialTheme.typography.bodySmall, color = TextTertiary, modifier = Modifier.width(40.dp))
        Text("PREV", style = MaterialTheme.typography.bodySmall, color = TextTertiary, modifier = Modifier.width(60.dp))
        Text("KG", style = MaterialTheme.typography.bodySmall, color = TextTertiary, modifier = Modifier.width(70.dp))
        Text("REPS", style = MaterialTheme.typography.bodySmall, color = TextTertiary, modifier = Modifier.width(60.dp))
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    previousWeight: String,
    previousReps: String,
    reps: String,
    weight: String,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$setNumber",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = NeonTeal,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = "$previousWeight x $previousReps",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.width(60.dp)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                modifier = Modifier.width(70.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    unfocusedBorderColor = TextTertiary
                )
            )
            OutlinedTextField(
                value = reps,
                onValueChange = onRepsChange,
                modifier = Modifier.width(60.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    unfocusedBorderColor = TextTertiary
                )
            )
            IconButton(onClick = onComplete) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = SuccessGreen)
            }
        }
    }
}

@Composable
fun RestTimerDisplay(timeRemaining: Int, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeonTeal.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rest Timer",
                style = MaterialTheme.typography.titleMedium,
                color = NeonTeal
            )
            Text(
                text = "${timeRemaining / 60}:${(timeRemaining % 60).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Skip", tint = NeonTeal)
            }
        }
    }
}

@Composable
fun PlateCalculatorButton() {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = NeonCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Plate Calculator", color = NeonCyan)
    }
}
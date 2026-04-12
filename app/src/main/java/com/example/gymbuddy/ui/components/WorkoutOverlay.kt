package com.example.gymbuddy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.theme.*

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
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (isExpanded) {
            ExpandedOverlay(
                session = currentSession,
                elapsedTime = elapsedTime,
                isResting = isResting,
                restTime = restTime,
                onCollapse = onCollapse,
                onAddRestTime = { /* TODO */ },
                onSkipRest = { /* TODO */ }
            )
        } else {
            CollapsedOverlayBar(
                session = currentSession,
                elapsedTime = elapsedTime,
                onExpand = onExpandToggle
            )
        }
    }
}

@Composable
fun CollapsedOverlayBar(
    session: com.example.gymbuddy.domain.model.WorkoutSession?,
    elapsedTime: Long,
    onExpand: () -> Unit
) {
    val exerciseName = session?.currentExercise?.name ?: "Starting..."
    val setProgress = if (session != null && session.totalSets > 0) {
        "Set ${session.currentSetIndex + 1}/${session.totalSets}"
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

@Composable
fun ExpandedOverlay(
    session: com.example.gymbuddy.domain.model.WorkoutSession?,
    elapsedTime: Long,
    isResting: Boolean,
    restTime: Int,
    onCollapse: () -> Unit,
    onAddRestTime: (Int) -> Unit,
    onSkipRest: () -> Unit
) {
    val exerciseName = session?.currentExercise?.name ?: "No exercise"
    val setProgress = session?.let {
        "${it.currentSetIndex + 1} of ${it.totalSets}"
    } ?: "0 of 0"
    val previousSet = "60kg x 10" // Placeholder
    
    val elapsedStr = formatTime(elapsedTime.toInt())
    val restStr = formatTime(restTime)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) {
                        onCollapse()
                    }
                }
            },
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Drag handle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(TextTertiary, RoundedCornerShape(2.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exercise name
            Text(
                text = "Current Exercise",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Set progress and previous
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Set",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = setProgress,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = previousSet,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Input fields placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    label = { Text("Weight (kg)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextTertiary
                    )
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    label = { Text("Reps") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextTertiary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Complete Set Button
            Button(
                onClick = { /* TODO: Complete set */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Complete Set",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rest Timer (if resting)
            if (isResting) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = NeonCyan.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rest Timer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan
                        )
                        Text(
                            text = restStr,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = { onAddRestTime(-15) }) {
                                Text("-15s", color = TextSecondary)
                            }
                            TextButton(onClick = { onAddRestTime(15) }) {
                                Text("+15s", color = NeonTeal)
                            }
                            TextButton(onClick = onSkipRest) {
                                Text("Skip", color = WarningOrange)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Elapsed time
            Text(
                text = "Total time: $elapsedStr",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
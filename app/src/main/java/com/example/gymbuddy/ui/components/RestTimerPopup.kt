package com.csci3310.gymbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.csci3310.gymbuddy.ui.theme.*

@Composable
fun RestTimerPopup(
    remainingSeconds: Int,
    totalSeconds: Int,
    isComplete: Boolean = false,
    onMinimize: () -> Unit,
    onAdjustTime: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextTertiary.copy(alpha = 0.5f))
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = formatTimeLarge(remainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isComplete) NeonTeal else NeonCyan
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isComplete) {
                    Text(
                        text = "Timer Complete!",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonTeal,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ProgressBarBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(NeonTeal, NeonCyan)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${formatTime(remainingSeconds)} remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isComplete) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonTeal
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Done ✓", fontWeight = FontWeight.Bold)
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onAdjustTime(-10) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 10s")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-10s")
                    }
                    
                    Button(
                        onClick = onMinimize,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Minimize", fontWeight = FontWeight.Bold)
                    }
                    
                    FilledTonalButton(
                        onClick = { onAdjustTime(10) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 10s")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+10s")
                    }
                    
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ErrorRed.copy(alpha = 0.2f),
                            contentColor = ErrorRed
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }
    }
}

private fun formatTimeLarge(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}

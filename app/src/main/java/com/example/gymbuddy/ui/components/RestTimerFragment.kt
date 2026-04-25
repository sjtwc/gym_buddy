package com.csci3310.gymbuddy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.csci3310.gymbuddy.domain.model.formatTime
import com.csci3310.gymbuddy.ui.theme.*

@Composable
fun RestTimerFragment(
    isVisible: Boolean,
    timerType: String,
    timeRemaining: Int,
    totalTime: Int,
    onToggleExpand: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddTime: () -> Unit,
    onSubtractTime: () -> Unit,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val isExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime.toFloat() else 1f
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(DarkSurface)
        ) {
            if (expanded) {
                ExpandedTimerControls(
                    timerType = timerType,
                    timeRemaining = timeRemaining,
                    totalTime = totalTime,
                    progress = progress,
                    isPaused = isPaused,
                    onCollapse = { expanded = false },
                    onPause = onPause,
                    onResume = onResume,
                    onStop = onStop,
                    onAddTime = onAddTime,
                    onSubtractTime = onSubtractTime
                )
            } else {
                CollapsedTimerRow(
                    timerType = timerType,
                    timeRemaining = timeRemaining,
                    progress = progress,
                    onExpand = { expanded = true },
                    onPause = onPause,
                    onResume = onResume
                )
            }
        }
    }
}

@Composable
fun CollapsedTimerRow(
    timerType: String,
    timeRemaining: Int,
    progress: Float,
    onExpand: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onExpand() },
        color = NeonCyan.copy(alpha = 0.2f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Rest: $timerType",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = formatTime(timeRemaining),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = if (progress < 1f) onPause else onResume) {
                        Icon(
                            imageVector = if (progress >= 1f) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (progress < 1f) "Pause" else "Resume",
                            tint = NeonTeal
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = NeonCyan,
                trackColor = DarkSurfaceElevated
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTimerControls(
    timerType: String,
    timeRemaining: Int,
    totalTime: Int,
    progress: Float,
    isPaused: Boolean,
    onCollapse: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddTime: () -> Unit,
    onSubtractTime: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
        color = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Text(
                        text = "Rest: $timerType",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextSecondary
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(timeRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonCyan,
                    trackColor = DarkSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${formatTime(timeRemaining)} / ${formatTime(totalTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSubtractTime,
                    modifier = Modifier
                        .size(56.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Text(
                        text = "-10s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                }
                
                IconButton(
                    onClick = if (isPaused) onResume else onPause,
                    modifier = Modifier
                        .size(72.dp)
                        .background(NeonTeal, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = DarkBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(56.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = WarningOrange
                    )
                }
                
                IconButton(
                    onClick = onAddTime,
                    modifier = Modifier
                        .size(56.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Text(
                        text = "+10s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                }
            }
        }
    }
}
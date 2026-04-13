package com.example.gymbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymbuddy.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    var showUnitsDialog by remember { mutableStateOf(false) }
    var showRestTimerDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingsSection(title = "Workout")
            
            SettingsItem(
                icon = Icons.Default.FitnessCenter,
                title = "Default Rest Timer",
                subtitle = "90 seconds",
                onClick = { showRestTimerDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.Speed,
                title = "Weight Unit",
                subtitle = "Kilograms (kg)",
                onClick = { showUnitsDialog = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "Notifications")
            
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Streak Reminders",
                subtitle = "Daily at 8:00 PM",
                onClick = { showNotificationsDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.Timer,
                title = "Rest Timer Notifications",
                subtitle = "On",
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "1.10.0",
                onClick = { }
            )
            
            SettingsItem(
                icon = Icons.Default.Code,
                title = "Open Source Licenses",
                subtitle = "View third-party licenses",
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showUnitsDialog) {
        UnitsSelectionDialog(
            currentUnit = "Kilograms (kg)",
            onDismiss = { showUnitsDialog = false },
            onSelect = { unit ->
                showUnitsDialog = false
            }
        )
    }
    
    if (showRestTimerDialog) {
        RestTimerSelectionDialog(
            currentSeconds = 90,
            onDismiss = { showRestTimerDialog = false },
            onSelect = { seconds ->
                showRestTimerDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = NeonTeal,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary
            )
        }
    }
}

@Composable
private fun UnitsSelectionDialog(
    currentUnit: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var selected by remember { mutableStateOf(currentUnit) }
    val units = listOf("Kilograms (kg)", "Pounds (lb)")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Weight Unit", color = TextPrimary)
        },
        text = {
            Column {
                units.forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = unit }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == unit,
                            onClick = { selected = unit },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(unit, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) {
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
private fun RestTimerSelectionDialog(
    currentSeconds: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var selected by remember { mutableIntStateOf(currentSeconds) }
    val options = listOf(30, 60, 90, 120, 180, 240, 300)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text("Default Rest Timer", color = TextPrimary)
        },
        text = {
            Column {
                options.forEach { seconds ->
                    val label = when (seconds) {
                        30 -> "30 seconds"
                        60 -> "1 minute"
                        90 -> "1 minute 30 seconds"
                        120 -> "2 minutes"
                        180 -> "3 minutes"
                        240 -> "4 minutes"
                        300 -> "5 minutes"
                        else -> "$seconds seconds"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = seconds }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == seconds,
                            onClick = { selected = seconds },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) {
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

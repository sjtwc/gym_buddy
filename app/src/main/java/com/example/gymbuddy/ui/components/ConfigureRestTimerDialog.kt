package com.example.gymbuddy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.domain.model.RestTimer
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.ui.theme.*

@Composable
fun ConfigureRestTimerDialog(
    restTimers: List<RestTimer>,
    onDismiss: () -> Unit,
    onSave: (List<RestTimer>) -> Unit
) {
    var warmupTime by remember { mutableStateOf("01:00") }
    var workTime by remember { mutableStateOf("01:30") }
    var dropTime by remember { mutableStateOf("01:00") }
    
    LaunchedEffect(restTimers) {
        warmupTime = restTimers.find { it.type == SetType.WARMUP }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "01:00"
        } ?: "01:00"
        workTime = restTimers.find { it.type == SetType.WORK }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "01:30"
        } ?: "01:30"
        dropTime = restTimers.find { it.type == SetType.DROP }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "01:00"
        } ?: "01:00"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Configure Rest Timer",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimerInputField(
                    label = "Warmup Set Timer",
                    value = warmupTime,
                    onValueChange = { warmupTime = it }
                )
                TimerInputField(
                    label = "Work Set Timer",
                    value = workTime,
                    onValueChange = { workTime = it }
                )
                TimerInputField(
                    label = "Drop Set Timer",
                    value = dropTime,
                    onValueChange = { dropTime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timers = listOf(
                        RestTimer(SetType.WARMUP, parseTimeInput(warmupTime), false),
                        RestTimer(SetType.WORK, parseTimeInput(workTime), false),
                        RestTimer(SetType.DROP, parseTimeInput(dropTime), false)
                    )
                    onSave(timers)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
            ) {
                Text("Save Configure", fontWeight = FontWeight.Bold)
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
fun TimerInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == ':' }
                if (filtered.length <= 5) {
                    if (filtered.length == 2 && !filtered.contains(":")) {
                        onValueChange(filtered + ":")
                    } else {
                        onValueChange(filtered)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = { Text("mm:ss", color = TextTertiary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                cursorColor = NeonCyan
            )
        )
    }
}

private fun formatTimeToInput(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun parseTimeInput(input: String): Int {
    val parts = input.split(":")
    return if (parts.size == 2) {
        (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    } else {
        input.toIntOrNull() ?: 0
    }
}
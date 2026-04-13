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
    var normalTime by remember { mutableStateOf("02:00") }
    var warmupTime by remember { mutableStateOf("01:00") }
    var workTime by remember { mutableStateOf("02:00") }
    var dropTime by remember { mutableStateOf("01:30") }
    var failureTime by remember { mutableStateOf("02:00") }
    
    LaunchedEffect(restTimers) {
        normalTime = restTimers.find { it.type == SetType.NORMAL }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "02:00"
        } ?: "02:00"
        warmupTime = restTimers.find { it.type == SetType.WARMUP }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "01:00"
        } ?: "01:00"
        workTime = restTimers.find { it.type == SetType.WORK }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "02:00"
        } ?: "02:00"
        dropTime = restTimers.find { it.type == SetType.DROP }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "01:30"
        } ?: "01:30"
        failureTime = restTimers.find { it.type == SetType.FAILURE }?.let { 
            if (it.durationSeconds > 0) formatTimeToInput(it.durationSeconds) else "02:00"
        } ?: "02:00"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Configure Rest Timers",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimerInputRow(
                    label = "Normal",
                    value = normalTime,
                    onValueChange = { normalTime = it }
                )
                TimerInputRow(
                    label = "Warmup",
                    value = warmupTime,
                    onValueChange = { warmupTime = it }
                )
                TimerInputRow(
                    label = "Work",
                    value = workTime,
                    onValueChange = { workTime = it }
                )
                TimerInputRow(
                    label = "Drop",
                    value = dropTime,
                    onValueChange = { dropTime = it }
                )
                TimerInputRow(
                    label = "Failure",
                    value = failureTime,
                    onValueChange = { failureTime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timers = listOf(
                        RestTimer(SetType.NORMAL, parseTimeInput(normalTime), false),
                        RestTimer(SetType.WARMUP, parseTimeInput(warmupTime), false),
                        RestTimer(SetType.WORK, parseTimeInput(workTime), false),
                        RestTimer(SetType.DROP, parseTimeInput(dropTime), false),
                        RestTimer(SetType.FAILURE, parseTimeInput(failureTime), false)
                    )
                    onSave(timers)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
private fun TimerInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
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
            modifier = Modifier.width(100.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.End),
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

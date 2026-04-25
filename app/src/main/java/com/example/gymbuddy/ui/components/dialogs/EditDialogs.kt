package com.csci3310.gymbuddy.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Workout Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout Name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("Save")
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
fun EditStartTimeDialog(
    currentStartTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var startTimeText by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf("") }

    LaunchedEffect(currentStartTime) {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentStartTime }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        startTimeText = String.format("%02d:%02d", hour, minute)
        startDateText = String.format("%02d/%02d/%04d", day, month, year)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Start Time") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = startTimeText,
                    onValueChange = { startTimeText = it },
                    label = { Text("Time (HH:mm)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("Date (dd/MM/yyyy)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val timeParts = startTimeText.split(":")
                val dateParts = startDateText.split("/")
                if (timeParts.size == 2 && dateParts.size == 3) {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, dateParts[2].toIntOrNull() ?: calendar.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, (dateParts[1].toIntOrNull() ?: 1) - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toIntOrNull() ?: 1)
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toIntOrNull() ?: 0)
                    calendar.set(Calendar.MINUTE, timeParts[1].toIntOrNull() ?: 0)
                    calendar.set(Calendar.SECOND, 0)
                    onConfirm(calendar.timeInMillis)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
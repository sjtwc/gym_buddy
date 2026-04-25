package com.csci3310.gymbuddy.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.csci3310.gymbuddy.ui.theme.DarkSurfaceElevated
import com.csci3310.gymbuddy.ui.theme.NeonTeal
import com.csci3310.gymbuddy.ui.theme.TextPrimary
import com.csci3310.gymbuddy.ui.theme.TextSecondary
import com.csci3310.gymbuddy.ui.theme.TextTertiary

private val RoundedCornerShape8 = RoundedCornerShape(8.dp)

@Composable
fun FeelingRatingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedFeeling by remember { mutableIntStateOf(3) }

    val feelings = listOf(
        1 to "😫 Terrible",
        2 to "😕 Bad",
        3 to "😐 Okay",
        4 to "😊 Good",
        5 to "🤩 Amazing"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text(
                text = "How was your workout?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate how you felt during this workout",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    feelings.forEach { (rating, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedFeeling = rating }
                        ) {
                            Text(
                                text = label.split(" ").first(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (selectedFeeling == rating) NeonTeal else TextTertiary,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = feelings.find { it.first == selectedFeeling }?.let { "${it.second}!" } ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedFeeling) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Finish Workout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
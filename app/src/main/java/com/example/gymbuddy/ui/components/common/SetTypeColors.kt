package com.example.gymbuddy.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.ui.theme.DarkSurfaceElevated
import com.example.gymbuddy.ui.theme.NeonCyan
import com.example.gymbuddy.ui.theme.NeonPurple
import com.example.gymbuddy.ui.theme.NeonTeal
import com.example.gymbuddy.ui.theme.TextPrimary
import com.example.gymbuddy.ui.theme.WarningOrange

object SetTypeColors {
    fun getBackgroundColor(setType: SetType): Color {
        return when (setType) {
            SetType.NORMAL -> DarkSurfaceElevated
            SetType.WORK -> NeonTeal.copy(alpha = 0.2f)
            SetType.WARMUP -> WarningOrange.copy(alpha = 0.2f)
            SetType.DROP -> NeonCyan.copy(alpha = 0.2f)
            SetType.FAILURE -> NeonPurple.copy(alpha = 0.2f)
        }
    }

    fun getTextColor(setType: SetType): Color {
        return when (setType) {
            SetType.NORMAL -> TextPrimary
            SetType.WORK -> NeonTeal
            SetType.WARMUP -> WarningOrange
            SetType.DROP -> NeonCyan
            SetType.FAILURE -> NeonPurple
        }
    }

    fun getTimerLabel(setType: SetType): String {
        return when (setType) {
            SetType.WARMUP -> "Warmup Set Timer"
            SetType.WORK -> "Work Set Timer"
            SetType.DROP -> "Drop Set Timer"
            SetType.NORMAL -> "Timer"
            SetType.FAILURE -> "Failure Set Timer"
        }
    }
}

@Composable
fun SetTypeButton(
    setType: SetType,
    setNumber: Int,
    onToggle: (SetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    val displayText = if (setType == SetType.NORMAL) setNumber.toString() else setType.abbreviation

    Box(modifier = modifier) {
        Surface(
            onClick = { showDropdown = true },
            shape = RoundedCornerShape(4.dp),
            color = SetTypeColors.getBackgroundColor(setType)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SetTypeColors.getTextColor(setType),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = { Text("Normal (${setNumber})") },
                onClick = {
                    onToggle(SetType.NORMAL)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Warmup Set", color = WarningOrange) },
                onClick = {
                    onToggle(SetType.WARMUP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Drop Set", color = NeonCyan) },
                onClick = {
                    onToggle(SetType.DROP)
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text("Failure Set", color = NeonPurple) },
                onClick = {
                    onToggle(SetType.FAILURE)
                    showDropdown = false
                }
            )
        }
    }
}
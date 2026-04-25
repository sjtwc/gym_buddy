package com.csci3310.gymbuddy.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.csci3310.gymbuddy.domain.model.PetMood
import com.csci3310.gymbuddy.domain.model.VirtualPet
import com.csci3310.gymbuddy.domain.usecase.GetWorkoutSummaryUseCase
import com.csci3310.gymbuddy.domain.usecase.WorkoutSummary
import com.csci3310.gymbuddy.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isFromPet: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetChatbotDialog(
    petName: String,
    petMood: PetMood,
    petHappiness: Int,
    lastWorkoutDate: Long?,
    onDismiss: () -> Unit,
    getWorkoutSummaryUseCase: GetWorkoutSummaryUseCase
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showingSummary by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(petName, petMood) {
        val greeting = getPetGreeting(petMood)
        messages = listOf(
            ChatMessage(
                content = "$greeting ${petMood.meowResponses.random()}",
                isFromPet = true
            )
        )
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    fun sendMessage() {
        if (inputText.isBlank()) return
        
        messages = messages + ChatMessage(content = inputText, isFromPet = false)
        val userMessage = inputText
        inputText = ""
        
        coroutineScope.launch {
            kotlinx.coroutines.delay(500)
            val response = getPetResponse(userMessage, petMood)
            messages = messages + ChatMessage(content = response, isFromPet = true)
        }
    }
    
    fun showWeeklySummary() {
        showingSummary = true
        isLoading = true
        
        coroutineScope.launch {
            val weeklySummary = getWorkoutSummaryUseCase.getWeeklySummary()
            val summaryText = formatWeeklySummary(weeklySummary)
            
            messages = messages + ChatMessage(
                content = "📊 Weekly Summary:\n$summaryText",
                isFromPet = true
            )
            isLoading = false
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated)
        ) {
            PetChatbotHeader(
                petName = petName,
                petMood = petMood,
                onDismiss = onDismiss
            )
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        petName = petName
                    )
                }
                
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Typing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Button(
                    onClick = { showWeeklySummary() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonTeal.copy(alpha = 0.2f),
                        contentColor = NeonTeal
                    )
                ) {
                    Text("📊 Weekly Summary")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Say something...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonTeal
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() })
                    )
                    
                    IconButton(
                        onClick = { sendMessage() },
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) NeonTeal else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PetChatbotHeader(
    petName: String,
    petMood: PetMood,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = petMood.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = petMood.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (petMood) {
                        PetMood.EXCITED -> NeonCyan
                        PetMood.HAPPY -> PetHappy
                        PetMood.NEUTRAL -> TextSecondary
                        PetMood.SAD -> PetSad
                        PetMood.DISAPPOINTED -> TextSecondary
                        PetMood.WAITING -> TextSecondary
                    }
                )
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    petName: String
) {
    val isFromUser = !message.isFromPet
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
    ) {
        if (!isFromUser) {
            Text(
                text = petName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isFromUser) NeonTeal.copy(alpha = 0.3f)
                    else DarkSurface
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

private fun getPetGreeting(petMood: PetMood): String {
    return when (petMood) {
        PetMood.EXCITED -> "Nyaaa! So happy to see you! 🎉"
        PetMood.HAPPY -> "Meow! Great to see you! 😺"
        PetMood.NEUTRAL -> "Meow~ Ready to chat? 🐱"
        PetMood.SAD -> "Meow... I missed you 😿"
        PetMood.DISAPPOINTED -> "Nya...? Where have you been? 🙀"
        PetMood.WAITING -> "Meow~ Take a seat! 😼"
    }
}

private fun getPetResponse(message: String, petMood: PetMood): String {
    val lowercaseMessage = message.lowercase()
    
    return when {
        lowercaseMessage.contains("hello") || lowercaseMessage.contains("hi") || lowercaseMessage.contains("hey") -> 
            "${petMood.meowResponses.random()} ${petMood.message}"
        lowercaseMessage.contains("help") || lowercaseMessage.contains("what can you do") ->
            "I can help you track your gym progress! Tap the 📊 Weekly Summary button to see your workout stats for the week!"
        lowercaseMessage.contains("workout") || lowercaseMessage.contains("exercise") ->
            "Keep up the great work! Every rep counts! 💪 Tap the summary button to see your progress!"
        lowercaseMessage.contains("thank") || lowercaseMessage.contains("thanks") ->
            "You're welcome! Meow~ 💕"
        lowercaseMessage.contains("love") ->
            "Meow! I love you too! ${petMood.meowResponses.random()}"
        else -> "${petMood.meowResponses.random()} ${petMood.extraMessages.random()}"
    }
}

private fun formatWeeklySummary(summary: WorkoutSummary): String {
    if (summary.totalWorkouts == 0) {
        return "No workouts this week yet! Let's get moving! 🏋️"
    }
    
    val topMuscles = summary.muscleGroupsTrained.entries
        .sortedByDescending { it.value }
        .take(3)
        .joinToString(", ") { "${it.key}: ${(it.value / 1000).toInt()}k" }
    
    return """
        🏋️ Workouts: ${summary.totalWorkouts}
        📦 Total Volume: ${(summary.totalVolume / 1000).toInt()}k lbs
        ⏱️ Duration: ${summary.totalDuration / 60}min
        💪 Top Muscles: $topMuscles
        🏆 PRs: ${summary.recordsBroken}
    """.trimIndent()
}
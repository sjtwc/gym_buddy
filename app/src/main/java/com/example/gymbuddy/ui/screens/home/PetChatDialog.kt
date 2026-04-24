package com.example.gymbuddy.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymbuddy.data.repository.ChatMessage
import com.example.gymbuddy.ui.theme.*

@Composable
fun PetChatDialog(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    petEmoji: String,
    aiPetEmoji: String,
    petName: String,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ChatHeader(
                    petEmoji = petEmoji,
                    aiPetEmoji = aiPetEmoji,
                    petName = petName,
                    onDismiss = onDismiss
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            petEmoji = petEmoji,
                            petName = petName
                        )
                    }

                    if (isLoading) {
                        item {
                            LoadingIndicator(petEmoji = petEmoji)
                        }
                    }
                }

                ChatInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank() && !isLoading) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    isSending = isLoading
                )
            }
        }
    }
}

@Composable
private fun ChatHeader(
    petEmoji: String,
    aiPetEmoji: String,
    petName: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PetHappy.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = aiPetEmoji,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = petName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "AI Pet",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
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
private fun ChatBubble(
    message: ChatMessage,
    petEmoji: String,
    petName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PetHappy.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = petEmoji,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) NeonTeal.copy(alpha = 0.2f)
                    else DarkSurfaceVariant
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

@Composable
private fun LoadingIndicator(petEmoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PetHappy.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = petEmoji,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonTeal
                )
            }
        }
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = if (isSending) "AI is typing..." else "Ask me anything...",
                    color = TextTertiary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant,
                cursorColor = NeonTeal,
                focusedIndicatorColor = NeonTeal,
                unfocusedIndicatorColor = DarkSurfaceVariant
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = true,
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onSend,
            enabled = !isSending && value.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (!isSending && value.isNotBlank()) NeonTeal
                    else DarkSurfaceVariant
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (!isSending && value.isNotBlank()) DarkBackground else TextTertiary
            )
        }
    }
}
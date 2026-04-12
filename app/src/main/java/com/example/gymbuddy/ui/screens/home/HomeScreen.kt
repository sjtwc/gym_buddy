package com.example.gymbuddy.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        GreetingSection(
            userName = uiState.userProfile?.name ?: "Trainer",
            level = uiState.userProfile?.level ?: 1,
            xp = uiState.userProfile?.xp ?: 0
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PetSection(
            petName = uiState.userProfile?.pet?.name ?: "GymBot",
            happiness = uiState.userProfile?.pet?.happiness ?: 50,
            mood = uiState.userProfile?.pet?.mood ?: PetMood.NEUTRAL
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StatsSection(
            currentStreak = uiState.userProfile?.currentStreak ?: 0,
            longestStreak = uiState.userProfile?.longestStreak ?: 0,
            totalWorkouts = uiState.userProfile?.totalWorkouts ?: 0
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StartWorkoutButton(
            onClick = { navController.navigate(Screen.ActiveWorkout.route) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val nextRoutine = uiState.nextWorkout
        if (nextRoutine != null) {
            NextWorkoutCard(
                routineName = nextRoutine.name,
                routineType = nextRoutine.type,
                exerciseCount = nextRoutine.exercises.size,
                estimatedMinutes = nextRoutine.estimatedMinutes,
                onStart = { navController.navigate(Screen.ActiveWorkout.route) }
            )
        }
    }
}

@Composable
fun GreetingSection(userName: String, level: Int, xp: Int) {
    Column {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        XpProgressBar(level = level, xp = xp)
    }
}

@Composable
fun XpProgressBar(level: Int, xp: Int) {
    val xpForNextLevel = level * 1000
    val progress = (xp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeonTeal
            )
            Text(
                text = "$xp / $xpForNextLevel XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ProgressBarBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NeonTeal, NeonCyan)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun PetSection(petName: String, happiness: Int, mood: PetMood) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                when {
                                    happiness > 70 -> PetHappy
                                    happiness > 40 -> PetExcited
                                    else -> PetSad
                                },
                                DarkSurfaceElevated
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mood.emoji,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\"${mood.message}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            CircularProgressIndicator(
                progress = { happiness / 100f },
                modifier = Modifier.size(48.dp),
                color = when {
                    happiness > 70 -> SuccessGreen
                    happiness > 40 -> WarningOrange
                    else -> ErrorRed
                },
                trackColor = ProgressBarBackground,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun StatsSection(currentStreak: Int, longestStreak: Int, totalWorkouts: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "$currentStreak",
            subtitle = "days",
            icon = "🔥",
            iconColor = StreakFlame
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Best",
            value = "$longestStreak",
            subtitle = "days",
            icon = "🏆",
            iconColor = XpGold
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total",
            value = "$totalWorkouts",
            subtitle = "workouts",
            icon = "💪",
            iconColor = NeonTeal
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StartWorkoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonTeal
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "START WORKOUT",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun NextWorkoutCard(
    routineName: String,
    routineType: String,
    exerciseCount: Int,
    estimatedMinutes: Int,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Next Workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = routineName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "$exerciseCount exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$estimatedMinutes min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChip(text = routineType)
                TagChip(text = "Push Day")
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = NeonTeal.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = NeonTeal
        )
    }
}
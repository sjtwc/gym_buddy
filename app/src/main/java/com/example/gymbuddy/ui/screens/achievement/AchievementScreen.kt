package com.example.gymbuddy.ui.screens.achievement

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymbuddy.ui.theme.*

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val progress: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(navController: NavController) {
    val achievements = listOf(
        Achievement(1, "First Steps", "Complete your first workout", "🎯", true, 100),
        Achievement(2, "Consistent", "Work out 7 days in a row", "🔥", true, 100),
        Achievement(3, "Heavy Lifter", "Lift 10,000kg total volume", "🏋️", true, 100),
        Achievement(4, "Early Bird", "Complete a workout before 7am", "🌅", false, 0),
        Achievement(5, "Night Owl", "Complete a workout after 9pm", "🌙", false, 0),
        Achievement(6, "Marathoner", "Complete 50 workouts", "🏆", false, 35),
        Achievement(7, "Strength Master", "Reach level 10", "💪", false, 60),
        Achievement(8, "Social Butterfly", "Share a workout", "🤝", false, 0),
        Achievement(9, "Perfectionist", "Complete workout without skipping", "✨", true, 100),
        Achievement(10, "Variety", "Try 20 different exercises", "🎨", false, 45),
        Achievement(11, "Beast Mode", "Complete 100 workouts", "🦍", false, 12),
        Achievement(12, "Championship", "Win a monthly challenge", "🥇", false, 0)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Achievements",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val unlockedCount = achievements.count { it.isUnlocked }
        val totalCount = achievements.size

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$unlockedCount / $totalCount unlocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Text(
                    "${(unlockedCount * 100 / totalCount)}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked) NeonTeal.copy(alpha = 0.2f)
                        else TextTertiary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (achievement.isUnlocked) achievement.icon else "🔒",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else TextTertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = TextTertiary
            )

            if (!achievement.isUnlocked && achievement.progress > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { achievement.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonTeal,
                    trackColor = ProgressBarBackground
                )
                Text(
                    text = "${achievement.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
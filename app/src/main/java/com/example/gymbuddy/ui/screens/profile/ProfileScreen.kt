package com.example.gymbuddy.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileHeader(
            name = uiState.userProfile?.name ?: "Trainer",
            level = uiState.userProfile?.level ?: 1,
            xp = uiState.userProfile?.xp ?: 0,
            title = uiState.userProfile?.title ?: "Novice",
            petMood = uiState.userProfile?.pet?.mood ?: PetMood.NEUTRAL
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileStats(
            currentStreak = uiState.userProfile?.currentStreak ?: 0,
            longestStreak = uiState.userProfile?.longestStreak ?: 0,
            totalWorkouts = uiState.userProfile?.totalWorkouts ?: 0,
            totalVolume = uiState.userProfile?.totalVolume ?: 0f
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AchievementBadgesSection(navController = navController)
    }
}

@Composable
fun ProfileHeader(name: String, level: Int, xp: Int, title: String, petMood: PetMood) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NeonTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = petMood.emoji, fontSize = 40.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            if (title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = XpGold.copy(alpha = 0.2f)) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = NeonTeal.copy(alpha = 0.2f)) {
                    Text(
                        text = "Level $level",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$xp XP", style = MaterialTheme.typography.bodyMedium, color = XpGold)
            }
        }
    }
}

@Composable
fun ProfileStats(currentStreak: Int, longestStreak: Int, totalWorkouts: Int, totalVolume: Float) {
    Column {
        Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Current Streak", "$currentStreak days", "🔥", modifier = Modifier.weight(1f))
            StatItem("Best Streak", "$longestStreak days", "🏆", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Total Workouts", "$totalWorkouts", "💪", modifier = Modifier.weight(1f))
            StatItem("Total Volume", "${totalVolume.toInt()}kg", "📊", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    }
}

@Composable
fun AchievementBadgesSection(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.Achievement.route) },
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("View your badges and progress", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
            Row {
                listOf("🥇", "🥈", "🥉").forEach { badge ->
                    Text(text = badge, fontSize = 20.sp)
                }
            }
        }
    }
}
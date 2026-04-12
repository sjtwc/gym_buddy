package com.example.gymbuddy.ui.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*

@Composable
fun ExerciseScreen(
    navController: NavController,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedExerciseId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Exercise Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Muscle Group Filter
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = uiState.selectedMuscle == null,
                    onClick = { viewModel.filterByMuscle(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonTeal,
                        selectedLabelColor = DarkBackground
                    )
                )
            }
            items(uiState.muscleGroups) { muscle ->
                FilterChip(
                    selected = uiState.selectedMuscle == muscle,
                    onClick = { viewModel.filterByMuscle(muscle) },
                    label = { Text(muscle) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonTeal,
                        selectedLabelColor = DarkBackground
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Exercise List
        if (uiState.exercises.isEmpty()) {
            EmptyExerciseState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.exercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { selectedExerciseId = exercise.id }
                    )
                }
            }
        }
    }

    selectedExerciseId?.let { exerciseId ->
        ExerciseInsightDialog(
            exerciseId = exerciseId,
            onDismiss = { selectedExerciseId = null }
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search exercises...", color = TextTertiary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceElevated,
            unfocusedContainerColor = DarkSurfaceElevated,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonTeal,
            focusedIndicatorColor = NeonTeal,
            unfocusedIndicatorColor = DarkSurfaceElevated
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Icon/Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(NeonTeal.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏋️", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonCyan.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = exercise.targetMuscle,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TextTertiary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = exercise.equipmentType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
            
            Text(text = "›", style = MaterialTheme.typography.headlineMedium, color = TextTertiary)
        }
    }
}

@Composable
fun EmptyExerciseState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No exercises found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
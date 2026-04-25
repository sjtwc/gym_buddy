package com.csci3310.gymbuddy.ui.screens.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.csci3310.gymbuddy.domain.model.Exercise
import com.csci3310.gymbuddy.domain.model.MuscleGroup
import com.csci3310.gymbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerPage(
    onDismiss: () -> Unit,
    onAddExercises: (List<Exercise>) -> Unit,
    alreadyAddedExerciseIds: Set<Long> = emptySet(),
    viewModel: ExercisePickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMuscleFilter by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopBar(
                    selectedCount = uiState.selectedExercises.size,
                    onClose = onDismiss,
                    onAdd = {
                        val selectedToAdd = uiState.selectedExercises
                            .filter { it.id !in alreadyAddedExerciseIds }
                            .filter { ex -> uiState.exercises.any { it.id == ex.id } }
                        onAddExercises(selectedToAdd)
                    }
                )

                SearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) }
                )

                FilterRow(
                    selectedMuscleGroup = uiState.selectedMuscleGroup,
                    sortOption = uiState.sortOption,
                    onMuscleGroupClick = { showMuscleFilter = true },
                    onSortClick = {
                        val nextSort = when (uiState.sortOption) {
                            SortOption.A_Z -> SortOption.Z_A
                            SortOption.Z_A -> SortOption.RECENTLY_USED
                            SortOption.RECENTLY_USED -> SortOption.A_Z
                        }
                        viewModel.updateSortOption(nextSort)
                    }
                )

                if (showMuscleFilter) {
                    MuscleGroupFilterSheet(
                        selectedMuscleGroup = uiState.selectedMuscleGroup,
                        onSelect = { muscleGroup ->
                            viewModel.updateMuscleGroupFilter(muscleGroup)
                            showMuscleFilter = false
                        },
                        onDismiss = { showMuscleFilter = false }
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonTeal)
                    }
                } else if (uiState.filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    val groupedExercises = viewModel.getGroupedExercises()
                        .mapValues { (_, exercises) -> 
                            exercises.filter { it.id !in alreadyAddedExerciseIds }
                        }
                        .filterValues { it.isNotEmpty() }
                    ExerciseGroupedList(
                        groupedExercises = groupedExercises,
                        selectedExercises = uiState.selectedExercises.filter { it.id !in alreadyAddedExerciseIds }.toSet(),
                        onToggle = { viewModel.toggleExerciseSelection(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary
            )
        }

        Text(
            text = "Select Exercises",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Button(
            onClick = onAdd,
            enabled = selectedCount > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonTeal,
                disabledContainerColor = TextTertiary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Add ($selectedCount)",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search exercises", color = TextSecondary) },
        leadingIcon = {
            Icon(Icons.Default.Search, null, tint = TextSecondary)
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, null, tint = TextSecondary)
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonTeal,
            unfocusedBorderColor = TextTertiary,
            cursorColor = NeonTeal,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun FilterRow(
    selectedMuscleGroup: String?,
    sortOption: SortOption,
    onMuscleGroupClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMuscleGroup != null,
            onClick = onMuscleGroupClick,
            label = {
                Text(selectedMuscleGroup ?: "Muscle Group")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = DarkSurface,
                labelColor = TextPrimary,
                iconColor = TextSecondary
            )
        )

        FilterChip(
            selected = true,
            onClick = onSortClick,
            label = { Text(sortOption.displayName) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Sort,
                    null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = DarkSurface,
                labelColor = TextPrimary,
                iconColor = TextSecondary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupFilterSheet(
    selectedMuscleGroup: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurfaceElevated.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Muscle Group",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                item {
                    FilterOptionItem(
                        text = "All",
                        isSelected = selectedMuscleGroup == null,
                        onClick = { onSelect(null) }
                    )
                }
                items(MuscleGroup.entries) { muscleGroup ->
                    FilterOptionItem(
                        text = muscleGroup.displayName,
                        isSelected = selectedMuscleGroup == muscleGroup.displayName,
                        onClick = { onSelect(muscleGroup.displayName) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) NeonTeal else TextPrimary
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                null,
                tint = NeonTeal
            )
        }
    }
}

@Composable
fun ExerciseGroupedList(
    groupedExercises: Map<String, List<Exercise>>,
    selectedExercises: Set<Exercise>,
    onToggle: (Exercise) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        groupedExercises.forEach { (muscleGroup, exercises) ->
            item {
                Text(
                    text = muscleGroup,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(exercises) { exercise ->
                ExerciseItem(
                    exercise = exercise,
                    isSelected = selectedExercises.contains(exercise),
                    onToggle = { onToggle(exercise) }
                )
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) NeonTeal else TextPrimary
            )
            Text(
                text = exercise.equipmentType,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = NeonTeal,
                uncheckedColor = TextTertiary
            )
        )
    }
}
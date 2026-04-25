package com.example.gymbuddy.ui.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.WorkoutSet
import com.example.gymbuddy.ui.theme.*
import com.example.gymbuddy.ui.components.common.StatCard
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInsightDialog(
    exerciseId: Long,
    onDismiss: () -> Unit,
    viewModel: ExerciseInsightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseData(exerciseId)
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
                DialogHeader(
                    exerciseName = uiState.exercise?.name ?: "Loading...",
                    onDismiss = onDismiss
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonTeal)
                    }
                } else {
                    InsightTabs(
                        exercise = uiState.exercise,
                        historicalSets = uiState.historicalSets,
                        graphData = uiState.graphData,
                        predictedRecords = uiState.predictedRecords,
                        summary = viewModel.getSummaryStats()
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    exerciseName: String,
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
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
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
private fun InsightTabs(
    exercise: Exercise?,
    historicalSets: List<HistoricalSetWithDate>,
    graphData: GraphData?,
    predictedRecords: PredictedRecords?,
    summary: InsightSummary
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("About", "History", "Graph", "Record")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = NeonTeal
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) NeonTeal else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AboutTab(exercise = exercise)
            1 -> HistoryTab(historicalSets = historicalSets)
            2 -> GraphTab(graphData = graphData)
            3 -> RecordTab(
                summary = summary,
                predictedRecords = predictedRecords,
                historicalSets = historicalSets
            )
        }
    }
}

@Composable
private fun AboutTab(exercise: Exercise?) {
    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.description.ifEmpty { "No description available" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = exercise.targetMuscle,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyan
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = exercise.equipmentType,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonPurple
                    )
                }
            }
        }

        if (exercise.instructions.isNotEmpty()) {
            item {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                exercise.instructions.forEachIndexed { index, instruction ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        if (exercise.secondaryMuscles.isNotEmpty()) {
            item {
                Text(
                    text = "Secondary Muscles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.secondaryMuscles.forEach { muscle ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TextTertiary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = muscle,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(historicalSets: List<HistoricalSetWithDate>) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (historicalSets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No history yet", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(historicalSets) { session ->
            HistoryCard(
                date = dateFormat.format(Date(session.date)),
                sets = session.sets
            )
        }
    }
}

@Composable
private fun HistoryCard(date: String, sets: List<WorkoutSet>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
            sets.filter { !it.isWarmUp }.take(5).forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set ${set.setNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${set.reps} reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${set.weight.toInt()} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (sets.filter { !it.isWarmUp }.size > 5) {
                Text(
                    text = "+${sets.filter { !it.isWarmUp }.size - 5} more sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GraphTab(graphData: GraphData?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Best Set (Est. 1RM)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
            SimpleLineChart(
                data = graphData?.bestEstimated1RM ?: emptyList(),
                lineColor = NeonTeal
            )
        }

        item {
            Text(
                text = "Best Set (Max Weight)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            SimpleLineChart(
                data = graphData?.bestMaxWeight ?: emptyList(),
                lineColor = NeonCyan
            )
        }

        item {
            Text(
                text = "Total Training Volume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            SimpleLineChart(
                data = graphData?.totalVolume ?: emptyList(),
                lineColor = NeonPurple
            )
        }

        item {
            Text(
                text = "Max Reps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonPink
            )
            Spacer(modifier = Modifier.height(8.dp))
            SimpleLineChart(
                data = graphData?.maxReps ?: emptyList(),
                lineColor = NeonPink
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<GraphPoint>,
    lineColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (data.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            val chartEntryModelProducer = remember(data) {
                ChartEntryModelProducer(
                    data.mapIndexed { index, point ->
                        entryOf(index.toFloat(), point.value)
                    }
                )
            }

            val dateFormatter = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
            val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                data.getOrNull(value.toInt())?.let { dateFormatter.format(Date(it.date)) } ?: ""
            }

            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(
                            lineColor = lineColor,
                            lineBackgroundShader = null
                        )
                    )
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(
                    label = textComponent(
                        color = TextSecondary,
                        padding = dimensionsOf(8.dp)
                    )
                ),
                bottomAxis = rememberBottomAxis(
                    label = textComponent(
                        color = TextSecondary,
                        padding = dimensionsOf(8.dp)
                    ),
                    valueFormatter = bottomAxisValueFormatter
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun RecordTab(
    summary: InsightSummary,
    predictedRecords: PredictedRecords?,
    historicalSets: List<HistoricalSetWithDate>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Best 1RM",
                    value = if (summary.bestEstimated1RM > 0) "${summary.bestEstimated1RM.toInt()} kg" else "-",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Max Weight",
                    value = "${summary.bestMaxWeight.toInt()} kg",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Max Volume",
                    value = "${summary.totalVolume.toInt()} kg",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Max Reps",
                    value = "${summary.maxReps}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Rep Max Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
        }

        item {
            RecordTable(
                predictedRecords = predictedRecords,
                historicalSets = historicalSets
            )
        }
    }
}

@Composable
private fun RecordTable(
    predictedRecords: PredictedRecords?,
    historicalSets: List<HistoricalSetWithDate>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RM",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Text(
                    text = "Best Record",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Text(
                    text = "Predicted",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
            }

            val rows = listOf(
                Triple("1RM", calculateBestForReps(historicalSets, 1), predictedRecords?.oneRM),
                Triple("2RM", calculateBestForReps(historicalSets, 2), predictedRecords?.twoRM),
                Triple("3RM", calculateBestForReps(historicalSets, 3), predictedRecords?.threeRM),
                Triple("4RM", calculateBestForReps(historicalSets, 4), predictedRecords?.fourRM),
                Triple("5RM", calculateBestForReps(historicalSets, 5), predictedRecords?.fiveRM),
                Triple("6RM", calculateBestForReps(historicalSets, 6), predictedRecords?.sixRM),
                Triple("7RM", calculateBestForReps(historicalSets, 7), predictedRecords?.sevenRM),
                Triple("8RM", calculateBestForReps(historicalSets, 8), predictedRecords?.eightRM),
                Triple("9RM", calculateBestForReps(historicalSets, 9), predictedRecords?.nineRM),
                Triple("10RM", calculateBestForReps(historicalSets, 10), predictedRecords?.tenRM),
                Triple("11RM", calculateBestForReps(historicalSets, 11), predictedRecords?.elevenRM),
                Triple("12RM", calculateBestForReps(historicalSets, 12), predictedRecords?.twelveRM)
            )

            rows.forEach { (rm, best, predicted) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rm,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = best,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = predicted?.let { "${it.toInt()} kg" } ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

private fun calculateBestForReps(sessions: List<HistoricalSetWithDate>, targetReps: Int): String {
    val allSets = sessions.flatMap { it.sets }.filter { !it.isWarmUp }
    if (allSets.isEmpty()) return "-"

    val matchingSets = allSets.filter { it.reps == targetReps }
    if (matchingSets.isEmpty()) return "-"

    val bestSet = matchingSets.maxByOrNull { it.weight } ?: return "-"
    return "${bestSet.weight.toInt()} kg"
}

private fun calculateEstimated1RM(weight: Float, reps: Int): Float {
    if (reps <= 0 || weight <= 0) return 0f
    if (reps == 1) return weight
    return weight * (1 + reps / 30f)
}
package com.csci3310.gymbuddy.service

import android.content.Context
import com.csci3310.gymbuddy.data.repository.ScheduledWorkoutRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar

class CalendarEventExporter(
    private val context: Context,
    private val calendarService: CalendarService,
    private val scheduledWorkoutRepository: ScheduledWorkoutRepository
) {
    companion object {
        private const val DEEP_LINK_FORMAT = "gymbuddy://routine?id=%d&date=%s"
    }

    suspend fun exportWeeklySchedule(): Int {
        return try {
            val weeklySchedule = scheduledWorkoutRepository.getWeeklySchedule().first()
            var exportedCount = 0

            for (scheduledWorkout in weeklySchedule) {
                if (scheduledWorkout.isRestDay || scheduledWorkout.routineId == null) {
                    continue
                }

                val routine = scheduledWorkoutRepository.getRoutineById(scheduledWorkout.routineId) ?: continue

                val date = getNextDateForDayOfWeek(scheduledWorkout.dayOfWeek)
                val dateStr = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(date.time)

                val exercisesDescription = buildExercisesDescription(routine.exercises)
                val title = "[GymBuddy] ${getDayName(scheduledWorkout.dayOfWeek)} - ${routine.name}"
                val description = """
                    Exercises:
                    $exercisesDescription

                    Open in app: ${String.format(DEEP_LINK_FORMAT, routine.id, dateStr)}
                """.trimIndent()

                val success = calendarService.createEvent(title, dateStr, description)
                if (success) {
                    exportedCount++
                }
            }

            exportedCount
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun buildExercisesDescription(exercises: List<com.csci3310.gymbuddy.domain.model.RoutineExercise>): String {
        return exercises.mapIndexed { index, exercise ->
            val sets = if (exercise.sets.isNotEmpty()) {
                "${exercise.sets.size}x${exercise.targetReps}"
            } else {
                "${exercise.targetSets}x${exercise.targetReps}"
            }
            "- ${exercise.exercise.name} - $sets"
        }.joinToString("\n")
    }

    private fun getNextDateForDayOfWeek(dayOfWeek: Int): Calendar {
        val calendar = java.util.Calendar.getInstance()
        val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val targetDayOfWeek = if (dayOfWeek == 7) 1 else dayOfWeek + 1

        var daysToAdd = targetDayOfWeek - currentDayOfWeek
        if (daysToAdd <= 0) {
            daysToAdd += 7
        }

        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day $dayOfWeek"
        }
    }
}
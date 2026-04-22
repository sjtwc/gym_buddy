package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.MuscleGoalPreferences
import com.example.gymbuddy.data.local.dao.ScheduledWorkoutDao
import com.example.gymbuddy.data.local.entity.ScheduledWorkoutEntity
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.ScheduledWorkout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledWorkoutRepository @Inject constructor(
    private val scheduledWorkoutDao: ScheduledWorkoutDao,
    private val routineRepository: RoutineRepository,
    private val muscleGoalPreferences: MuscleGoalPreferences
) {
    fun getWeeklySchedule(): Flow<List<ScheduledWorkout>> {
        val weekStart = getWeekStartDate()
        return scheduledWorkoutDao.getScheduledWorkoutsForWeek(weekStart).map { entities ->
            if (entities.isEmpty()) {
                getDefaultAllRestSchedule()
            } else {
                entities.map { entity ->
                    ScheduledWorkout(
                        dayOfWeek = entity.dayOfWeek,
                        routineId = entity.routineId,
                        isRestDay = entity.isRestDay
                    )
                }
            }
        }
    }

    private fun getDefaultAllRestSchedule(): List<ScheduledWorkout> {
        return (1..7).map { day ->
            ScheduledWorkout(
                dayOfWeek = day,
                routineId = null,
                isRestDay = true
            )
        }
    }

    suspend fun saveScheduledWorkout(dayOfWeek: Int, routineId: Long?, isRestDay: Boolean) {
        val weekStart = getWeekStartDate()
        val entity = ScheduledWorkoutEntity(
            weekStartDate = weekStart,
            dayOfWeek = dayOfWeek,
            routineId = routineId,
            isRestDay = isRestDay
        )
        scheduledWorkoutDao.insertScheduledWorkout(entity)
    }

    suspend fun clearWeekSchedule() {
        val weekStart = getWeekStartDate()
        scheduledWorkoutDao.clearWeekSchedule(weekStart)
    }

    suspend fun getSuggestedSchedule(userPreferredRestDays: Set<Int> = setOf(0, 6)): List<ScheduledWorkout> {
        val allRoutines = routineRepository.getAllRoutines().first()
        val workoutHistory = getThisWeeksWorkouts()

        val musclesWorkedThisWeek = workoutHistory.flatMap { workout ->
            workout.exercises.mapNotNull { it.exercise.targetMuscle }
        }.groupingBy { it }.eachCount()

        val muscleGoals = muscleGoalPreferences.getAllGoals()
        val totalGoal = muscleGoals.values.sum()
        if (totalGoal == 0f) return generateDefaultSchedule(allRoutines, userPreferredRestDays)

        val musclesNeeded = muscleGoals.mapValues { (muscle, goal) ->
            val worked = musclesWorkedThisWeek[muscle] ?: 0
            val targetSessions = (goal / totalGoal * 14).toInt().coerceAtLeast(1)
            (targetSessions - worked).coerceAtLeast(0)
        }.filter { it.value > 0 }

        val sortedRoutines = allRoutines.sortedByDescending { routine ->
            routine.exercises.sumOf { exercise ->
                musclesNeeded[exercise.bodyFocus] ?: 0
            }
        }

        val schedule = mutableListOf<ScheduledWorkout>()
        val usedRoutines = mutableSetOf<Long>()
        var routineIndex = 0

        for (day in 1..7) {
            val isRestDay = day in userPreferredRestDays
            if (isRestDay) {
                schedule.add(ScheduledWorkout(dayOfWeek = day, routineId = null, isRestDay = true))
            } else {
                val routine = sortedRoutines.getOrNull(routineIndex % sortedRoutines.size)
                if (routine != null && !usedRoutines.contains(routine.id)) {
                    schedule.add(ScheduledWorkout(dayOfWeek = day, routineId = routine.id, isRestDay = false))
                    usedRoutines.add(routine.id)
                    routineIndex++
                } else if (sortedRoutines.isNotEmpty()) {
                    schedule.add(ScheduledWorkout(dayOfWeek = day, routineId = sortedRoutines[0].id, isRestDay = false))
                } else {
                    schedule.add(ScheduledWorkout(dayOfWeek = day, routineId = null, isRestDay = true))
                }
            }
        }

        return schedule
    }

    private fun getThisWeeksWorkouts(): List<Routine> {
        return emptyList()
    }

    private fun generateDefaultSchedule(
        allRoutines: List<Routine>,
        userPreferredRestDays: Set<Int>
    ): List<ScheduledWorkout> {
        val schedule = mutableListOf<ScheduledWorkout>()
        val sortedRoutines = allRoutines.sortedBy { it.type }

        for (day in 1..7) {
            val isRestDay = day in userPreferredRestDays
            if (isRestDay) {
                schedule.add(ScheduledWorkout(dayOfWeek = day, routineId = null, isRestDay = true))
            } else {
                val routineIndex = schedule.count { !it.isRestDay } % sortedRoutines.size.coerceAtLeast(1)
                val routine = sortedRoutines.getOrNull(routineIndex)
                schedule.add(
                    ScheduledWorkout(
                        dayOfWeek = day,
                        routineId = routine?.id,
                        isRestDay = false
                    )
                )
            }
        }

        return schedule
    }

    private fun getWeekStartDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun getRoutineById(id: Long): Routine? {
        return routineRepository.getRoutineById(id)
    }
}

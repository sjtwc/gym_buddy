package com.csci3310.gymbuddy.domain.usecase

import com.csci3310.gymbuddy.data.local.dao.SetDao
import com.csci3310.gymbuddy.data.local.dao.WorkoutDao
import com.csci3310.gymbuddy.data.local.dao.PersonalRecordDao
import com.csci3310.gymbuddy.data.local.entity.WorkoutEntity
import com.csci3310.gymbuddy.data.local.entity.PersonalRecordEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

data class WorkoutSummary(
    val totalWorkouts: Int,
    val totalVolume: Float,
    val totalDuration: Int,
    val muscleGroupsTrained: Map<String, Float>,
    val personalRecords: List<PersonalRecordEntity>,
    val recordsBroken: Int
)

class GetWorkoutSummaryUseCase @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val personalRecordDao: PersonalRecordDao
) {
    suspend fun getWeeklySummary(): WorkoutSummary {
        val (startDate, endDate) = getWeekDateRange()
        return getSummaryForDateRange(startDate, endDate)
    }

    suspend fun get4WeekSummary(): WorkoutSummary {
        val (startDate, endDate) = get4WeekDateRange()
        return getSummaryForDateRange(startDate, endDate)
    }

    suspend fun getWeeklyComparison(): Map<String, Any> {
        val currentWeek = getWeeklySummary()
        val previous4Week = getPrevious4WeekSummary()
        
        val volumeChange = if (previous4Week.totalVolume > 0) {
            ((currentWeek.totalVolume - previous4Week.totalVolume) / previous4Week.totalVolume * 100)
        } else 0f
        
        val workoutChange = if (previous4Week.totalWorkouts > 0) {
            ((currentWeek.totalWorkouts - previous4Week.totalWorkouts).toFloat() / previous4Week.totalWorkouts * 100)
        } else 0f
        
        return mapOf(
            "currentWeekWorkouts" to currentWeek.totalWorkouts,
            "previous4WeekWorkouts" to previous4Week.totalWorkouts,
            "workoutChangePercent" to workoutChange,
            "currentWeekVolume" to currentWeek.totalVolume,
            "previous4WeekVolume" to previous4Week.totalVolume,
            "volumeChangePercent" to volumeChange,
            "currentWeekPRs" to currentWeek.recordsBroken,
            "previous4WeekPRs" to previousWeekRecords()
        )
    }

    private suspend fun getSummaryForDateRange(startDate: Long, endDate: Long): WorkoutSummary {
        val workouts = workoutDao.getWorkoutsByDateRangeSync(startDate, endDate)
        
        val totalWorkouts = workouts.size
        val totalDuration = workouts.sumOf { it.duration ?: 0 }
        
        var totalVolume = 0f
        val muscleGroups = mutableMapOf<String, Float>()
        
        for (muscle in listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core")) {
            val volume = setDao.getVolumeForMuscleGroup(startDate, endDate, muscle)
            if (volume > 0) {
                muscleGroups[muscle] = volume
                totalVolume += volume
            }
        }
        
        val prs = personalRecordDao.getPersonalRecordsForDateRangeSync(startDate, endDate)
        
        return WorkoutSummary(
            totalWorkouts = totalWorkouts,
            totalVolume = totalVolume,
            totalDuration = totalDuration,
            muscleGroupsTrained = muscleGroups,
            personalRecords = prs,
            recordsBroken = prs.size
        )
    }

    private suspend fun getPrevious4WeekSummary(): WorkoutSummary {
        val (startDate, endDate) = getPrevious4WeekDateRange()
        return getSummaryForDateRange(startDate, endDate)
    }

    private suspend fun previousWeekRecords(): Int {
        val (startDate, endDate) = getPreviousWeekDateRange()
        return personalRecordDao.getPersonalRecordsForDateRangeSync(startDate, endDate).size
    }

    private fun getWeekDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endDate = calendar.timeInMillis - 1
        
        return Pair(startDate, endDate)
    }

    private fun get4WeekDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, -3)
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.WEEK_OF_YEAR, 4)
        val endDate = calendar.timeInMillis - 1
        
        return Pair(startDate, endDate)
    }

    private fun getPrevious4WeekDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, -7)
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.WEEK_OF_YEAR, -4)
        val endDate = calendar.timeInMillis - 1
        
        return Pair(startDate, endDate)
    }

    private fun getPreviousWeekDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.timeInMillis
        
        return Pair(startDate, endDate)
    }
}
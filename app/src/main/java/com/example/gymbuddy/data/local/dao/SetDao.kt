package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    suspend fun getSetsForWorkoutExerciseOnce(workoutExerciseId: Long): List<SetEntity>
    
    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun getSetById(id: Long): SetEntity?
    
    @Query("""
        SELECT s.* FROM sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId
        ORDER BY s.completedAt DESC
        LIMIT :limit
    """)
    fun getPreviousSetsForExercise(exerciseId: Long, limit: Int): Flow<List<SetEntity>>
    
    @Query("""
        SELECT COALESCE(SUM(s.reps * s.weight), 0) as totalVolume
        FROM sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE w.date >= :startDate AND w.date <= :endDate AND w.isCompleted = 1
    """)
    suspend fun getTotalVolumeForDateRange(startDate: Long, endDate: Long): Float
    
    @Query("""
        SELECT COALESCE(SUM(s.reps * s.weight), 0) as totalVolume
        FROM sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN exercises e ON we.exerciseId = e.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE w.date >= :startDate AND w.date <= :endDate 
        AND w.isCompleted = 1 AND e.targetMuscle = :muscleGroup
    """)
    suspend fun getVolumeForMuscleGroup(startDate: Long, endDate: Long, muscleGroup: String): Float
    
    @Query("""
        SELECT COALESCE(SUM(s.reps * s.weight), 0) as dailyVolume, w.date as workoutDate
        FROM sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE w.date >= :startDate AND w.date <= :endDate AND w.isCompleted = 1
        GROUP BY DATE(w.date / 1000, 'unixepoch')
        ORDER BY w.date ASC
    """)
    suspend fun getDailyVolumes(startDate: Long, endDate: Long): List<DailyVolumeResult>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetEntity>)
    
    @Update
    suspend fun updateSet(set: SetEntity)
    
    @Delete
    suspend fun deleteSet(set: SetEntity)
    
    @Query("DELETE FROM sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteAllForWorkoutExercise(workoutExerciseId: Long)
@Query("""
        SELECT COALESCE(SUM(s.weight * s.reps), 0) 
        FROM sets s
        WHERE s.workoutExerciseId IN (
            SELECT id FROM workout_exercises WHERE workoutId = :workoutId
        )
    """)
    suspend fun getWorkoutVolume(workoutId: Long): Float
}

data class DailyVolumeResult(
    val dailyVolume: Float,
    val workoutDate: Long
)

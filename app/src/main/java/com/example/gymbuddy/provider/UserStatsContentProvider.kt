package com.example.gymbuddy.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.gymbuddy.data.local.GymBuddyDatabase
import com.example.gymbuddy.data.local.dao.ExerciseDao
import com.example.gymbuddy.data.local.dao.PersonalRecordDao
import com.example.gymbuddy.data.local.dao.SetDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserStatsContentProvider : ContentProvider() {

    companion object {
        private const val PERSONAL_RECORDS = 1
        private const val WORKOUT_STATS = 2
        private const val EXERCISE_STATS = 3
        private const val EXERCISE_RECORDS = 4

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(UserStatsContract.AUTHORITY, UserStatsContract.PersonalRecords.PATH, PERSONAL_RECORDS)
            addURI(UserStatsContract.AUTHORITY, UserStatsContract.WorkoutStats.PATH, WORKOUT_STATS)
            addURI(UserStatsContract.AUTHORITY, UserStatsContract.ExerciseStats.PATH, EXERCISE_STATS)
            addURI(UserStatsContract.AUTHORITY, "${UserStatsContract.PersonalRecords.PATH}/#", EXERCISE_RECORDS)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UserStatsContentProviderEntryPoint {
        fun database(): GymBuddyDatabase
    }

    private fun getDatabase(): GymBuddyDatabase {
        val appContext = context?.applicationContext ?: throw IllegalStateException("Context is null")
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            UserStatsContentProviderEntryPoint::class.java
        )
        return entryPoint.database()
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            PERSONAL_RECORDS -> queryPersonalRecords()
            WORKOUT_STATS -> queryWorkoutStats()
            EXERCISE_STATS -> queryExerciseStats()
            EXERCISE_RECORDS -> {
                val exerciseId = ContentUris.parseId(uri)
                queryPersonalRecordsForExercise(exerciseId)
            }
            else -> null
        }
    }

    private fun queryPersonalRecords(): Cursor {
        val database = getDatabase()
        val personalRecordDao = database.personalRecordDao()
        val exerciseDao = database.exerciseDao()

        val cursor = MatrixCursor(
            arrayOf(
                UserStatsContract.PersonalRecords.COLUMN_ID,
                UserStatsContract.PersonalRecords.COLUMN_EXERCISE_ID,
                UserStatsContract.PersonalRecords.COLUMN_EXERCISE_NAME,
                UserStatsContract.PersonalRecords.COLUMN_TYPE,
                UserStatsContract.PersonalRecords.COLUMN_VALUE,
                UserStatsContract.PersonalRecords.COLUMN_REPS,
                UserStatsContract.PersonalRecords.COLUMN_WEIGHT,
                UserStatsContract.PersonalRecords.COLUMN_DATE,
                UserStatsContract.PersonalRecords.COLUMN_WORKOUT_ID
            )
        )

        try {
            val records = kotlinx.coroutines.runBlocking {
                personalRecordDao.getAllRecords(100).first()
            }
            records.forEach { record ->
                val exercise = kotlinx.coroutines.runBlocking {
                    exerciseDao.getExerciseById(record.exerciseId)
                }
                cursor.addRow(
                    arrayOf(
                        record.id,
                        record.exerciseId,
                        exercise?.name ?: "Unknown",
                        record.type,
                        record.value,
                        record.reps,
                        record.weight,
                        record.date,
                        record.workoutId
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cursor
    }

    private fun queryPersonalRecordsForExercise(exerciseId: Long): Cursor {
        val database = getDatabase()
        val personalRecordDao = database.personalRecordDao()
        val exerciseDao = database.exerciseDao()

        val cursor = MatrixCursor(
            arrayOf(
                UserStatsContract.PersonalRecords.COLUMN_ID,
                UserStatsContract.PersonalRecords.COLUMN_EXERCISE_ID,
                UserStatsContract.PersonalRecords.COLUMN_EXERCISE_NAME,
                UserStatsContract.PersonalRecords.COLUMN_TYPE,
                UserStatsContract.PersonalRecords.COLUMN_VALUE,
                UserStatsContract.PersonalRecords.COLUMN_REPS,
                UserStatsContract.PersonalRecords.COLUMN_WEIGHT,
                UserStatsContract.PersonalRecords.COLUMN_DATE,
                UserStatsContract.PersonalRecords.COLUMN_WORKOUT_ID
            )
        )

        try {
            val records = kotlinx.coroutines.runBlocking {
                personalRecordDao.getRecordsForExercise(exerciseId).first()
            }
            val exercise = kotlinx.coroutines.runBlocking {
                exerciseDao.getExerciseById(exerciseId)
            }
            records.forEach { record ->
                cursor.addRow(
                    arrayOf(
                        record.id,
                        record.exerciseId,
                        exercise?.name ?: "Unknown",
                        record.type,
                        record.value,
                        record.reps,
                        record.weight,
                        record.date,
                        record.workoutId
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cursor
    }

    private fun queryWorkoutStats(): Cursor {
        val database = getDatabase()
        val workoutDao = database.workoutDao()
        val setDao = database.setDao()

        val cursor = MatrixCursor(
            arrayOf(
                UserStatsContract.WorkoutStats.COLUMN_TOTAL_WORKOUTS,
                UserStatsContract.WorkoutStats.COLUMN_TOTAL_VOLUME,
                UserStatsContract.WorkoutStats.COLUMN_CURRENT_STREAK,
                UserStatsContract.WorkoutStats.COLUMN_LONGEST_STREAK,
                UserStatsContract.WorkoutStats.COLUMN_TOTAL_TIME
            )
        )

        try {
            val workouts = kotlinx.coroutines.runBlocking {
                workoutDao.getAllWorkouts().first()
            }
            val completedWorkouts = workouts.filter { it.isCompleted }
            val totalVolume = completedWorkouts.sumOf { it.duration.toLong() }.toFloat()
            val totalTime = completedWorkouts.sumOf { (it.duration ?: 0).toLong() }

            cursor.addRow(
                arrayOf(
                    completedWorkouts.size,
                    totalVolume,
                    0,
                    0,
                    totalTime
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            cursor.addRow(arrayOf(0, 0f, 0, 0, 0))
        }

        return cursor
    }

    private fun queryExerciseStats(): Cursor {
        val database = getDatabase()
        val exerciseDao = database.exerciseDao()
        val personalRecordDao = database.personalRecordDao()

        val cursor = MatrixCursor(
            arrayOf(
                UserStatsContract.ExerciseStats.COLUMN_EXERCISE_ID,
                UserStatsContract.ExerciseStats.COLUMN_EXERCISE_NAME,
                UserStatsContract.ExerciseStats.COLUMN_BEST_WEIGHT,
                UserStatsContract.ExerciseStats.COLUMN_TOTAL_SETS,
                UserStatsContract.ExerciseStats.COLUMN_TOTAL_REPS
            )
        )

        try {
            val exercises = kotlinx.coroutines.runBlocking {
                exerciseDao.getAllExercises().first()
            }
            exercises.take(20).forEach { exercise ->
                val records = kotlinx.coroutines.runBlocking {
                    personalRecordDao.getRecordsForExercise(exercise.id).first()
                }
                val bestWeight = records.maxOfOrNull { it.weight } ?: 0f
                val totalSets = records.size
                val totalReps = records.sumOf { it.reps }

                cursor.addRow(
                    arrayOf(
                        exercise.id,
                        exercise.name,
                        bestWeight,
                        totalSets,
                        totalReps
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            PERSONAL_RECORDS -> UserStatsContract.PersonalRecords.CONTENT_TYPE
            WORKOUT_STATS -> UserStatsContract.WorkoutStats.CONTENT_TYPE
            EXERCISE_STATS -> UserStatsContract.ExerciseStats.CONTENT_TYPE
            EXERCISE_RECORDS -> UserStatsContract.PersonalRecords.CONTENT_ITEM_TYPE
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

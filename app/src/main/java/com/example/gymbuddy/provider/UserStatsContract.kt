package com.csci3310.gymbuddy.provider

import android.net.Uri

object UserStatsContract {
    const val AUTHORITY = "com.csci3310.gymbuddy.provider"
    val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")

    object PersonalRecords {
        const val PATH = "personal_records"
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, PATH)

        const val COLUMN_ID = "id"
        const val COLUMN_EXERCISE_ID = "exerciseId"
        const val COLUMN_EXERCISE_NAME = "exerciseName"
        const val COLUMN_TYPE = "type"
        const val COLUMN_VALUE = "value"
        const val COLUMN_REPS = "reps"
        const val COLUMN_WEIGHT = "weight"
        const val COLUMN_DATE = "date"
        const val COLUMN_WORKOUT_ID = "workoutId"

        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH"
    }

    object WorkoutStats {
        const val PATH = "workout_stats"
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, PATH)

        const val COLUMN_TOTAL_WORKOUTS = "totalWorkouts"
        const val COLUMN_TOTAL_VOLUME = "totalVolume"
        const val COLUMN_CURRENT_STREAK = "currentStreak"
        const val COLUMN_LONGEST_STREAK = "longestStreak"
        const val COLUMN_TOTAL_TIME = "totalTime"

        const val CONTENT_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH"
    }

    object ExerciseStats {
        const val PATH = "exercise_stats"
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, PATH)

        const val COLUMN_EXERCISE_ID = "exerciseId"
        const val COLUMN_EXERCISE_NAME = "exerciseName"
        const val COLUMN_BEST_WEIGHT = "bestWeight"
        const val COLUMN_BEST_VOLUME = "bestVolume"
        const val COLUMN_TOTAL_SETS = "totalSets"
        const val COLUMN_TOTAL_REPS = "totalReps"

        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH"
    }

    object DailySummary {
        const val PATH = "daily_summary"
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, PATH)

        const val COLUMN_CURRENT_STREAK = "currentStreak"
        const val COLUMN_WORKOUTS_THIS_WEEK = "workoutsThisWeek"
        const val COLUMN_TODAY_COMPLETED = "todayCompleted"
        const val COLUMN_WEEKLY_TARGET = "weeklyTarget"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_TITLE = "title"

        const val CONTENT_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH"
    }
}

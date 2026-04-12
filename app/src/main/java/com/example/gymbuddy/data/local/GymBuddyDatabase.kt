package com.example.gymbuddy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymbuddy.data.local.dao.*
import com.example.gymbuddy.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        SetEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        UserProfileEntity::class,
        PersonalRecordEntity::class,
        BodyMeasurementEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymBuddyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun setDao(): SetDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        @Volatile
        private var INSTANCE: GymBuddyDatabase? = null
        
        fun getDatabase(context: Context): GymBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymBuddyDatabase::class.java,
                    "gym_buddy_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        suspend fun populateDatabase(database: GymBuddyDatabase) {
            val exercises = listOf(
                ExerciseEntity(
                    id = 1,
                    name = "Barbell Bench Press",
                    description = "Classic chest exercise using a barbell",
                    targetMuscle = "Chest",
                    secondaryMuscles = "Triceps, Shoulders",
                    equipmentType = "Barbell",
                    instructions = "1. Lie on bench with feet flat\n2. Grip bar slightly wider than shoulders\n3. Lower bar to chest\n4. Press up to start\n5. Keep core tight throughout",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 2,
                    name = "Deadlift",
                    description = "Full body compound lift",
                    targetMuscle = "Back",
                    secondaryMuscles = "Hamstrings, Glutes, Core",
                    equipmentType = "Barbell",
                    instructions = "1. Stand with feet hip-width\n2. Grip bar outside knees\n3. Keep back straight\n4. Drive through heels\n5. Stand up tall, squeeze glutes",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 3,
                    name = "Overhead Press",
                    description = "Standing barbell overhead press",
                    targetMuscle = "Shoulders",
                    secondaryMuscles = "Triceps, Core",
                    equipmentType = "Barbell",
                    instructions = "1. Stand with feet shoulder-width\n2. Hold bar at shoulder level\n3. Press overhead\n4. Lock out arms\n5. Lower with control",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 4,
                    name = "Barbell Row",
                    description = "Bent over barbell row",
                    targetMuscle = "Back",
                    secondaryMuscles = "Biceps, Rear Delts",
                    equipmentType = "Barbell",
                    instructions = "1. Bend at hips, keep back straight\n2. Grip bar shoulder-width\n3. Pull to lower chest\n4. Squeeze shoulder blades\n5. Lower with control",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 5,
                    name = "Barbell Curl",
                    description = "Classic bicep curl",
                    targetMuscle = "Arms",
                    secondaryMuscles = "Forearms",
                    equipmentType = "Barbell",
                    instructions = "1. Stand with arms extended\n2. Grip bar shoulder-width\n3. Curl weight up\n4. Squeeze at top\n5. Lower with control",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 6,
                    name = "Tricep Pushdown",
                    description = "Cable tricep pushdown",
                    targetMuscle = "Arms",
                    secondaryMuscles = "",
                    equipmentType = "Cable",
                    instructions = "1. Face cable machine\n2. Grip bar or rope\n3. Keep elbows at sides\n4. Push down until straight\n5. Return with control",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 7,
                    name = "Barbell Squat",
                    description = "Classic barbell back squat",
                    targetMuscle = "Legs",
                    secondaryMuscles = "Glutes, Core",
                    equipmentType = "Barbell",
                    instructions = "1. Place bar on upper back\n2. Feet shoulder-width apart\n3. Squat down keeping back straight\n4. Drive through heels\n5. Stand up squeezing glutes",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 8,
                    name = "Romanian Deadlift",
                    description = "Hip hinge movement for hamstrings",
                    targetMuscle = "Legs",
                    secondaryMuscles = "Glutes, Back",
                    equipmentType = "Barbell",
                    instructions = "1. Hold bar at hip level\n2. Slight knee bend\n3. Push hips back\n4. Feel hamstring stretch\n5. Return to standing",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 9,
                    name = "Plank",
                    description = "Core stability exercise",
                    targetMuscle = "Core",
                    secondaryMuscles = "Shoulders, Back",
                    equipmentType = "Bodyweight",
                    instructions = "1. Forearms on ground\n2. Body in straight line\n3. Engage core\n4. Hold position\n5. Don't let hips sag",
                    isCustom = false
                ),
                ExerciseEntity(
                    id = 10,
                    name = "Lat Pulldown",
                    description = "Cable lat pulldown",
                    targetMuscle = "Back",
                    secondaryMuscles = "Biceps",
                    equipmentType = "Cable",
                    instructions = "1. Sit at machine\n2. Grip bar wide\n3. Pull to upper chest\n4. Squeeze lats\n5. Control the return",
                    isCustom = false
                )
            )
            database.exerciseDao().insertExercises(exercises)
            
            val userProfile = UserProfileEntity(
                id = 1,
                name = "Trainer",
                petName = "GymBot",
                petHappiness = 50,
                petMood = "neutral"
            )
            database.userProfileDao().insertUserProfile(userProfile)
            
            val routines = listOf(
                RoutineEntity(
                    id = 1,
                    name = "Push Day",
                    description = "Chest, Shoulders, Triceps",
                    type = "Push",
                    difficulty = "Intermediate",
                    estimatedMinutes = 60,
                    isCustom = false
                ),
                RoutineEntity(
                    id = 2,
                    name = "Pull Day",
                    description = "Back, Biceps",
                    type = "Pull",
                    difficulty = "Intermediate",
                    estimatedMinutes = 55,
                    isCustom = false
                ),
                RoutineEntity(
                    id = 3,
                    name = "Leg Day",
                    description = "Quads, Hamstrings, Glutes",
                    type = "Legs",
                    difficulty = "Intermediate",
                    estimatedMinutes = 65,
                    isCustom = false
                )
            )
            routines.forEach { database.routineDao().insertRoutine(it) }
            
            val routineExercises = listOf(
                RoutineExerciseEntity(routineId = 1, exerciseId = 1, orderIndex = 0, targetSets = 4, targetReps = "6-10"),
                RoutineExerciseEntity(routineId = 1, exerciseId = 3, orderIndex = 1, targetSets = 3, targetReps = "8-12"),
                RoutineExerciseEntity(routineId = 1, exerciseId = 6, orderIndex = 2, targetSets = 3, targetReps = "10-15"),
                RoutineExerciseEntity(routineId = 2, exerciseId = 2, orderIndex = 0, targetSets = 4, targetReps = "5-8"),
                RoutineExerciseEntity(routineId = 2, exerciseId = 4, orderIndex = 1, targetSets = 3, targetReps = "8-12"),
                RoutineExerciseEntity(routineId = 2, exerciseId = 10, orderIndex = 2, targetSets = 3, targetReps = "10-12"),
                RoutineExerciseEntity(routineId = 2, exerciseId = 5, orderIndex = 3, targetSets = 3, targetReps = "10-12"),
                RoutineExerciseEntity(routineId = 3, exerciseId = 7, orderIndex = 0, targetSets = 4, targetReps = "6-10"),
                RoutineExerciseEntity(routineId = 3, exerciseId = 8, orderIndex = 1, targetSets = 3, targetReps = "8-12"),
                RoutineExerciseEntity(routineId = 3, exerciseId = 9, orderIndex = 2, targetSets = 3, targetReps = "30-60s")
            )
            database.routineExerciseDao().insertRoutineExercises(routineExercises)
        }
    }
}
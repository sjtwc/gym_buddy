# Gym Buddy Database Documentation

## Overview

The Gym Buddy app uses an **offline-first** architecture with **Room** as the local database. The database contains 9 tables with proper relationships, indexes, and foreign key constraints to ensure data integrity.

### Database Specifications

- **Database Name**: `gym_buddy_database`
- **Version**: 7
- **Export Schema**: Disabled (for development)

---

## Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────────────┐       ┌─────────────────┐
│   UserProfile   │       │       Workout          │       │    Routine      │
│   (Singleton)   │       │                         │       │                 │
├─────────────────┤       ├─────────────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)                 │       │ id (PK)         │
│ name            │       │ name                    │       │ name            │
│ level           │       │ date                    │       │ description     │
│ xp              │       │ duration                │       │ type            │
│ currentStreak   │       │ routine_id (FK)         │       │ difficulty      │
│ longestStreak   │       │ is_completed            │       │ estimated_min   │
│ totalWorkouts   │       └───────────┬─────────────┘       │ is_custom       │
│ totalVolume     │                   │                     │ is_favorite     │
│ petName         │                   │                     └────────┬────────┘
│ petHappiness    │                   │                              │
│ petMood         │                   │                              │
└─────────────────┘                   │                              │
                                      │                              │
                                      ▼                              ▼
┌──────────────────────┐    ┌─────────────────────────────┐    ┌──────────────────┐
│     Exercise        │    │   WorkoutExercise           │    │ RoutineExercise │
│                      │    │                             │    │                  │
├──────────────────────┤    ├─────────────────────────────┤    ├──────────────────┤
│ id (PK)             │    │ id (PK)                    │    │ id (PK)          │
│ name                │    │ workout_id (FK) ──────────►│    │ routine_id (FK) │
│ description         │    │ exercise_id (FK) ──────────►│    │ exercise_id (FK)│
│ target_muscle       │    │ order_index                │    │ order_index     │
│ secondary_muscles   │    │ notes                      │    │ target_sets     │
│ equipment_type      │    │ rest_timer_seconds         │    │ target_reps     │
│ video_url           │    │ target_reps                │    │ rest_seconds    │
│ instructions        │    └───────────┬─────────────────┘    │ notes           │
│ is_custom           │                │                     └──────────────────┘
│ created_at          │                ▼
└──────────────────┘    ┌─────────────────────┐
                        │        Set          │
                        ├─────────────────────┤
                        │ id (PK)             │
                        │ workout_exercise_id │
                        │ (FK) ──────────────►│
                        │ set_number          │
                        │ reps                │
                        │ weight              │
                        │ rpe                 │
                        │ is_warm_up          │
                        │ is_drop_set         │
                        │ is_failure_set      │
                        │ is_superset         │
                        │ notes               │
                        │ completed_at        │
                        │ previous_set_id     │
                        └─────────────────────┘

┌──────────────────────┐    ┌──────────────────────┐    ┌──────────────────────┐
│ PersonalRecord      │    │ BodyMeasurement     │    │   RoutineSet        │
│                     │    │                     │    │                      │
├─────────────────────┤    ├─────────────────────┤    ├──────────────────────┤
│ id (PK)             │    │ id (PK)             │    │ id (PK)              │
│ exercise_id (FK) ──►│    │ date                │    │ routine_exercise_id  │
│ type                │    │ weight              │    │ set_type             │
│ value               │    │ body_fat            │    │ target_reps          │
│ reps                │    │ chest               │    │ target_weight        │
│ weight              │    │ waist               │    │ rest_seconds         │
│ date                │    │ hips                │    └──────────────────────┘
│ workout_id (FK)     │    │ biceps              │
└─────────────────────┘    │ thighs              │    ┌──────────────────────┐
                            │ calves              │    │  RoutineTimer        │
                            │ shoulders           │    │                      │
                            │ notes               │    ├──────────────────────┤
                            └─────────────────────┘    │ id (PK)               │
                                                      │ routine_exercise_id   │
                                                      │ set_type              │
                                                      │ timer_seconds         │
                                                      └──────────────────────┘
```

---

## Table Definitions

### 1. Exercise Entity

**Table Name**: `exercises`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `name` | `String` | NOT NULL | Exercise name |
| `description` | `String` | NOT NULL | Brief description |
| `target_muscle` | `String` | NOT NULL | Primary muscle group |
| `secondary_muscles` | `String` | NULLABLE | Comma-separated secondary muscles |
| `equipment_type` | `String` | NOT NULL | Equipment required |
| `video_url` | `String` | NULLABLE | URL to exercise video |
| `instructions` | `String` | NOT NULL | Step-by-step instructions |
| `is_custom` | `Boolean` | NOT NULL, DEFAULT FALSE | User-created exercise |
| `created_at` | `Long` | NOT NULL | Unix timestamp |

**Indexes**: None (uses primary key for lookups)

---

### 2. Workout Entity

**Table Name**: `workouts`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `name` | `String` | NOT NULL | Workout name |
| `date` | `Long` | NOT NULL | Workout date (unix timestamp) |
| `duration` | `Int` | NOT NULL, DEFAULT 0 | Duration in minutes |
| `notes` | `String` | NULLABLE | Workout notes |
| `routine_id` | `Long` | NULLABLE, FK → `routines.id` | Source routine |
| `is_completed` | `Boolean` | NOT NULL, DEFAULT FALSE | Completion status |
| `created_at` | `Long` | NOT NULL | Unix timestamp |

**Foreign Keys**:
- `routine_id` → `routines.id` (ON DELETE SET NULL)

**Indexes**:
- `idx_workouts_date` ON `date`
- `idx_workouts_is_completed` ON `is_completed`

---

### 3. WorkoutExercise Entity

**Table Name**: `workout_exercises`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `workout_id` | `Long` | NOT NULL, FK → `workouts.id` | Parent workout |
| `exercise_id` | `Long` | NOT NULL, FK → `exercises.id` | Exercise used |
| `order_index` | `Int` | NOT NULL | Order in workout |
| `notes` | `String` | NULLABLE | Exercise-specific notes |
| `rest_timer_seconds` | `Int` | NOT NULL, DEFAULT 90 | Default rest time |
| `target_reps` | `String` | NULLABLE | Target rep range from routine |

**Foreign Keys**:
- `workout_id` → `workouts.id` (ON DELETE CASCADE)
- `exercise_id` → `exercises.id` (ON DELETE CASCADE)

**Indexes**:
- `idx_workout_exercises_workout_id` ON `workout_id`
- `idx_workout_exercises_exercise_id` ON `exercise_id`

---

### 4. Set Entity

**Table Name**: `sets`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `workout_exercise_id` | `Long` | NOT NULL, FK → `workout_exercises.id` | Parent workout exercise |
| `set_number` | `Int` | NOT NULL | Set number (1, 2, 3...) |
| `reps` | `Int` | NOT NULL | Number of reps |
| `weight` | `Float` | NOT NULL | Weight lifted (kg) |
| `rpe` | `Int` | NULLABLE | Rate of Perceived Exertion (1-10) |
| `is_warm_up` | `Boolean` | NOT NULL, DEFAULT FALSE | Warm-up set flag |
| `is_drop_set` | `Boolean` | NOT NULL, DEFAULT FALSE | Drop set flag |
| `is_failure_set` | `Boolean` | NOT NULL, DEFAULT FALSE | Failure set flag |
| `is_superset` | `Boolean` | NOT NULL, DEFAULT FALSE | Superset flag |
| `notes` | `String` | NULLABLE | Set notes |
| `completed_at` | `Long` | NULLABLE | Completion timestamp |
| `previous_set_id` | `Long` | NULLABLE | Previous set for comparison |

**Foreign Keys**:
- `workout_exercise_id` → `workout_exercises.id` (ON DELETE CASCADE)

**Indexes**:
- `idx_sets_workout_exercise_id` ON `workout_exercise_id`

---

### 5. Routine Entity

**Table Name**: `routines`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `name` | `String` | NOT NULL | Routine name |
| `description` | `String` | NULLABLE | Routine description |
| `type` | `String` | NOT NULL | Routine type (Push/Pull/Legs/Upper/Lower/Full Body) |
| `difficulty` | `String` | NOT NULL | Difficulty level |
| `estimated_minutes` | `Int` | NOT NULL, DEFAULT 60 | Estimated duration |
| `is_custom` | `Boolean` | NOT NULL, DEFAULT TRUE | User-created |
| `is_favorite` | `Boolean` | NOT NULL, DEFAULT FALSE | Favorite flag |
| `created_at` | `Long` | NOT NULL | Unix timestamp |

**Indexes**:
- `idx_routines_type` ON `type`
- `idx_routines_is_favorite` ON `is_favorite`

---

### 6. RoutineExercise Entity

**Table Name**: `routine_exercises`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `routine_id` | `Long` | NOT NULL, FK → `routines.id` | Parent routine |
| `exercise_id` | `Long` | NOT NULL, FK → `exercises.id` | Exercise |
| `order_index` | `Int` | NOT NULL | Order in routine |
| `target_sets` | `Int` | NOT NULL, DEFAULT 3 | Target number of sets |
| `target_reps` | `String` | NOT NULL, DEFAULT "8-12" | Target rep range |
| `rest_seconds` | `Int` | NOT NULL, DEFAULT 90 | Rest between sets |
| `notes` | `String` | NULLABLE | Exercise notes |

**Foreign Keys**:
- `routine_id` → `routines.id` (ON DELETE CASCADE)
- `exercise_id` → `exercises.id` (ON DELETE CASCADE)

**Indexes**:
- `idx_routine_exercises_routine_id` ON `routine_id`
- `idx_routine_exercises_exercise_id` ON `exercise_id`

---

### 7. RoutineSet Entity

**Table Name**: `routine_sets`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `routine_exercise_id` | `Long` | NOT NULL, FK → `routine_exercises.id` | Parent routine exercise |
| `set_type` | `String` | NOT NULL, DEFAULT "normal" | Set type (normal, warmup, work, drop, failure) |
| `target_reps` | `String` | NOT NULL, DEFAULT "8-12" | Target rep range |
| `target_weight` | `Float` | NOT NULL, DEFAULT 0 | Target weight (kg) |
| `rest_seconds` | `Int` | NOT NULL, DEFAULT 90 | Rest between sets |

**Foreign Keys**:
- `routine_exercise_id` → `routine_exercises.id` (ON DELETE CASCADE)

**Indexes**:
- `idx_routine_sets_routine_exercise_id` ON `routine_exercise_id`

---

### 8. RoutineTimer Entity

**Table Name**: `routine_timers`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `routine_exercise_id` | `Long` | NOT NULL, FK → `routine_exercises.id` | Parent routine exercise |
| `set_type` | `String` | NOT NULL | Set type (normal, warmup, work, drop, failure) |
| `timer_seconds` | `Int` | NOT NULL, DEFAULT 90 | Rest timer duration |

**Foreign Keys**:
- `routine_exercise_id` → `routine_exercises.id` (ON DELETE CASCADE)

**Indexes**:
- `idx_routine_timers_routine_exercise_id` ON `routine_exercise_id`

---

### 10. UserProfile Entity

**Table Name**: `user_profile`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY (always 1) | Singleton profile |
| `name` | `String` | NOT NULL, DEFAULT "Trainer" | User name |
| `level` | `Int` | NOT NULL, DEFAULT 1 | User level |
| `xp` | `Int` | NOT NULL, DEFAULT 0 | Experience points |
| `current_streak` | `Int` | NOT NULL, DEFAULT 0 | Current workout streak |
| `longest_streak` | `Int` | NOT NULL, DEFAULT 0 | Best streak |
| `total_workouts` | `Int` | NOT NULL, DEFAULT 0 | Total completed workouts |
| `total_volume` | `Float` | NOT NULL, DEFAULT 0 | Total volume lifted |
| `pet_name` | `String` | NOT NULL, DEFAULT "GymBot" | Pet name |
| `pet_happiness` | `Int` | NOT NULL, DEFAULT 50 | Pet happiness (0-100) |
| `pet_mood` | `String` | NOT NULL, DEFAULT "neutral" | Pet mood |
| `last_workout_date` | `Long` | NULLABLE | Last workout timestamp |
| `title` | `String` | NOT NULL, DEFAULT "Novice" | User title based on level |
| `gender` | `String` | NULLABLE | User gender |
| `age` | `Int` | NULLABLE | User age |
| `height` | `Float` | NULLABLE | User height (cm) |
| `weight` | `Float` | NULLABLE | User weight (kg) |
| `avatar_uri` | `String` | NULLABLE | User avatar image URI |
| `created_at` | `Long` | NOT NULL | Unix timestamp |

**Indexes**: None (singleton table)

---

### 11. PersonalRecord Entity

**Table Name**: `personal_records`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `exercise_id` | `Long` | NOT NULL, FK → `exercises.id` | Exercise |
| `type` | `String` | NOT NULL | Record type (1RM, Volume, Reps, Weight) |
| `value` | `Float` | NOT NULL | Record value |
| `reps` | `Int` | NOT NULL | Reps at record |
| `weight` | `Float` | NOT NULL | Weight at record |
| `date` | `Long` | NOT NULL | Record date |
| `workout_id` | `Long` | NULLABLE, FK → `workouts.id` | Source workout |

**Foreign Keys**:
- `exercise_id` → `exercises.id` (ON DELETE CASCADE)
- `workout_id` → `workouts.id` (ON DELETE SET NULL)

**Indexes**:
- `idx_personal_records_exercise_id` ON `exercise_id`
- `idx_personal_records_type` ON `type`

---

### 12. BodyMeasurement Entity

**Table Name**: `body_measurements`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `date` | `Long` | NOT NULL | Measurement date |
| `weight` | `Float` | NULLABLE | Body weight (kg) |
| `body_fat` | `Float` | NULLABLE | Body fat percentage |
| `chest` | `Float` | NULLABLE | Chest circumference (cm) |
| `waist` | `Float` | NULLABLE | Waist circumference (cm) |
| `hips` | `Float` | NULLABLE | Hips circumference (cm) |
| `biceps` | `Float` | NULLABLE | Biceps circumference (cm) |
| `thighs` | `Float` | NULLABLE | Thighs circumference (cm) |
| `calves` | `Float` | NULLABLE | Calves circumference (cm) |
| `shoulders` | `Float` | NULLABLE | Shoulders circumference (cm) |
| `notes` | `String` | NULLABLE | Measurement notes |

**Indexes**:
- `idx_body_measurements_date` ON `date`

---

### 13. Achievement Entity

**Table Name**: `achievements`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `Long` | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `type` | `String` | NOT NULL | Achievement type enum |
| `earned_at` | `Long` | NOT NULL | Timestamp when earned |
| `claim_count` | `Int` | NOT NULL, DEFAULT 0 | Times this achievement was earned |
| `workout_id` | `Long` | NULLABLE, FK → `workouts.id` | Associated workout |

**Foreign Keys**:
- `workout_id` → `workouts.id` (ON DELETE SET NULL)

**Indexes**:
- `idx_achievements_type` ON `type`

---

### 14. ScheduledWorkout Entity

**Table Name**: `scheduled_workouts`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `schedule_key` | `String` | PRIMARY KEY | Composite key: `{weekStartDate}_{dayOfWeek}` |
| `week_start_date` | `Long` | NOT NULL | Start of the week (Unix timestamp) |
| `day_of_week` | `Int` | NOT NULL | Day of week (0=Sunday, 6=Saturday) |
| `routine_id` | `Long` | NULLABLE, FK → `routines.id` | Assigned routine |
| `is_rest_day` | `Boolean` | NOT NULL, DEFAULT FALSE | Whether this is a rest day |

**Foreign Keys**:
- `routine_id` → `routines.id` (ON DELETE SET NULL)

**Indexes**:
- `idx_scheduled_workouts_week_start_date` ON `week_start_date`

---

## DAO Methods

### ExerciseDao

```kotlin
// Get all exercises ordered by target muscle
fun getAllExercises(): Flow<List<ExerciseEntity>>

// Get exercises by muscle group
fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>>

// Get single exercise by ID
suspend fun getExerciseById(id: Long): ExerciseEntity?

// Search exercises by name
fun searchExercises(query: String): Flow<List<ExerciseEntity>>

// Insert exercise
suspend fun insertExercise(exercise: ExerciseEntity): Long

// Insert multiple exercises
suspend fun insertExercises(exercises: List<ExerciseEntity>)

// Update exercise
suspend fun updateExercise(exercise: ExerciseEntity)

// Delete exercise
suspend fun deleteExercise(exercise: ExerciseEntity)

// Get exercise count
suspend fun getExerciseCount(): Int
```

### WorkoutDao

```kotlin
// Get all workouts ordered by date
fun getAllWorkouts(): Flow<List<WorkoutEntity>>

// Get workout by ID
suspend fun getWorkoutById(id: Long): WorkoutEntity?

// Get workouts in date range
fun getWorkoutsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>

// Get active (incomplete) workout
suspend fun getActiveWorkout(): WorkoutEntity?

// Get recent completed workouts
fun getRecentCompletedWorkouts(limit: Int): Flow<List<WorkoutEntity>>

// Get completed workout count
fun getCompletedWorkoutCount(): Flow<Int>

// Get total workout duration
fun getTotalWorkoutDuration(): Flow<Int?>

// Insert workout
suspend fun insertWorkout(workout: WorkoutEntity): Long

// Update workout
suspend fun updateWorkout(workout: WorkoutEntity)

// Delete workout
suspend fun deleteWorkout(workout: WorkoutEntity)
```

### SetDao

```kotlin
// Get sets for workout exercise
fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<SetEntity>>

// Get set by ID
suspend fun getSetById(id: Long): SetEntity?

// Get previous sets for exercise (for comparison)
fun getPreviousSetsForExercise(exerciseId: Long, limit: Int): Flow<List<SetEntity>>

// Insert set
suspend fun insertSet(set: SetEntity): Long

// Insert multiple sets
suspend fun insertSets(sets: List<SetEntity>)

// Update set
suspend fun updateSet(set: SetEntity)

// Delete set
suspend fun deleteSet(set: SetEntity)

// Delete all sets for workout exercise
suspend fun deleteAllForWorkoutExercise(workoutExerciseId: Long)

// Get total volume for a muscle group within a date range
fun getVolumeForMuscleGroup(targetMuscle: String, startDate: Long, endDate: Long): Flow<Float>

// Get daily volumes for the last N days
fun getDailyVolumes(days: Int): Flow<List<DailyVolume>>
```

### UserProfileDao

```kotlin
// Get user profile as flow
fun getUserProfile(): Flow<UserProfileEntity?>

// Get user profile synchronously
suspend fun getUserProfileSync(): UserProfileEntity?

// Insert or update user profile
suspend fun insertUserProfile(userProfile: UserProfileEntity)

// Update user profile
suspend fun updateUserProfile(userProfile: UserProfileEntity)

// Update streak
suspend fun updateStreak(streak: Int)

// Add XP and check for level up
suspend fun addXp(xpAmount: Int)

// Update pet status
suspend fun updatePetStatus(happiness: Int, mood: String)
```

### PersonalRecordDao

```kotlin
// Get records for exercise
fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>

// Get records by type
fun getRecordsByType(type: String): Flow<List<PersonalRecordEntity>>

// Get all records with limit
fun getAllRecords(limit: Int): Flow<List<PersonalRecordEntity>>

// Insert record
suspend fun insertRecord(record: PersonalRecordEntity): Long

// Delete record
suspend fun deleteRecord(record: PersonalRecordEntity)

// Reset all personal records
suspend fun deleteAllRecords()
```

### AchievementDao

```kotlin
// Get all earned achievements
fun getAllAchievements(): Flow<List<AchievementEntity>>

// Get achievements by type
fun getAchievementsByType(type: String): Flow<List<AchievementEntity>>

// Insert achievement
suspend fun insertAchievement(achievement: AchievementEntity)

// Update claim count
suspend fun updateClaimCount(id: Long, count: Int)

// Reset all achievements
suspend fun deleteAllAchievements()
```

### ScheduledWorkoutDao

```kotlin
// Get scheduled workouts for a week
fun getScheduledWorkoutsForWeek(weekStartDate: Long): Flow<List<ScheduledWorkoutEntity>>

// Get single scheduled workout for a day
suspend fun getScheduledWorkout(weekStartDate: Long, dayOfWeek: Int): ScheduledWorkoutEntity?

// Insert or update scheduled workout
suspend fun insertScheduledWorkout(scheduledWorkout: ScheduledWorkoutEntity)

// Insert multiple scheduled workouts
suspend fun insertScheduledWorkouts(scheduledWorkouts: List<ScheduledWorkoutEntity>)

// Delete scheduled workout
suspend fun deleteScheduledWorkout(scheduledWorkout: ScheduledWorkoutEntity)

// Clear all scheduled workouts for a week
suspend fun clearWeekSchedule(weekStartDate: Long)
```

---

## Pre-populated Data

### Default Exercises (10)

| ID | Name | Target Muscle | Equipment |
|----|------|---------------|------------|
| 1 | Barbell Bench Press | Chest | Barbell |
| 2 | Deadlift | Back | Barbell |
| 3 | Overhead Press | Shoulders | Barbell |
| 4 | Barbell Row | Back | Barbell |
| 5 | Barbell Curl | Arms | Barbell |
| 6 | Tricep Pushdown | Arms | Cable |
| 7 | Barbell Squat | Legs | Barbell |
| 8 | Romanian Deadlift | Legs | Barbell |
| 9 | Plank | Core | Bodyweight |
| 10 | Lat Pulldown | Back | Cable |

### Default Routines (3)

| ID | Name | Type | Difficulty | Exercises |
|----|------|------|------------|-----------|
| 1 | Push Day | Push | Intermediate | Bench Press, Overhead Press, Tricep Pushdown |
| 2 | Pull Day | Pull | Intermediate | Deadlift, Barbell Row, Lat Pulldown, Barbell Curl |
| 3 | Leg Day | Legs | Intermediate | Barbell Squat, Romanian Deadlift, Plank |

---

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                │
│  (Compose Screens ←→ ViewModels ←→ StateFlow)                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Repository Layer                             │
│  (ExerciseRepository, WorkoutRepository, UserProfileRepository)│
│  - Transforms Entity ↔ Domain Model                            │
│  - Provides Flow-based data access                              │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DAO Layer                                 │
│  (ExerciseDao, WorkoutDao, SetDao, etc.)                      │
│  - Room queries with Flow return types                          │
│  - Suspend functions for write operations                       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Database Layer                                │
│  (GymBuddyDatabase - Room Database)                             │
│  - SQLite with proper indexing                                  │
│  - Foreign key constraints                                      │
│  - Pre-populated on first create                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Design Decisions

1. **Flow-based Queries**: All read operations return `Flow` for reactive UI updates
2. **Cascade Deletes**: Workout cascades to exercises and sets; Routines cascade to routine exercises
3. **Soft Deletes**: Not implemented - using hard deletes for simplicity
4. **Nullable Foreign Keys**: Workout.routine_id and PersonalRecord.workout_id can be NULL
5. **Singleton Profile**: UserProfile table always has id=1 (upsert pattern)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Initial | 9 tables, DAOs, repositories |
| 3 | 2026-04-13 | Routine redesign: added routine_sets and routine_timers tables |
| 4→5 | 2026-04-22 | Added routine_sets and routine_timers tables with indices; Added target_reps column to workout_exercises |
| 6 | 2026-04-22 | Added scheduled_workouts table for weekly schedule persistence |
| 7 | 2026-04-23 | Added avatar_uri column to user_profile; title field added to entity |

**Note**: Database is currently at version 6 (exportSchema disabled). The v1.13.0 profile edit fields (gender, age, height, weight) were added to the existing UserProfileEntity without requiring a migration since all fields are nullable.
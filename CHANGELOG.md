# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.13.0] - 2026-04-23

### Fixed
- **Actual Records Only in Exercise Record Tab** - Best Record column now shows only actual workout records
  - "Best 1RM" summary only shows if user performed an actual 1-rep set
  - Best Record column only shows weight for exact rep count (e.g., 2RM shows "-" if never performed)
  - Removed Epley calculation from Best Record column display
  - Prediction column remains unchanged

### Added
- **Profile Edit Screen** - Full screen edit mode for user profile
  - Edit name, pet name, gender, age, height, and weight
  - Gender dropdown selection (Male, Female, Other, Prefer not to say)
  - Edit button on Profile screen header

### Updated
- `UserProfileEntity.kt` - Added gender, age, height, weight fields
- `UserProfile.kt` (domain) - Added same fields
- `UserProfileRepository.kt` - Updated toEntity/toDomain mappings
- `ProfileScreen.kt` - Added edit button next to settings
- `Screen.kt` - Added ProfileEdit route
- `GymBuddyNavigation.kt` - Added ProfileEditScreen composable
- Database schema version bumped to 7

### New Files
- `ProfileEditScreen.kt` - Full screen edit form with Material3 inputs
- `ProfileEditViewModel.kt` - ViewModel for profile editing

---

## [1.12.0] - 2026-04-23

### Fixed
- **Predicted 1RM Calculation** - Fixed bug where larger rep counts showed larger predicted results instead of smaller
  - Now uses inverse Epley formula: `weight = baseRM / (1 + reps/30f)`
  - 1.5 month (6 weeks) time window for base record selection
  - Finds best actual record within window (prioritizes lower rep counts: 1RM > 2RM > 3RM...)
  - Only updates prediction if new calculated value is higher than current

### Added
- **Reset Records Button** - New button in Settings → "Your Stats" section
  - Clears all personal records (1RM-12RM) and predicted records
  - Confirmation dialog before reset with warning text

### Updated
- `ExerciseInsightViewModel.kt` - Rewrote `calculatePredictedRecords()` with correct logic
- `ExerciseInsightViewModel.kt` - Added `resetPredictedRecords()` method
- `SettingsViewModel.kt` - Added `resetAllRecords()` with PersonalRecordDao
- `SettingsScreen.kt` - Added reset button UI with confirmation dialog

---

## [1.11.1] - 2026-04-22

### Fixed
- **Weekly Schedule Updates** - Fixed schedule day selection not being saved correctly
  - Changed ScheduledWorkoutEntity primary key from auto-generated ID to composite `scheduleKey` (weekStartDate_dayOfWeek)
  - Added `onConfirm` callback to `DayPickerDialog` for proper dialog dismissal
- **Exercises Not Loading in Workout** - Fixed race condition where exercises weren't loaded when starting workout from routine
  - `WorkoutSessionManager` now queues exercise loading and executes after service binding completes
  - Added `loadExercisesInternal()` method and pending state tracking
  - `onServiceConnected` callback now loads pending exercises after service is bound

### Added
- **Routine Sets and Timers Persistence** - Properly persist set data (reps, weight, setType) and timer configurations to database
  - Added `RoutineSetEntity` and `RoutineTimerEntity` tables
  - Added `RoutineSetDao` and `RoutineTimerDao` with CRUD operations
  - Updated `RoutineRepository` to save/load sets and timers to separate tables
- **Target Reps Display** - Show target rep range as placeholder in workout logging input
  - Added `targetReps` field to `WorkoutExerciseEntity`, `WorkoutExercise`, `WorkoutExerciseSession`
  - `startWorkoutFromRoutine()` now maps targetReps from routine to workout
  - `SetRow` displays targetReps placeholder (e.g., "8-12 reps") when input is empty
- **Database Migrations** - Added proper Room migrations for schema updates
  - Migration 4→5: Creates routine_sets and routine_timers tables with indices
  - Migration 5→6: Adds targetReps column to workout_exercises table
  - Database version bumped to 6

### Updated
- `ScheduledWorkoutEntity.kt` - Changed primary key to composite scheduleKey
- `DayPickerDialog` - Added onConfirm callback parameter
- `WorkoutSessionService.kt` - Added `loadExercisesForWorkout()` method
- `WorkoutSessionManager.kt` - Added pending state variables, `loadExercisesFromWorkout()`, `loadExercisesInternal()`
- `WorkoutExerciseEntity.kt` - Added targetReps field
- `WorkoutOverlay.kt` - Updated SetRow to accept and display targetReps placeholder

---

## [1.11.0] - 2026-04-13

### Added
- **Create Routine Redesign** - Completely redesigned routine creation screen based on workout overlay style
  - Exercise cards with set rows, weight/reps inputs, set type badges
  - Body focus selection per exercise
  - Configure Timer per set type (Normal, Warmup, Work, Drop, Failure)
  - Timer preview rows shown after each set
- **Delete Routine** - Added delete option in routine card "..." menu with confirmation dialog
- **Edit Routine** - Edit now loads existing routine data and updates (not creates new)
- **Rest Timer Configuration** - Per-set-type rest timers stored per exercise

### Fixed
- **Routine Saving** - Fixed routine exercises not being saved to database properly
- **Start Workout from Routine** - Now properly copies exercises from routine to workout session
- **Routine List Display** - Shows estimated duration instead of "0 exercises"

### Updated
- `CreateRoutineScreen.kt` - Redesigned with compact header (name, type dropdown, duration), larger exercise section
- `CreateRoutineViewModel.kt` - Added `loadRoutine()`, `resetState()`, `updateExerciseTimers()`, `toggleRoutineType()`
- `WorkoutRepository.kt` - Added `startWorkoutFromRoutine()` to copy routine exercises to workout
- `WorkoutViewModel.kt` - Updated `startWorkoutWithRoutine()` to load full routine and copy exercises
- `Screen.kt` - Updated `CreateRoutine` route to accept `routineId` parameter
- `GymBuddyNavigation.kt` - Updated CreateRoutine composable with routineId argument
- Database schema bump to version 3

---

## [1.10.0] - 2026-04-13

### Added
- **Workout Detail Popup** - Tapping a workout in history now opens a popup showing workout details
  - Displays workout name, date, duration, and feeling emoji
  - Shows all exercises with sets, weights, reps, and RPE values
  - Warmup sets indicator per exercise
- **Feeling Rating** - After finishing a workout, users rate how they felt (1-5 scale with emoji)
  - Saved in workout history and displayed in workout detail popup
- **Swipe-to-Delete Timer Rows** - Timer rows now support swipe-to-delete functionality
  - Added `SwipeableTimerRow` composable
- **Reset Exercise** - Changed "Swap Exercise" to "Reset Exercise" in overlay menu

### Fixed
- **Exercise Graph Data Loading** - Fixed historical data grouping to properly load sets by workout session
- **1RM-12RM Calculations** - Fixed rep max calculations to use Epley formula properly
- **Graph Display** - Shows blank graph placeholder when no data instead of "No data to display"
- **Delete Button Alignment** - Fixed SwipeableSetRow delete button height to match set row height (36dp)

### Updated
- `WorkoutEntity.kt` - Added `feeling` field
- `Workout.kt` - Added `feeling` field
- `WorkoutRepository.kt` - Updated `saveCompletedWorkout()` to accept feeling, added `getWorkoutWithDetails()`
- `WorkoutSessionManager.kt` - Updated `finishWorkout()` to accept feeling parameter
- `WorkoutOverlay.kt` - Added `FeelingRatingDialog`, `SwipeableTimerRow`, fixed overlay header menu
- `ProgressViewModel.kt` - Limited workout history to 14 recent workouts, added `selectWorkout()` method
- `ProgressScreen.kt` - Added `WorkoutDetailDialog`, workout now opens detail popup on tap

---

## [1.9.0] - 2026-04-13

### Added
- **Workout Overlay Default Expanded** - Workout overlay now starts in expanded mode when starting a quick workout
- **Notification Tap Expands Overlay** - Tapping the workout notification returns to expanded overlay mode
- **Expand/Collapse Methods** - Added `expand()` and `collapse()` methods to `WorkoutSessionManager`

### Updated
- `WorkoutSessionService.kt` - Added `EXTRA_EXPAND_OVERLAY` constant and notification intent with expand flag
- `WorkoutSessionManager.kt` - Added `expand()` method, `startSession()` now sets expanded to true by default
- `MainActivity.kt` - Handles `expand_overlay` intent extra on first launch

---

## [1.8.0] - 2026-04-13

### Added
- **Split Home Screen Widgets** - 4 separate widgets for flexible placement
  - **Streak Widget** - Shows current streak with fire emoji 🔥
  - **Today Status Widget** - Shows today's workout status (✓ Done / ○ Pending)
  - **Weekly Progress Widget** - Shows weekly workout progress bar with count
  - **Quick Start Widget** - Quick start workout button 🏋️
  - Auto-updates when workout is completed

### Updated
- `WorkoutSessionManager.kt` - Update widget on workout completion

---

## [1.6.0] - 2026-04-13

### Added
- **Notification Permission Fix** - Add runtime notification permission request for Android 13+
- **Notification Sync** - Sync method for workouts and notification service
- **Quick Testing Mode** - Set notification interval to 30 seconds for quick testing

### Updated
- `NotificationService.kt` - Fixed MainActivity import
- `StreakReminderWorker.kt` - Notification permission handling

---

## [1.5.0] - 2026-04-13

### Added
- **Achievement System** - Full badge/achievement system with XP rewards
  - 17 achievement types (12 original + 5 new weekly achievements)
  - Database storage with claim count tracking (can earn multiple times)
  - XP rewards per badge (50-1000 XP based on difficulty)
  - Badge earned notifications when completing workouts
- **User Title System** - Fancy gym-themed titles based on level
  - New Year's Resolution (Level 1-4)
  - Gym Rat (Level 5-9)
  - Iron Pumper (Level 10-19)
  - Beast (Level 20-29)
  - Swolefather (Level 30-49)
  - Greek God (Level 50-74)
  - Mountain (Level 75-99)
  - Immortal (Level 100+)
- **Workout XP System** - 100 XP per workout completion
- **Volume Tracking** - Tracks total gym volume (weight × reps) across all workouts
- **Gym Finder Section** - New section on home screen to find nearest gym location
- **Gym Chain Selection** - Dropdown to select from 5 gym chains: HK, 247, Anytime, Snap Fitness, EFX Fitness
- **Location-Based Search** - Uses device GPS to find nearest gym location
- **WebView Map Display** - Embedded Google Maps via WebView showing nearest gym (no API key required)
- **40+ HK Gym Locations** - Pre-defined database of gym locations across Hong Kong
- **Full Body Muscle Focus** - Now shows all 6 muscle groups (Chest, Back, Shoulders, Arms, Legs, Core) with real volume data from current week's workouts
- **10-Day Volume Chart** - Line graph showing daily training volume for the last 10 days with tap-to-view data points

### Fixed
- **XP/Level Calculation** - Fixed level calculation logic with proper exponential formula
- **Streak Calculation** - Fixed streak increment/reset logic
- **Achievement Screen** - Now fetches from database with real-time progress

### Updated
- `UserProfileEntity.kt` - Added title field
- `UserProfileRepository.kt` - Added volume tracking and streak fix
- `WorkoutRepository.kt` - Added getWorkoutVolume() method
- `WorkoutSessionManager.kt` - Passes volume through to profile
- `ProfileScreen.kt` - Shows user title with gold badge
- `HomeScreen.kt` - Shows user title in greeting section + gym finder
- `AchievementScreen.kt` - Fetches from DB, shows claim count and progress
- `build.gradle.kts` - Added WebView dependency, removed Google Maps dependencies
- `AndroidManifest.xml` - Added location and internet permissions
- `HomeViewModel.kt` - Added gym selection state and location handling
- `SetDao.kt` - Added queries: getVolumeForMuscleGroup, getDailyVolumes

### New Files
- `AchievementType.kt` - Enum with all achievement types and XP rewards
- `AchievementEntity.kt` - Database entity for storing earned achievements
- `AchievementDao.kt` - DAO for CRUD operations
- `AchievementRepository.kt` - Repository with achievement checking logic
- `AchievementViewModel.kt` - ViewModel for AchievementScreen
- `NotificationService.kt` - Service for achievement notifications
- `GymLocation.kt` - Data model for gym locations and gym chain enum
- `LocationService.kt` - Service for getting device GPS location
- `GymLocationHelper.kt` - Helper with gym data and nearest location search logic

---

## [1.4.0] - 2026-04-13

### Added
- **Full Body Muscle Focus** - Now shows all 6 muscle groups (Chest, Back, Shoulders, Arms, Legs, Core) with real volume data from current week's workouts
- **Muscle Goal Setting** - Click on any muscle to set weekly volume goal via dialog; goals stored in SharedPreferences
- **10-Day Volume Chart** - Line graph showing daily training volume for the last 10 days with tap-to-view data points
- **Personal Records Enhancement** - Improved card styling with trophy icons, date achieved, and PR type badges
- **Progress Page Data** - All cards now load real data from database instead of hardcoded values

### Changed
- **Muscle Focus Calculation** - Now aggregates (reps × weight) by targetMuscle for current week
- **Volume Chart** - Replaced static weekly volume with interactive 10-day line graph
- **PR Display** - Enhanced visual design with proper formatting and icons

### Updated
- `SetDao.kt` - Added queries: getVolumeForMuscleGroup, getDailyVolumes, getTotalVolumeForDateRange
- `WorkoutRepository.kt` - Added: getWeeklyVolumePerMuscle(), getDailyVolumesForLast10Days()
- `MuscleGoalPreferences.kt` - New SharedPreferences manager for weekly volume goals
- `ProgressViewModel.kt` - Updated to load muscle focus, daily volumes, and PRs from database
- `ProgressScreen.kt` - Complete UI overhaul with Canvas-based line chart, muscle grid, goal dialog
- `build.gradle.kts` - Added buildConfig = true for BuildConfig reference
- `AchievementScreen.kt` - Added @OptIn(ExperimentalMaterial3Api::class) for TopAppBar
- `StreakReminderWorker.kt` - Fixed import: MainActivity now in correct package

---

## [1.1.1] - 2026-04-13

### Fixed
- **Workout History Not Showing** - Completed workouts now properly persist to the database so they appear in the Progress page history section
- `WorkoutSessionManager.kt` - Now calls `WorkoutRepository.completeWorkout()` when finishing a workout to mark it as completed with duration

---

## [1.2.0] - 2026-04-13

### Added
- **Exercise Insight Dialog** - New popup dialog (75% screen size) triggered by tapping exercise card
  - **About Tab**: Description, target muscles, equipment type, instructions, secondary muscles
  - **History Tab**: Past workout sessions with date, sets, reps, weights
  - **Graph Tab**: Line charts showing progress over time
    - Best Set (Est. 1RM) using Epley formula
    - Best Set (Max Weight)
    - Total Training Volume
    - Max Reps
  - **Record Tab**: Summary cards + 3-column table (RM, Best Record, Predicted)
- **Epley Formula Prediction** - Sophisticated prediction using recent best performance with 0.5% weekly progression
- **Vico Charting Library** - Integrated for progress visualization graphs
- **Workout Queries** - Added database queries for fetching workouts with specific exercises

### Updated
- `ExerciseScreen.kt` - Now opens insight dialog on tap instead of navigating to detail
- `build.gradle.kts` - Added Vico and Foundation dependencies

---

## [1.1.0] - 2026-04-12

### Added
- **Exercise Library Screen** - New screen with search and muscle group filter
- **Workout History Section** - Added to Progress page showing recent workouts
- **Start Workout Button** - Added to Workout page (renamed from Routines)
- **startedAt field** - Added to WorkoutEntity for session tracking

### Changed
- **Navigation Restructuring**:
  - `Routines` renamed to `Workout` - Now contains Start Workout button + Routines list
  - `Workout` renamed to `Exercise` - New Exercise Library
  - `Progress` now includes Workout History section
- **Home Screen** - Removed Start Workout button (moved to Workout page)

### Updated
- `WorkoutScreen.kt` - Now includes Start Workout button and routines list
- `ExerciseScreen.kt` - New exercise library with search and filters
- `ProgressScreen.kt` - Added workout history section with cards
- `WorkoutEntity.kt` - Added `startedAt` field
- `WorkoutViewModel.kt` - Updated to handle workout start with routine

---

## [1.0.1] - 2026-04-12

### Fixed
- **App Crash on Startup** - Added `GymBuddyApplication.kt` with `@HiltAndroidApp` annotation
- **JVM Target Compatibility** - Added Kotlin JVM toolchain (Java 17) to fix compatibility between Kotlin and Java compilation
- **Material3 Theme Issues** - Updated `Type.kt` with full Typography definitions

### Added
- `GymBuddyApplication.kt` - Hilt Application class for proper dependency injection initialization

### Changed
- `app/build.gradle.kts` - Added `kotlin { jvmToolchain(17) }` block

---

## [1.0.0] - 2026-04-12

### Added
- **Core Architecture**:
  - Clean Architecture with MVVM pattern
  - Kotlin + Jetpack Compose + Material 3
  - Room database with 9 tables
  - Hilt dependency injection

- **Data Layer** (17 files):
  - Entities: ExerciseEntity, WorkoutEntity, WorkoutExerciseEntity, SetEntity, RoutineEntity, RoutineExerciseEntity, UserProfileEntity, PersonalRecordEntity, BodyMeasurementEntity
  - DAOs: ExerciseDao, WorkoutDao, WorkoutExerciseDao, SetDao, RoutineDao, RoutineExerciseDao, UserProfileDao, PersonalRecordDao

- **Domain Layer** (4 files):
  - Exercise, Workout, Routine, UserProfile models with enums

- **Repository Layer** (4 files):
  - ExerciseRepository, WorkoutRepository, UserProfileRepository, RoutineRepository

- **UI Layer - Theme** (3 files):
  - Cyber-futuristic dark theme with neon cyan/teal colors
  - Custom Material3 color scheme
  - Typography definitions

- **UI Layer - Navigation** (2 files):
  - Bottom navigation with 5 tabs
  - Route definitions for all screens

- **UI Layer - Screens** (14 files):
  - Home: Greeting, XP bar, virtual pet, stats, Start Workout button
  - Workout: Workout history list
  - Routines: Push/Pull/Legs templates
  - Progress: Muscle heat map, volume charts, PRs
  - Profile: User stats, achievements, pet display
  - Exercise Detail: Instructions, target muscles

- **Service Layer** (1 file):
  - RestTimerService - Foreground Service for rest timer notifications

- **Virtual Pet System**:
  - Pet happiness tied to workout streaks
  - Multiple mood states (happy, sad, excited, disappointed)
  - Mood messages for motivation

- **Pre-populated Data**:
  - 10 default exercises (Chest, Back, Shoulders, Arms, Legs, Core)
  - 3 routines (Push Day, Pull Day, Leg Day)

### Changed
- Initial project setup and structure

---

[1.13.0]: https://github.com/example/gymbuddy/compare/v1.12.0...v1.13.0
[1.12.0]: https://github.com/example/gymbuddy/compare/v1.11.1...v1.12.0
[1.11.1]: https://github.com/example/gymbuddy/compare/v1.11.0...v1.11.1
[1.11.0]: https://github.com/example/gymbuddy/compare/v1.10.0...v1.11.0
[1.10.0]: https://github.com/example/gymbuddy/compare/v1.9.0...v1.10.0
[1.9.0]: https://github.com/example/gymbuddy/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/example/gymbuddy/compare/v1.6.0...v1.8.0
[1.5.0]: https://github.com/example/gymbuddy/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/example/gymbuddy/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/example/gymbuddy/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/example/gymbuddy/compare/v1.1.0...v1.2.0
[1.1.1]: https://github.com/example/gymbuddy/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/example/gymbuddy/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/example/gymbuddy/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/example/gymbuddy/releases/tag/v1.0.0

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

## [1.3.0] - 2026-04-13

### Added
- **Achievement Screen** - New screen with 2x6 grid of achievement badges showing progress
- **Streak Reminder Notifications** - Duolingo-style blackmail notifications to encourage workout streaks
- **WorkManager Integration** - Background worker for scheduled daily notifications

### Fixed
- **Achievement Section Unreachable** - Made AchievementBadgesSection clickable to navigate to new AchievementScreen

### Changed
- **Notification Timing**:
  - Debug mode: Every 1 minute for quick testing
  - Production: Daily at 7 PM
- **Notification Messages** - Escalating guilt-trip messages based on streak length (3-30+ days)

### Updated
- `Screen.kt` - Added Achievement route
- `GymBuddyNavigation.kt` - Added AchievementScreen composable
- `ProfileScreen.kt` - Added clickable modifier to AchievementBadgesSection
- `MainActivity.kt` - Added WorkManager scheduler for streak reminders
- `StreakReminderWorker.kt` - New worker with guilt-trip notification messages
- `app/build.gradle.kts` - Added WorkManager + Hilt worker dependencies


---

## [1.2.0] - 2026-04-13

### Added
- **Workout Session System** - Foreground service with non-dismissible notification for active workouts
- **Workout Overlay** - Expandable/collapsed UI panel for tracking active workout sessions
- **Set Type Management** - Support for Normal, Warmup, Drop, and Failure set types
- **Rest Timer Configuration** - Per-exercise timer settings (warmup, work, drop, failure)
- **Set Type Toggle** - Dropdown to change set type for each row independently

### Changed
- **Timer Display Logic**:
  - Timer now shows between sets based on the CURRENT set's type
  - Matches timer by type AND abbreviation fallback (W matches both WORK and WARMUP)
- **Set Renumbering**:
  - NORMAL sets display as sequential numbers: 1, 2, 3...
  - Non-NORMAL sets display type abbreviation: W, D, F
  - Numbers auto-adjust when changing set types
- **Swipe Delete** - Background now red when swiping to delete

### Fixed
- **Timer matching** - Timer now correctly matches set type (was only showing for some types)
- **Cross-exercise timer pollution** - Each exercise has independent timers (adding timer to Bicep Curls doesn't affect other exercises)
- **Set type toggle** - Only the specific set is modified (was affecting all sets in exercise)
- **Timer callback chain** - Fixed type mismatches causing build errors

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

[1.5.0]: https://github.com/example/gymbuddy/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/example/gymbuddy/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/example/gymbuddy/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/example/gymbuddy/compare/v1.1.0...v1.2.0
[1.1.1]: https://github.com/example/gymbuddy/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/example/gymbuddy/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/example/gymbuddy/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/example/gymbuddy/releases/tag/v1.0.0

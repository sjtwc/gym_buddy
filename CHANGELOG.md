# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.1] - 2026-04-13

### Fixed
- **Workout History Not Showing** - Completed workouts now properly persist to the database so they appear in the Progress page history section
- `WorkoutSessionManager.kt` - Now calls `WorkoutRepository.completeWorkout()` when finishing a workout to mark it as completed with duration

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

[1.1.1]: https://github.com/example/gymbuddy/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/example/gymbuddy/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/example/gymbuddy/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/example/gymbuddy/releases/tag/v1.0.0
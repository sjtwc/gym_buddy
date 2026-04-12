# Gym Buddy - Premium Gym Workout Tracking App

A premium native Android gym workout tracking application written in Kotlin with Jetpack Compose. Designed for strength training and weightlifting with a clean, fast, professional, and highly motivating user experience.

## Features

### Core Features
- **Intuitive Workout Logging** - Fast set/rep/weight/RPE input with previous performance inline
- **Exercise Database** - 10+ pre-loaded exercises (Chest, Back, Shoulders, Arms, Legs, Core)
- **Routine Management** - Pre-built Push/Pull/Legs templates
- **Progress Tracking** - PRs, volume charts, muscle heat maps
- **Virtual Pet Companion** - Gamification with pet happiness tied to workout streaks

### Technical Features
- **Offline-First** - Room database for local storage
- **MVVM Architecture** - Clean Architecture with Hilt DI
- **Foreground Service** - Rest timer with persistent notification
- **Dark Theme** - Cyber-futuristic UI with neon accents

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room
- **DI**: Hilt
- **Navigation**: Navigation Compose
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/src/main/java/com/example/gymbuddy/
├── data/                    # Data Layer
│   ├── local/
│   │   ├── entity/          # Room Entities (9 files)
│   │   ├── dao/             # Room DAOs (8 files)
│   │   └── GymBuddyDatabase.kt
│   └── repository/          # Repositories (4 files)
├── domain/
│   └── model/               # Domain Models (4 files)
├── di/                      # Hilt Modules
├── service/                 # Background Services
├── ui/
│   ├── theme/               # Theme (Color, Theme, Type)
│   ├── navigation/          # Navigation Setup
│   └── screens/             # UI Screens (6 screens)
├── GymBuddyApplication.kt   # Application Class
└── MainActivity.kt          # Entry Point
```

## Module Breakdown

### 1. Data Layer (17 files)
**Entities:**
- `ExerciseEntity` - Exercise definitions
- `WorkoutEntity` - Workout sessions
- `WorkoutExerciseEntity` - Exercises in a workout
- `SetEntity` - Individual sets with reps/weight/RPE
- `RoutineEntity` - Pre-built workout routines
- `RoutineExerciseEntity` - Exercises in a routine
- `UserProfileEntity` - User stats, pet, streaks
- `PersonalRecordEntity` - PR tracking
- `BodyMeasurementEntity` - Body measurements

**DAOs:**
- `ExerciseDao`, `WorkoutDao`, `WorkoutExerciseDao`, `SetDao`
- `RoutineDao`, `RoutineExerciseDao`, `UserProfileDao`, `PersonalRecordDao`

### 2. Domain Layer (4 files)
- `Exercise.kt` - Exercise model + MuscleGroup, EquipmentType enums
- `Workout.kt` - Workout, WorkoutExercise, WorkoutSet models
- `Routine.kt` - Routine, RoutineExercise + RoutineType, Difficulty enums
- `UserProfile.kt` - UserProfile, VirtualPet, PetMood, PersonalRecord, BodyMeasurement

### 3. Repository Layer (4 files)
- `ExerciseRepository.kt` - Exercise data operations
- `WorkoutRepository.kt` - Workout & set operations
- `UserProfileRepository.kt` - User profile & pet operations
- `RoutineRepository.kt` - Routine operations

### 4. DI Layer (1 file)
- `DatabaseModule.kt` - Hilt module providing database and DAOs

### 5. UI Layer - Theme (3 files)
- `Color.kt` - Neon colors (NeonCyan, NeonTeal, NeonPurple, DarkBackground, etc.)
- `Theme.kt` - Material3 dark theme with custom colors
- `Type.kt` - Typography definitions

### 6. UI Layer - Navigation (2 files)
- `Screen.kt` - Route definitions for all screens
- `GymBuddyNavigation.kt` - Bottom nav + NavHost

### 7. UI Layer - Screens (14 files)

| Screen | Description | Files |
|--------|-------------|-------|
| **Home** | Greeting, XP bar, pet display, stats, start workout | `HomeScreen.kt`, `HomeViewModel.kt` |
| **Workout** | Workout history list | `WorkoutScreen.kt`, `WorkoutViewModel.kt` |
| **Active Workout** | Set logging, rest timer, plate calculator | `ActiveWorkoutScreen.kt` |
| **Routines** | Routine list (Push/Pull/Legs) | `RoutinesScreen.kt`, `RoutinesViewModel.kt` |
| **Progress** | Muscle heat map, volume, PRs | `ProgressScreen.kt`, `ProgressViewModel.kt` |
| **Profile** | User stats, achievements, pet | `ProfileScreen.kt`, `ProfileViewModel.kt` |
| **Exercise Detail** | Exercise info, instructions, muscles | `ExerciseDetailScreen.kt`, `ExerciseDetailViewModel.kt` |

### 8. Service Layer (1 file)
- `RestTimerService.kt` - Foreground Service for rest timer with notification

## Building

```bash
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Screenshots

The app features a cyber-futuristic dark theme with:
- Neon cyan/teal accents
- Dark backgrounds (#0A0A0F, #12121A)
- Glowing progress indicators
- Virtual pet with mood messages

## Recent Changes

### v1.0.1 - Bug Fixes
- **Added GymBuddyApplication.kt** - Fixed app crash on startup by adding Hilt Application class
- **Added JVM Toolchain** - Fixed inconsistent JVM-target compatibility (17 for both Kotlin and Java)
- **Updated Typography** - Added full Typography definitions to fix Material3 theme issues

## License

MIT License
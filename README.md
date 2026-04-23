# Gym Buddy - Premium Gym Workout Tracking App

A premium native Android gym workout tracking application written in Kotlin with Jetpack Compose. Designed for strength training and weightlifting with a clean, fast, professional, and highly motivating user experience.

## Features

### Core Features
- **Intuitive Workout Logging** - Fast set/rep/weight/RPE input with previous performance inline
- **Exercise Database** - 10+ pre-loaded exercises (Chest, Back, Shoulders, Arms, Legs, Core)
- **Routine Management** - Pre-built Push/Pull/Legs templates with custom routine creation
- **Progress Tracking** - PRs, volume charts, muscle heat maps, 10-day volume trends
- **Virtual Pet Companion** - Gamification with pet happiness tied to workout streaks

### User Experience
- **Avatar Upload** - Personal avatar with internal storage
- **Time-Based Greeting** - Good morning/afternoon/evening/night based on time of day
- **Pet Tap Interaction** - Tap pet card to cycle through motivational messages
- **Home Screen Widgets** - 4 widgets: Quick Start, Streak, Today Status, Weekly Progress
- **Exercise Insight** - Detailed exercise analysis with graphs and PR tracking
- **Weekly Schedule** - Schedule workouts for specific days

### Gamification
- **Achievement System** - 20+ badges with XP rewards
- **User Titles** - Progressive titles based on level (Gym Rat, Iron Pumper, Beast, etc.)
- **Workout XP** - Earn XP for completing workouts
- **Streak Tracking** - Current and longest streak display

### Calendar Integration
- **Google Calendar Sync** - Connect calendar for workout scheduling
- **Free Time Detection** - Find available time slots for workouts
- **Scheduled Reminders** - Configurable daily streak reminders

### Technical Features
- **Offline-First** - Room database for local storage (11 tables)
- **MVVM Architecture** - Clean Architecture with Hilt DI
- **Foreground Service** - Rest timer with persistent notification
- **Dark Theme** - Cyber-futuristic UI with neon accents
- **Coil Image Loading** - Efficient image loading for avatars

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
│   │   ├── entity/          # Room Entities (11 files)
│   │   ├── dao/             # Room DAOs (10 files)
│   │   └── GymBuddyDatabase.kt
│   └── repository/          # Repositories (5 files)
├── domain/
│   └── model/               # Domain Models (6 files)
├── di/                      # Hilt Modules
├── service/                 # Background Services
├── ui/
│   ├── theme/               # Theme (Color, Theme, Type)
│   ├── navigation/          # Navigation Setup
│   └── screens/             # UI Screens (14 screens)
├── widget/                  # App Widgets (5 files)
├── GymBuddyApplication.kt   # Application Class
└── MainActivity.kt          # Entry Point
```

## Module Breakdown

### 1. Data Layer (20+ files)
**Entities:**
- `ExerciseEntity` - Exercise definitions
- `WorkoutEntity` - Workout sessions
- `WorkoutExerciseEntity` - Exercises in a workout
- `SetEntity` - Individual sets with reps/weight/RPE
- `RoutineEntity` - Pre-built workout routines
- `RoutineExerciseEntity` - Exercises in a routine
- `RoutineSetEntity` - Sets within routine exercises
- `RoutineTimerEntity` - Timer configurations per set type
- `UserProfileEntity` - User stats, pet, streaks, avatar
- `PersonalRecordEntity` - PR tracking
- `BodyMeasurementEntity` - Body measurements

**DAOs:**
- `ExerciseDao`, `WorkoutDao`, `WorkoutExerciseDao`, `SetDao`
- `RoutineDao`, `RoutineExerciseDao`, `RoutineSetDao`, `RoutineTimerDao`
- `UserProfileDao`, `PersonalRecordDao`, `BodyMeasurementDao`

### 2. Domain Layer (6 files)
- `Exercise.kt` - Exercise model + MuscleGroup, EquipmentType enums
- `Workout.kt` - Workout, WorkoutExercise, WorkoutSet models
- `Routine.kt` - Routine, RoutineExercise, RoutineSetData, RoutineExerciseTimer + RoutineType, Difficulty, SetType enums
- `UserProfile.kt` - UserProfile, VirtualPet, PetMood, PersonalRecord, BodyMeasurement
- `AchievementType.kt` - Achievement types and XP rewards
- `WorkoutSummary.kt` - Summary statistics models

### 3. Repository Layer (5 files)
- `ExerciseRepository.kt` - Exercise data operations
- `WorkoutRepository.kt` - Workout & set operations
- `UserProfileRepository.kt` - User profile & pet operations
- `RoutineRepository.kt` - Routine operations
- `AchievementRepository.kt` - Achievement checking and rewards

### 4. DI Layer (1 file)
- `DatabaseModule.kt` - Hilt module providing database and DAOs

### 5. UI Layer - Theme (3 files)
- `Color.kt` - Neon colors (NeonCyan, NeonTeal, NeonPurple, DarkBackground, etc.)
- `Theme.kt` - Material3 dark theme with custom colors
- `Type.kt` - Typography definitions

### 6. UI Layer - Navigation (2 files)
- `Screen.kt` - Route definitions for all screens
- `GymBuddyNavigation.kt` - Bottom nav + NavHost

### 7. UI Layer - Screens (16 files)

| Screen | Description | Files |
|--------|-------------|-------|
| **Home** | Greeting, XP bar, pet display, stats, start workout | `HomeScreen.kt`, `HomeViewModel.kt` |
| **Workout** | Start workout, routine list, weekly schedule | `WorkoutScreen.kt`, `WorkoutViewModel.kt` |
| **Active Workout** | Set logging, rest timer, plate calculator | `ActiveWorkoutScreen.kt` |
| **Progress** | Muscle heat map, volume, PRs, workout history | `ProgressScreen.kt`, `ProgressViewModel.kt` |
| **Profile** | User stats, achievements, pet display | `ProfileScreen.kt`, `ProfileViewModel.kt` |
| **Profile Edit** | Edit name, avatar, pet name, body measurements | `ProfileEditScreen.kt`, `ProfileEditViewModel.kt` |
| **Settings** | App settings, calendar integration, stats, reset | `SettingsScreen.kt`, `SettingsViewModel.kt` |
| **Exercise** | Exercise library with search and muscle filter | `ExerciseScreen.kt` |
| **Exercise Detail** | Exercise info, instructions, history, graphs, records | `ExerciseDetailScreen.kt`, `ExerciseDetailViewModel.kt` |
| **Exercise Insight** | Full exercise analysis with graphs and PRs | `ExerciseInsightDialog.kt`, `ExerciseInsightViewModel.kt` |
| **Achievements** | Badge display and XP progress | `AchievementScreen.kt`, `AchievementViewModel.kt` |
| **Create Routine** | Create/edit routines with exercises | `CreateRoutineScreen.kt`, `CreateRoutineViewModel.kt` |

### 8. Service Layer (4 files)
- `WorkoutSessionManager.kt` - Workout session state management
- `RestTimerService.kt` - Foreground Service for rest timer with notification
- `StreakReminderWorker.kt` - Background streak reminder notifications
- `LocationService.kt` - GPS location for gym finder

### 9. Widget Layer (5 files)
- `QuickStartWidgetProvider.kt` - Quick start workout widget
- `StreakWidgetProvider.kt` - Current streak display widget
- `TodayStatusWidgetProvider.kt` - Today's workout status widget
- `WeeklyProgressWidgetProvider.kt` - Weekly progress widget
- `WidgetPreferences.kt` - Widget data management

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

## License

MIT License
package com.example.gymbuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Workout : Screen("workout", "Workout", Icons.Default.SportsGymnastics)
    data object Exercises : Screen("exercises", "Exercises", Icons.Default.List)
    data object Routines : Screen("routines", "Routines", Icons.Default.Folder)
    data object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
    data object Achievements : Screen("achievements", "Badges", Icons.Default.Star)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    
    data object ExerciseDetail : Screen("exercise/{exerciseId}", "Exercise Detail") {
        fun createRoute(exerciseId: Long) = "exercise/$exerciseId"
    }
    
    data object WorkoutLog : Screen("workout/log/{workoutId}", "Workout") {
        fun createRoute(workoutId: Long) = "workout/log/$workoutId"
    }
    
    data object RoutineDetail : Screen("routine/{routineId}", "Routine") {
        fun createRoute(routineId: Long) = "routine/$routineId"
    }
    
    data object ActiveWorkout : Screen("active_workout", "Active Workout")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Workout,
    Screen.Routines,
    Screen.Progress,
    Screen.Profile
)
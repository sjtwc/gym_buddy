package com.example.gymbuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    
    // Renamed: Routines -> Workout (now has Start Workout button)
    data object Workout : Screen("workout", "Workout", Icons.Default.Folder)
    
    // Renamed: Workout -> Exercise (Exercise Library)
    data object Exercise : Screen("exercise", "Exercise", Icons.Default.List)
    
    data object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    
    data object Achievement : Screen("achievement", "Achievements")
    
    data object ExerciseDetail : Screen("exercise/{exerciseId}", "Exercise Detail") {
        fun createRoute(exerciseId: Long) = "exercise/$exerciseId"
    }
    
    data object RoutineDetail : Screen("routine/{routineId}", "Routine") {
        fun createRoute(routineId: Long) = "routine/$routineId"
    }
    
    data object RoutineEdit : Screen("routine_edit/{routineId}", "Edit Routine") {
        fun createRoute(routineId: Long = 0L) = "routine_edit/$routineId"
    }
    
    data object ActiveWorkout : Screen("active_workout", "Active Workout")
    data object CreateRoutine : Screen("create_routine", "Create Routine")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Workout,
    Screen.Exercise,
    Screen.Progress,
    Screen.Profile
)
package com.csci3310.gymbuddy.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csci3310.gymbuddy.service.WorkoutSessionManager
import com.csci3310.gymbuddy.ui.components.WorkoutOverlay
import com.csci3310.gymbuddy.ui.screens.home.HomeScreen
import com.csci3310.gymbuddy.ui.screens.workout.WorkoutScreen
import com.csci3310.gymbuddy.ui.screens.exercise.ExerciseScreen
import com.csci3310.gymbuddy.ui.screens.progress.ProgressScreen
import com.csci3310.gymbuddy.ui.screens.profile.ProfileScreen
import com.csci3310.gymbuddy.ui.screens.profile.ProfileEditScreen
import com.csci3310.gymbuddy.ui.screens.exercise.ExerciseDetailScreen
import com.csci3310.gymbuddy.ui.screens.achievement.AchievementScreen
import com.csci3310.gymbuddy.ui.screens.routines.CreateRoutineScreen
import com.csci3310.gymbuddy.ui.screens.settings.SettingsScreen

@Composable
fun GymBuddyNavigation(
    sessionManager: WorkoutSessionManager,
    onNavControllerReady: (NavHostController) -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isSessionActive by sessionManager.isActive.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }

                composable(Screen.Workout.route) {
                    WorkoutScreen(navController = navController, sessionManager = sessionManager)
                }

                composable(Screen.Exercise.route) {
                    ExerciseScreen(navController = navController)
                }

                composable(Screen.Progress.route) {
                    ProgressScreen(navController = navController)
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(navController = navController)
                }

                composable(Screen.Achievement.route) {
                    AchievementScreen(navController = navController)
                }

                composable(
                    route = Screen.ExerciseDetail.route,
                    arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
                    ExerciseDetailScreen(exerciseId = exerciseId, navController = navController)
                }

                composable(
                    route = Screen.CreateRoutine.route,
                    arguments = listOf(navArgument("routineId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    })
                ) { backStackEntry ->
                    val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                    CreateRoutineScreen(navController = navController, routineId = routineId)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(navController = navController)
                }

                composable(Screen.ProfileEdit.route) {
                    ProfileEditScreen(navController = navController)
                }
            }

            if (isSessionActive) {
                WorkoutOverlay(
                    sessionManager = sessionManager,
                    isVisible = isSessionActive,
                    onExpandToggle = { sessionManager.toggleExpanded() },
                    onCollapse = { sessionManager.collapse() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
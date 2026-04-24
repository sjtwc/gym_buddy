package com.example.gymbuddy.di

import android.content.Context
import com.example.gymbuddy.data.local.CalendarPreferences
import com.example.gymbuddy.data.local.GymBuddyDatabase
import com.example.gymbuddy.data.local.MuscleGoalPreferences
import com.example.gymbuddy.data.local.dao.*
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.ScheduledWorkoutRepository
import com.example.gymbuddy.service.CalendarEventExporter
import com.example.gymbuddy.service.CalendarService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymBuddyDatabase {
        return GymBuddyDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCalendarPreferences(@ApplicationContext context: Context): CalendarPreferences {
        return CalendarPreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideMuscleGoalPreferences(@ApplicationContext context: Context): MuscleGoalPreferences {
        return MuscleGoalPreferences(context)
    }
    
    @Provides
    fun provideExerciseDao(database: GymBuddyDatabase): ExerciseDao {
        return database.exerciseDao()
    }
    
    @Provides
    fun provideWorkoutDao(database: GymBuddyDatabase): WorkoutDao {
        return database.workoutDao()
    }
    
    @Provides
    fun provideWorkoutExerciseDao(database: GymBuddyDatabase): WorkoutExerciseDao {
        return database.workoutExerciseDao()
    }
    
    @Provides
    fun provideSetDao(database: GymBuddyDatabase): SetDao {
        return database.setDao()
    }
    
    @Provides
    fun provideRoutineDao(database: GymBuddyDatabase): RoutineDao {
        return database.routineDao()
    }
    
    @Provides
    fun provideRoutineExerciseDao(database: GymBuddyDatabase): RoutineExerciseDao {
        return database.routineExerciseDao()
    }

    @Provides
    fun provideRoutineSetDao(database: GymBuddyDatabase): RoutineSetDao {
        return database.routineSetDao()
    }

    @Provides
    fun provideRoutineTimerDao(database: GymBuddyDatabase): RoutineTimerDao {
        return database.routineTimerDao()
    }

    @Provides
    fun provideUserProfileDao(database: GymBuddyDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    fun providePersonalRecordDao(database: GymBuddyDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }
    
    @Provides
    fun provideAchievementDao(database: GymBuddyDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    fun provideScheduledWorkoutDao(database: GymBuddyDatabase): ScheduledWorkoutDao {
        return database.scheduledWorkoutDao()
    }

    @Provides
    @Singleton
    fun provideScheduledWorkoutRepository(
        scheduledWorkoutDao: ScheduledWorkoutDao,
        routineRepository: RoutineRepository,
        muscleGoalPreferences: MuscleGoalPreferences
    ): ScheduledWorkoutRepository {
        return ScheduledWorkoutRepository(scheduledWorkoutDao, routineRepository, muscleGoalPreferences)
    }

    @Provides
    @Singleton
    fun provideCalendarEventExporter(
        @ApplicationContext context: Context,
        calendarService: CalendarService,
        scheduledWorkoutRepository: ScheduledWorkoutRepository
    ): CalendarEventExporter {
        return CalendarEventExporter(context, calendarService, scheduledWorkoutRepository)
    }

    @Provides
    @Singleton
    fun provideCalendarService(
        @ApplicationContext context: Context
    ): CalendarService {
        return CalendarService(context)
    }
}
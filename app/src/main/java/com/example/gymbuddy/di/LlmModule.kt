package com.example.gymbuddy.di

import android.content.Context
import com.example.gymbuddy.service.LlmService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    @Provides
    @Singleton
    fun provideLlmService(@ApplicationContext context: Context): LlmService {
        return LlmService(context)
    }
}
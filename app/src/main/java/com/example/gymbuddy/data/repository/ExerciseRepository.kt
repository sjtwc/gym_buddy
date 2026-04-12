package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.ExerciseDao
import com.example.gymbuddy.data.local.entity.ExerciseEntity
import com.example.gymbuddy.domain.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {
    fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscle(muscle).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getExerciseById(id: Long): Exercise? =
        exerciseDao.getExerciseById(id)?.toDomain()
    
    fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun insertExercise(exercise: Exercise): Long =
        exerciseDao.insertExercise(exercise.toEntity())
    
    suspend fun updateExercise(exercise: Exercise) =
        exerciseDao.updateExercise(exercise.toEntity())
    
    suspend fun deleteExercise(exercise: Exercise) =
        exerciseDao.deleteExercise(exercise.toEntity())
    
    private fun ExerciseEntity.toDomain() = Exercise(
        id = id,
        name = name,
        description = description,
        targetMuscle = targetMuscle,
        secondaryMuscles = secondaryMuscles.split(",").filter { it.isNotBlank() },
        equipmentType = equipmentType,
        videoUrl = videoUrl,
        instructions = instructions.split("\n").filter { it.isNotBlank() },
        isCustom = isCustom,
        createdAt = createdAt
    )
    
    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        name = name,
        description = description,
        targetMuscle = targetMuscle,
        secondaryMuscles = secondaryMuscles.joinToString(","),
        equipmentType = equipmentType,
        videoUrl = videoUrl,
        instructions = instructions.joinToString("\n"),
        isCustom = isCustom,
        createdAt = createdAt
    )
}
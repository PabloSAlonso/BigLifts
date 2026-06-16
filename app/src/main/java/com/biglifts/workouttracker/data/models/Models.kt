package com.biglifts.workouttracker.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val weight_unit: String = "kg",
    
    // Onboarding data
    val age: Int? = null,
    val gender: String? = null,
    val height_cm: Double? = null,
    val current_weight_kg: Double? = null,
    val target_weight_kg: Double? = null,
    val activity_level: String? = null,
    val training_experience: String? = null,
    val primary_goal: String? = null,
    val training_days_per_week: Int? = null,
    val injuries: String? = null,
    val available_equipment: String? = null,
    val onboarding_completed: Boolean = false,
    
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Exercise(
    val id: String? = null,
    val name: String,
    val category: String,
    val muscle_group: String? = null,
    @SerialName("secondary_muscles") val secondaryMuscles: List<String>? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("is_custom") val isCustom: Boolean = false,
    @SerialName("user_id") val userId: String? = null,
    val created_at: String? = null
)

@Serializable
data class WorkoutSession(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("session_type") val sessionType: String = "custom",
    @SerialName("template_id") val templateId: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("total_volume") val totalVolume: Double? = null,
    @SerialName("total_sets") val totalSets: Int? = null,
    @SerialName("avg_rpe") val avgRpe: Double? = null,
    val notes: String? = null,
    @SerialName("mood_before") val moodBefore: Int? = null,
    @SerialName("mood_after") val moodAfter: Int? = null,
    @SerialName("energy_level") val energyLevel: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class SessionExercise(
    val id: String? = null,
    @SerialName("session_id") val sessionId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("target_sets") val targetSets: Int? = null,
    @SerialName("target_reps_min") val targetRepsMin: Int? = null,
    @SerialName("target_reps_max") val targetRepsMax: Int? = null,
    @SerialName("target_rpe") val targetRpe: Double? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class WorkoutSet(
    val id: String? = null,
    @SerialName("session_exercise_id") val sessionExerciseId: String,
    @SerialName("set_number") val setNumber: Int,
    @SerialName("set_type") val setType: String = "working",
    
    // Performance data
    val weight: Double? = null,
    val reps: Int? = null,
    val rir: Int? = null,
    val rpe: Double? = null,
    
    // Intensity techniques
    @SerialName("is_intensity_technique") val isIntensityTechnique: Boolean = false,
    @SerialName("intensity_technique") val intensityTechnique: String? = null,
    @SerialName("dropset_details") val dropsetDetails: DropsetDetails? = null,
    
    // Tempo and timing
    val rest_seconds: Int? = null,
    val tempo: String? = null,
    @SerialName("time_under_tension_seconds") val timeUnderTensionSeconds: Double? = null,
    
    // Completion status
    val completed: Boolean = false,
    val failed: Boolean = false,
    @SerialName("failure_type") val failureType: String? = null,
    
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class DropsetDetails(
    val drops: List<Drop>? = null
)

@Serializable
data class Drop(
    val weight: Double,
    val reps: Int
)

@Serializable
data class WorkoutTemplate(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    @SerialName("split_day") val splitDay: String? = null,
    val goal: String = "strength",
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
    @SerialName("exercise_count") val exerciseCount: Int = 0,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("use_count") val useCount: Int = 0,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class TemplateExercise(
    val id: String? = null,
    @SerialName("template_id") val templateId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("target_sets") val targetSets: Int = 3,
    @SerialName("target_reps_min") val targetRepsMin: Int = 8,
    @SerialName("target_reps_max") val targetRepsMax: Int = 12,
    @SerialName("target_rpe") val targetRpe: Double? = null,
    @SerialName("rest_seconds") val restSeconds: Int = 90,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class PersonalRecord(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("pr_type") val prType: String,
    val weight: Double? = null,
    val reps: Int? = null,
    @SerialName("estimated_1rm") val estimated1rm: Double? = null,
    val volume: Double? = null,
    @SerialName("set_id") val setId: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("achieved_at") val achievedAt: String? = null,
    val created_at: String? = null
)

@Serializable
data class BodyMeasurement(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val weight: Double? = null,
    @SerialName("body_fat_percentage") val bodyFatPercentage: Double? = null,
    val chest: Double? = null,
    val waist: Double? = null,
    val hips: Double? = null,
    val arms: Double? = null,
    val thighs: Double? = null,
    val calves: Double? = null,
    val neck: Double? = null,
    val shoulders: Double? = null,
    val notes: String? = null,
    @SerialName("measured_at") val measuredAt: String? = null,
    val created_at: String? = null
)
package com.biglifts.workouttracker.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Auth
@Serializable
data class AuthRequest(
    val action: String,
    val email: String,
    val password: String,
    val username: String? = null,
    val full_name: String? = null,
    val refresh_token: String? = null
)

@Serializable
data class AuthResponse(
    val user: UserResponse? = null,
    val session: SessionResponse? = null,
    val error: String? = null,
    val message: String? = null
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String? = null,
    val username: String? = null
)

@Serializable
data class SessionResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("expires_in") val expiresIn: Int
)

// Profile
@Serializable
data class ProfileResponse(
    val data: com.biglifts.workouttracker.data.models.Profile? = null,
    val error: String? = null
)

// Onboarding
@Serializable
data class OnboardingRequest(
    val age: Int,
    val gender: String,
    val height_cm: Double,
    val current_weight_kg: Double,
    val target_weight_kg: Double? = null,
    val activity_level: String,
    val training_experience: String,
    val primary_goal: String,
    val training_days_per_week: Int,
    val injuries: String? = null,
    val available_equipment: String? = null
)

// Workouts
@Serializable
data class WorkoutListResponse(
    val data: List<com.biglifts.workouttracker.data.models.WorkoutSession> = emptyList(),
    val total: Int = 0,
    val error: String? = null
)

@Serializable
data class CreateWorkoutRequest(
    val name: String,
    val session_type: String = "custom",
    val template_id: String? = null,
    val notes: String? = null,
    @SerialName("mood_before") val moodBefore: Int? = null,
    @SerialName("energy_level") val energyLevel: Int? = null
)

@Serializable
data class WorkoutDataResponse(
    val data: com.biglifts.workouttracker.data.models.WorkoutSession? = null,
    val error: String? = null
)

// Session Exercises
@Serializable
data class SessionExerciseResponse(
    val data: List<SessionExerciseWithExercise> = emptyList(),
    val error: String? = null
)

@Serializable
data class SessionExerciseWithExercise(
    val id: String? = null,
    @SerialName("session_id") val sessionId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("target_sets") val targetSets: Int? = null,
    @SerialName("target_reps_min") val targetRepsMin: Int? = null,
    @SerialName("target_reps_max") val targetRepsMax: Int? = null,
    @SerialName("target_rpe") val targetRpe: Double? = null,
    val exercises: com.biglifts.workouttracker.data.models.Exercise? = null
)

@Serializable
data class AddExerciseRequest(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("target_sets") val targetSets: Int? = null,
    @SerialName("target_reps_min") val targetRepsMin: Int? = null,
    @SerialName("target_reps_max") val targetRepsMax: Int? = null,
    @SerialName("target_rpe") val targetRpe: Double? = null
)

// Exercises
@Serializable
data class ExerciseListResponse(
    val data: List<com.biglifts.workouttracker.data.models.Exercise> = emptyList(),
    val error: String? = null
)

// Sets
@Serializable
data class SetListResponse(
    val data: List<com.biglifts.workouttracker.data.models.WorkoutSet> = emptyList(),
    val error: String? = null
)

@Serializable
data class SetDataResponse(
    val data: com.biglifts.workouttracker.data.models.WorkoutSet? = null,
    val error: String? = null
)

@Serializable
data class CreateSetRequest(
    @SerialName("session_exercise_id") val sessionExerciseId: String,
    @SerialName("set_number") val setNumber: Int,
    @SerialName("set_type") val setType: String = "working",
    val weight: Double? = null,
    val reps: Int? = null,
    val rir: Int? = null,
    val rpe: Double? = null,
    @SerialName("is_intensity_technique") val isIntensityTechnique: Boolean = false,
    @SerialName("intensity_technique") val intensityTechnique: String? = null,
    @SerialName("dropset_details") val dropsetDetails: DropsetDetails? = null,
    val rest_seconds: Int? = null,
    val tempo: String? = null,
    val completed: Boolean = false,
    val failed: Boolean = false,
    @SerialName("failure_type") val failureType: String? = null,
    val notes: String? = null
)

@Serializable
data class BulkSetsRequest(
    val sets: List<CreateSetRequest>
)

// PRs
@Serializable
data class PRListResponse(
    val data: List<com.biglifts.workouttracker.data.models.PersonalRecord> = emptyList(),
    val error: String? = null
)

@Serializable
data class CreatePRRequest(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("pr_type") val prType: String = "1rm",
    val weight: Double? = null,
    val reps: Int? = null,
    @SerialName("set_id") val setId: String? = null,
    @SerialName("session_id") val sessionId: String? = null
)

// Stats
@Serializable
data class StatsResponse(
    @SerialName("total_workouts") val totalWorkouts: Int = 0,
    @SerialName("week_workouts") val weekWorkouts: Int = 0,
    @SerialName("recent_prs") val recentPrs: List<com.biglifts.workouttracker.data.models.PersonalRecord> = emptyList()
)

// Templates
@Serializable
data class CreateTemplateRequest(
    val name: String,
    val category: String = "strength",
    @SerialName("split_day") val splitDay: String? = null,
    val goal: String = "strength",
    val notes: String? = null
)

@Serializable
data class TemplateListResponse(
    val data: List<com.biglifts.workouttracker.data.models.WorkoutTemplate> = emptyList(),
    val error: String? = null
)

@Serializable
data class TemplateDataResponse(
    val data: com.biglifts.workouttracker.data.models.WorkoutTemplate? = null,
    val error: String? = null
)

@Serializable
data class TemplateExerciseRequest(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("target_sets") val targetSets: Int = 3,
    @SerialName("target_reps_min") val targetRepsMin: Int = 8,
    @SerialName("target_reps_max") val targetRepsMax: Int = 12,
    @SerialName("target_rpe") val targetRpe: Double? = null
)

@Serializable
data class TemplateSetRequest(
    @SerialName("set_number") val setNumber: Int,
    @SerialName("set_type") val setType: String = "working",
    @SerialName("target_reps") val targetReps: Int = 10,
    @SerialName("target_weight") val targetWeight: Double? = null,
    @SerialName("target_rpe") val targetRpe: Double? = null
)

// Body Measurements
@Serializable
data class BodyMeasurementListResponse(
    val data: List<com.biglifts.workouttracker.data.models.BodyMeasurement> = emptyList(),
    val error: String? = null
)

@Serializable
data class BodyMeasurementDataResponse(
    val data: com.biglifts.workouttracker.data.models.BodyMeasurement? = null,
    val error: String? = null
)

@Serializable
data class CreateBodyMeasurementRequest(
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
    val notes: String? = null
)

// Volume Analytics
@Serializable
data class VolumeAnalyticsResponse(
    @SerialName("volume_by_muscle") val volumeByMuscle: List<MuscleVolume> = emptyList(),
    @SerialName("weekly_volume") val weeklyVolume: List<WeeklyVolume> = emptyList(),
    val error: String? = null
)

@Serializable
data class MuscleVolume(
    val muscle: String,
    val volume: Double,
    @SerialName("change_percentage") val changePercentage: Double? = null
)

@Serializable
data class WeeklyVolume(
    val week: String,
    val volume: Double
)

// Exercise History
@Serializable
data class ExerciseHistoryResponse(
    val history: List<ExerciseHistoryEntry> = emptyList(),
    @SerialName("personal_records") val personalRecords: List<ExercisePR> = emptyList(),
    val error: String? = null
)

@Serializable
data class ExerciseHistoryEntry(
    val date: String,
    val weight: Double? = null,
    val reps: Int? = null,
    @SerialName("estimated_1rm") val estimated1rm: Double? = null,
    val volume: Double? = null,
    val rir: Int? = null,
    val rpe: Double? = null
)

@Serializable
data class ExercisePR(
    val date: String,
    val weight: Double,
    val reps: Int,
    @SerialName("estimated_1rm") val estimated1rm: Double? = null,
    @SerialName("pr_type") val prType: String = "1rm"
)
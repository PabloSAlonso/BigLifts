package com.biglifts.workouttracker.data.api

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiClient(private val context: Context) {

    private val baseUrl = "https://pacwbkqmuripxmfhwnek.supabase.co/functions/v1"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    private fun getToken(): String? = prefs.getString("access_token", null)

    private fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null

    private suspend fun makeRequest(
        endpoint: String,
        method: String = "GET",
        body: Any? = null,
        requireAuth: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = method
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBhY3diaiBxbXVyaXB4bWZod25layIsInJvbGUiOiJhbm9uIiwiaWF0IjoxNzE4NjU2MDAwLCJleHAiOjIwMzQyMzIwMDB9.example-key")

        if (requireAuth) {
            getToken()?.let { token ->
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
        }

        if (body != null && method != "GET") {
            connection.doOutput = true
            val jsonString = json.encodeToString(
                when (body) {
                    is AuthRequest -> AuthRequest.serializer()
                    is OnboardingRequest -> OnboardingRequest.serializer()
                    is CreateWorkoutRequest -> CreateWorkoutRequest.serializer()
                    is AddExerciseRequest -> AddExerciseRequest.serializer()
                    is CreateSetRequest -> CreateSetRequest.serializer()
                    is BulkSetsRequest -> BulkSetsRequest.serializer()
                    is CreatePRRequest -> CreatePRRequest.serializer()
                    is CreateTemplateRequest -> CreateTemplateRequest.serializer()
                    is CreateBodyMeasurementRequest -> CreateBodyMeasurementRequest.serializer()
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val mapBody = body as Map<String, Any?>
                        return@let json.encodeToString(
                            kotlinx.serialization.json.JsonObject.serializer(),
                            kotlinx.serialization.json.JsonObject(
                                mapBody.mapValues { (_, value) ->
                                    when (value) {
                                        is String -> kotlinx.serialization.json.JsonPrimitive(value)
                                        is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                                        is Boolean -> kotlinx.serialization.json.JsonPrimitive(value)
                                        else -> kotlinx.serialization.json.JsonNull
                                    }
                                }
                            )
                        )
                    }
                    else -> throw IllegalArgumentException("Unknown body type")
                },
                body
            )
            OutputStreamWriter(connection.outputStream).use { it.write(jsonString) }
        }

        val responseCode = connection.responseCode
        val inputStream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val response = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

        if (responseCode !in 200..299) {
            throw ApiException(responseCode, response)
        }

        response
    }

    // Auth
    suspend fun register(email: String, password: String, username: String): AuthResponse {
        val response = makeRequest(
            "auth",
            "POST",
            AuthRequest(action = "register", email = email, password = password, username = username)
        )
        return json.decodeFromString<AuthResponse>(response)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val response = makeRequest(
            "auth",
            "POST",
            AuthRequest(action = "login", email = email, password = password)
        )
        val authResponse = json.decodeFromString<AuthResponse>(response)
        authResponse.session?.let { saveTokens(it.accessToken, it.refreshToken) }
        return authResponse
    }

    suspend fun refreshToken(): AuthResponse {
        val refreshToken = prefs.getString("refresh_token", null)
            ?: throw ApiException(401, "No refresh token")
        val response = makeRequest(
            "auth",
            "POST",
            AuthRequest(action = "refresh", refresh_token = refreshToken)
        )
        val authResponse = json.decodeFromString<AuthResponse>(response)
        authResponse.session?.let { saveTokens(it.accessToken, it.refreshToken) }
        return authResponse
    }

    // Profile
    suspend fun getProfile(): com.biglifts.workouttracker.data.models.Profile {
        val response = makeRequest("profile", requireAuth = true)
        return json.decodeFromString<ProfileResponse>(response).data
            ?: throw ApiException(404, "Profile not found")
    }

    suspend fun updateProfile(profile: com.biglifts.workouttracker.data.models.Profile): com.biglifts.workouttracker.data.models.Profile {
        val response = makeRequest("profile", "PUT", profile, requireAuth = true)
        return json.decodeFromString<ProfileResponse>(response).data
            ?: throw ApiException(500, "Failed to update profile")
    }

    suspend fun completeOnboarding(request: OnboardingRequest): com.biglifts.workouttracker.data.models.Profile {
        val response = makeRequest("profile/onboarding", "POST", request, requireAuth = true)
        return json.decodeFromString<ProfileResponse>(response).data
            ?: throw ApiException(500, "Failed to save onboarding")
    }

    suspend fun getStats(): StatsResponse {
        val response = makeRequest("profile/stats", requireAuth = true)
        return json.decodeFromString<StatsResponse>(response)
    }

    // Workouts
    suspend fun getWorkouts(limit: Int = 50, offset: Int = 0): WorkoutListResponse {
        val response = makeRequest("api/workouts?limit=$limit&offset=$offset", requireAuth = true)
        return json.decodeFromString<WorkoutListResponse>(response)
    }

    suspend fun createWorkout(request: CreateWorkoutRequest): com.biglifts.workouttracker.data.models.WorkoutSession {
        val response = makeRequest("api/workouts", "POST", request, requireAuth = true)
        return json.decodeFromString<WorkoutDataResponse>(response).data
            ?: throw ApiException(500, "Failed to create workout")
    }

    suspend fun updateWorkout(id: String, updates: Map<String, Any?>): com.biglifts.workouttracker.data.models.WorkoutSession {
        val response = makeRequest("api/workouts/$id", "PUT", updates, requireAuth = true)
        return json.decodeFromString<WorkoutDataResponse>(response).data
            ?: throw ApiException(500, "Failed to update workout")
    }

    suspend fun deleteWorkout(id: String) {
        makeRequest("api/workouts/$id", "DELETE", requireAuth = true)
    }

    // Session Exercises
    suspend fun getSessionExercises(sessionId: String): List<SessionExerciseWithExercise> {
        val response = makeRequest("api/workouts/$sessionId/exercises", requireAuth = true)
        return json.decodeFromString<SessionExerciseResponse>(response).data
    }

    suspend fun addExerciseToSession(sessionId: String, request: AddExerciseRequest): SessionExerciseWithExercise {
        val response = makeRequest("api/workouts/$sessionId/exercises", "POST", request, requireAuth = true)
        return json.decodeFromString<SessionExerciseResponse>(response).data.firstOrNull()
            ?: throw ApiException(500, "Failed to add exercise")
    }

    // Sets
    suspend fun getSets(sessionExerciseId: String): List<com.biglifts.workouttracker.data.models.WorkoutSet> {
        val response = makeRequest("sets?session_exercise_id=$sessionExerciseId", requireAuth = true)
        return json.decodeFromString<SetListResponse>(response).data
    }

    suspend fun createSet(request: CreateSetRequest): com.biglifts.workouttracker.data.models.WorkoutSet {
        val response = makeRequest("sets", "POST", request, requireAuth = true)
        return json.decodeFromString<SetDataResponse>(response).data
            ?: throw ApiException(500, "Failed to create set")
    }

    suspend fun updateSet(id: String, updates: Map<String, Any?>): com.biglifts.workouttracker.data.models.WorkoutSet {
        val response = makeRequest("sets/$id", "PUT", updates, requireAuth = true)
        return json.decodeFromString<SetDataResponse>(response).data
            ?: throw ApiException(500, "Failed to update set")
    }

    suspend fun deleteSet(id: String) {
        makeRequest("sets/$id", "DELETE", requireAuth = true)
    }

    suspend fun createBulkSets(sets: List<CreateSetRequest>): List<com.biglifts.workouttracker.data.models.WorkoutSet> {
        val response = makeRequest("sets/bulk", "POST", BulkSetsRequest(sets), requireAuth = true)
        return json.decodeFromString<SetListResponse>(response).data
    }

    // Exercises
    suspend fun getExercises(category: String? = null, search: String? = null): List<com.biglifts.workouttracker.data.models.Exercise> {
        val params = mutableListOf<String>()
        category?.let { params.add("category=$it") }
        search?.let { params.add("search=$it") }
        val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        val response = makeRequest("api/exercises$query", requireAuth = true)
        return json.decodeFromString<ExerciseListResponse>(response).data
    }

    // PRs
    suspend fun getPersonalRecords(exerciseId: String? = null): List<com.biglifts.workouttracker.data.models.PersonalRecord> {
        val param = exerciseId?.let { "?exercise_id=$it" } ?: ""
        val response = makeRequest("sets/prs$param", requireAuth = true)
        return json.decodeFromString<PRListResponse>(response).data
    }

    suspend fun createPR(request: CreatePRRequest): com.biglifts.workouttracker.data.models.PersonalRecord {
        val response = makeRequest("sets/prs", "POST", request, requireAuth = true)
        return json.decodeFromString<PRListResponse>(response).data.firstOrNull()
            ?: throw ApiException(500, "Failed to create PR")
    }

    // Templates
    suspend fun getTemplates(): List<com.biglifts.workouttracker.data.models.WorkoutTemplate> {
        val response = makeRequest("body/templates", requireAuth = true)
        return json.decodeFromString<TemplateListResponse>(response).data
    }

    suspend fun createTemplate(request: CreateTemplateRequest): com.biglifts.workouttracker.data.models.WorkoutTemplate? {
        val response = makeRequest("body/templates", "POST", request, requireAuth = true)
        return json.decodeFromString<TemplateDataResponse>(response).data
    }

    suspend fun deleteTemplate(id: String) {
        makeRequest("body/templates/$id", "DELETE", requireAuth = true)
    }

    suspend fun startFromTemplate(templateId: String): com.biglifts.workouttracker.data.models.WorkoutSession? {
        val response = makeRequest("body/templates/$templateId/start", "POST", requireAuth = true)
        return json.decodeFromString<WorkoutDataResponse>(response).data
    }

    // Body Measurements
    suspend fun getBodyMeasurements(): List<com.biglifts.workouttracker.data.models.BodyMeasurement> {
        val response = makeRequest("body/measurements", requireAuth = true)
        return json.decodeFromString<BodyMeasurementListResponse>(response).data
    }

    suspend fun createBodyMeasurement(request: CreateBodyMeasurementRequest): com.biglifts.workouttracker.data.models.BodyMeasurement? {
        val response = makeRequest("body/measurements", "POST", request, requireAuth = true)
        return json.decodeFromString<BodyMeasurementDataResponse>(response).data
    }

    suspend fun deleteBodyMeasurement(id: String) {
        makeRequest("body/measurements/$id", "DELETE", requireAuth = true)
    }

    // Volume Analytics
    suspend fun getVolumeAnalytics(): VolumeAnalyticsResponse {
        val response = makeRequest("body/volume/analytics", requireAuth = true)
        return json.decodeFromString<VolumeAnalyticsResponse>(response)
    }

    // Exercise History
    suspend fun getExerciseHistory(exerciseId: String): ExerciseHistoryResponse {
        val response = makeRequest("body/exercises/$exerciseId/history", requireAuth = true)
        return json.decodeFromString<ExerciseHistoryResponse>(response)
    }
}

class ApiException(val code: Int, override val message: String) : Exception(message)
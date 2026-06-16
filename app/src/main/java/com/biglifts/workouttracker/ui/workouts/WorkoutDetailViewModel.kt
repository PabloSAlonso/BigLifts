package com.biglifts.workouttracker.ui.workouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.SessionExerciseWithExercise
import com.biglifts.workouttracker.data.models.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _workout = MutableStateFlow<WorkoutSession?>(null)
    val workout: StateFlow<WorkoutSession?> = _workout

    private val _exercises = MutableStateFlow<List<SessionExerciseWithExercise>>(emptyList())
    val exercises: StateFlow<List<SessionExerciseWithExercise>> = _exercises

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() { _error.value = null }

    fun loadWorkoutDetail(workoutId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load workout sessions and find the one
                val response = apiClient.getWorkouts()
                _workout.value = response.data.find { it.id == workoutId }

                // Load exercises
                val exercises = apiClient.getSessionExercises(workoutId)
                _exercises.value = exercises
            } catch (e: Exception) {
                _error.value = "Failed to load workout details: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
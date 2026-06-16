package com.biglifts.workouttracker.ui.workouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.models.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _workouts = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workouts: StateFlow<List<WorkoutSession>> = _workouts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() { _error.value = null }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiClient.getWorkouts()
                _workouts.value = response.data
            } catch (e: Exception) {
                _workouts.value = emptyList()
                _error.value = "Failed to load workouts: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch {
            try {
                apiClient.deleteWorkout(id)
                _workouts.value = _workouts.value.filter { it.id != id }
            } catch (e: Exception) {
                _error.value = "Failed to delete workout: ${e.localizedMessage}"
            }
        }
    }
}
package com.biglifts.workouttracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.StatsResponse
import com.biglifts.workouttracker.data.models.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _recentWorkouts = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val recentWorkouts: StateFlow<List<WorkoutSession>> = _recentWorkouts

    private val _stats = MutableStateFlow(StatsResponse())
    val stats: StateFlow<StatsResponse> = _stats

    fun loadData() {
        viewModelScope.launch {
            try {
                val workouts = apiClient.getWorkouts(limit = 5)
                _recentWorkouts.value = workouts.data

                val stats = apiClient.getStats()
                _stats.value = stats
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
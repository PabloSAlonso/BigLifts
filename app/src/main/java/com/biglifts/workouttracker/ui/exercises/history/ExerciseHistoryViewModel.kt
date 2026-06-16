package com.biglifts.workouttracker.ui.exercises.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.ExerciseHistoryEntry
import com.biglifts.workouttracker.data.models.PersonalRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _history = MutableStateFlow<List<ExerciseHistoryEntry>>(emptyList())
    val history: StateFlow<List<ExerciseHistoryEntry>> = _history

    private val _pr = MutableStateFlow<PersonalRecord?>(null)
    val pr: StateFlow<PersonalRecord?> = _pr

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadExerciseHistory(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiClient.getExerciseHistory(exerciseId)
                _history.value = response.history

                val prs = apiClient.getPersonalRecords(exerciseId)
                _pr.value = prs.firstOrNull()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
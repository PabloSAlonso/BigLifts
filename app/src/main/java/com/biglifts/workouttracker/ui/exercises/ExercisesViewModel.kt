package com.biglifts.workouttracker.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.models.Exercise
import com.biglifts.workouttracker.data.repositories.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val exercises = exerciseRepository.getExercises()
            _allExercises.value = exercises
            _exercises.value = exercises
        }
    }

    fun searchExercises(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _exercises.value = _allExercises.value
            } else {
                _exercises.value = exerciseRepository.searchExercises(query)
            }
        }
    }

    fun filterByCategory(category: String?) {
        viewModelScope.launch {
            if (category == null) {
                _exercises.value = _allExercises.value
            } else {
                _exercises.value = exerciseRepository.getExercisesByCategory(category)
            }
        }
    }
}
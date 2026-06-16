package com.biglifts.workouttracker.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.models.*
import com.biglifts.workouttracker.data.repositories.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val templateRepository: WorkoutTemplateRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val sessions: StateFlow<List<WorkoutSession>> = _sessions

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession

    private val _sessionExercises = MutableStateFlow<List<SessionExercise>>(emptyList())
    val sessionExercises: StateFlow<List<SessionExercise>> = _sessionExercises

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadSessions(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _sessions.value = sessionRepository.getSessions(userId)
            _isLoading.value = false
        }
    }

    fun loadTemplates(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _templates.value = templateRepository.getTemplates(userId)
            _isLoading.value = false
        }
    }

    fun loadExercises() {
        viewModelScope.launch {
            _exercises.value = exerciseRepository.getExercises()
        }
    }

    fun startWorkout(name: String, userId: String, templateId: String? = null) {
        viewModelScope.launch {
            val session = WorkoutSession(
                userId = userId,
                templateId = templateId,
                name = name,
                startedAt = java.time.Instant.now().toString()
            )
            _currentSession.value = sessionRepository.createSession(session)
        }
    }

    fun addExerciseToSession(exerciseId: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val sessionExercise = SessionExercise(
                sessionId = session.id ?: "",
                exerciseId = exerciseId,
                orderIndex = _sessionExercises.value.size
            )
            val saved = sessionRepository.addExerciseToSession(sessionExercise)
            _sessionExercises.value = _sessionExercises.value + saved
        }
    }

    fun addSet(sessionExerciseId: String, weight: Double?, reps: Int?) {
        viewModelScope.launch {
            val setNumber = _sessionExercises.value
                .find { it.id == sessionExerciseId }
                ?.let { sessionRepository.getSets(it.id ?: "").size }
                ?: 0

            val set = WorkoutSet(
                sessionExerciseId = sessionExerciseId,
                setNumber = setNumber + 1,
                weight = weight,
                reps = reps,
                completed = true
            )
            sessionRepository.addSet(set)
        }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val completedSession = session.copy(
                completedAt = java.time.Instant.now().toString()
            )
            sessionRepository.updateSession(completedSession)
            _currentSession.value = null
            _sessionExercises.value = emptyList()
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(id)
            _sessions.value = _sessions.value.filter { it.id != id }
        }
    }
}
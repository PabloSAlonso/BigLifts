package com.biglifts.workouttracker.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.SessionExerciseWithExercise
import com.biglifts.workouttracker.data.models.Exercise
import com.biglifts.workouttracker.data.models.WorkoutSession
import com.biglifts.workouttracker.data.models.WorkoutSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _currentWorkout = MutableStateFlow<WorkoutSession?>(null)
    val currentWorkout: StateFlow<WorkoutSession?> = _currentWorkout

    private val _activeExercises = MutableStateFlow<List<ActiveExerciseData>>(emptyList())
    val activeExercises: StateFlow<List<ActiveExerciseData>> = _activeExercises

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() { _error.value = null }

    private var workoutId: String? = null
    private val previousDataCache = mutableMapOf<String, Triple<List<WorkoutSet>, Double?, Int?>>()

    data class ActiveExerciseData(
        val exerciseId: String,
        val exercise: Exercise,
        val sessionExerciseId: String? = null,
        val sets: List<WorkoutSet> = emptyList(),
        val previousSets: List<WorkoutSet> = emptyList(),
        val previousBestWeight: Double? = null,
        val previousBestReps: Int? = null,
        val orderIndex: Int = 0
    )

    fun startNewWorkout(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val workout = apiClient.createWorkout(
                    com.biglifts.workouttracker.data.api.CreateWorkoutRequest(name = name)
                )
                _currentWorkout.value = workout
                workoutId = workout.id
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_create_workout)} ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWorkout(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            workoutId = id
            try {
                // Load workout
                val workouts = apiClient.getWorkouts()
                _currentWorkout.value = workouts.data.find { it.id == id }

                // Load existing exercises
                val exercises = apiClient.getSessionExercises(id)
                val activeExercises = exercises.map { se ->
                    val previousData = getPreviousExerciseData(se.exerciseId)
                    ActiveExerciseData(
                        exerciseId = se.exerciseId,
                        exercise = se.exercises ?: Exercise(name = getApplication<Application>().getString(R.string.unknown), category = "other"),
                        sessionExerciseId = se.id,
                        previousSets = previousData.first,
                        previousBestWeight = previousData.second,
                        previousBestReps = previousData.third
                    )
                }
                _activeExercises.value = activeExercises
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_load_workout)} ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            val currentWorkoutId = workoutId ?: return@launch
            _isLoading.value = true
            try {
                val sessionExercise = apiClient.addExerciseToSession(
                    currentWorkoutId,
                    com.biglifts.workouttracker.data.api.AddExerciseRequest(
                        exerciseId = exercise.id ?: "",
                        orderIndex = _activeExercises.value.size
                    )
                )

                val previousData = getPreviousExerciseData(exercise.id ?: "")

                val newExercise = ActiveExerciseData(
                    exerciseId = exercise.id ?: "",
                    exercise = exercise,
                    sessionExerciseId = sessionExercise.id,
                    previousSets = previousData.first,
                    previousBestWeight = previousData.second,
                    previousBestReps = previousData.third,
                    orderIndex = _activeExercises.value.size
                )

                _activeExercises.value = _activeExercises.value + newExercise
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_add_exercise)} ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSet(exerciseId: String) {
        addSetWithTechnique(exerciseId, null)
    }

    fun addSetWithTechnique(exerciseId: String, technique: String?) {
        viewModelScope.launch {
            val exercise = _activeExercises.value.find { it.exerciseId == exerciseId } ?: return@launch
            val sessionExerciseId = exercise.sessionExerciseId ?: return@launch

            val setNumber = exercise.sets.size + 1

            // Pre-fill from previous data if available
            val previousSet = exercise.previousSets.find { it.setNumber == setNumber }

            val newSet = com.biglifts.workouttracker.data.api.CreateSetRequest(
                sessionExerciseId = sessionExerciseId,
                setNumber = setNumber,
                weight = previousSet?.weight,
                reps = previousSet?.reps,
                rir = previousSet?.rir,
                intensityTechnique = technique,
                isIntensityTechnique = technique != null
            )

            try {
                val createdSet = apiClient.createSet(newSet)
                updateExerciseSets(exerciseId, exercise.sets + createdSet)
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_add_set)} ${e.localizedMessage}"
            }
        }
    }

    fun updateSet(exerciseId: String, set: WorkoutSet) {
        viewModelScope.launch {
            try {
                apiClient.updateSet(set.id ?: "", mapOf(
                    "weight" to set.weight,
                    "reps" to set.reps,
                    "rir" to set.rir,
                    "completed" to set.completed
                ))

                val exercise = _activeExercises.value.find { it.exerciseId == exerciseId } ?: return@launch
                val updatedSets = exercise.sets.map { if (it.id == set.id) set else it }
                updateExerciseSets(exerciseId, updatedSets)
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_update_set)} ${e.localizedMessage}"
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        viewModelScope.launch {
            _activeExercises.value = _activeExercises.value.filter { it.exerciseId != exerciseId }
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            val id = workoutId ?: return@launch
            try {
                apiClient.updateWorkout(id, mapOf(
                    "completed_at" to java.time.Instant.now().toString(),
                    "duration_minutes" to ((System.currentTimeMillis() - (_currentWorkout.value?.startedAt?.let {
                        java.time.Instant.parse(it).toEpochMilli()
                    } ?: System.currentTimeMillis())) / 60000).toInt()
                ))
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_finish_workout)} ${e.localizedMessage}"
            }
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            workoutId?.let {
                try {
                    apiClient.deleteWorkout(it)
                } catch (e: Exception) {
                    _error.value = "${getApplication<Application>().getString(R.string.failed_discard_workout)} ${e.localizedMessage}"
                }
            }
        }
    }

    private fun updateExerciseSets(exerciseId: String, sets: List<WorkoutSet>) {
        _activeExercises.value = _activeExercises.value.map {
            if (it.exerciseId == exerciseId) it.copy(sets = sets) else it
        }
    }

    private suspend fun getPreviousExerciseData(exerciseId: String): Triple<List<WorkoutSet>, Double?, Int?> {
        previousDataCache[exerciseId]?.let { return it }

        try {
            val allWorkouts = apiClient.getWorkouts()

            // Batch: collect all session exercises for non-current workouts first
            val otherWorkouts = allWorkouts.data.filter { it.id != workoutId }
            val allSessionExercises = otherWorkouts.mapNotNull { workout ->
                workout.id?.let { wid ->
                    try {
                        wid to apiClient.getSessionExercises(wid)
                    } catch (e: Exception) {
                        null
                    }
                }
            }.toMap()

            // Find the last workout that had this exercise
            for ((_, exercises) in allSessionExercises) {
                val exercise = exercises.find { it.exerciseId == exerciseId } ?: continue

                exercise.id?.let { sessionExerciseId ->
                    val sets = apiClient.getSets(sessionExerciseId)
                    if (sets.isNotEmpty()) {
                        val bestSet = sets.maxByOrNull { it.weight ?: 0.0 }
                        val result = Triple(sets, bestSet?.weight, bestSet?.reps)
                        previousDataCache[exerciseId] = result
                        return result
                    }
                }
            }
        } catch (e: Exception) {
            // Previous data not critical - fail silently with log
        }
        return Triple(emptyList(), null, null)
    }
}
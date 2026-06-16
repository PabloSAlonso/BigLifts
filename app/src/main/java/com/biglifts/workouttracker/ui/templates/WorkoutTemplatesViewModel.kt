package com.biglifts.workouttracker.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.CreateTemplateRequest
import com.biglifts.workouttracker.data.api.TemplateExerciseRequest
import com.biglifts.workouttracker.data.api.TemplateSetRequest
import com.biglifts.workouttracker.data.models.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutTemplatesViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val api = ApiClient(application)

    private val _templates = MutableLiveData<List<WorkoutTemplate>>(emptyList())
    val templates: LiveData<List<WorkoutTemplate>> = _templates

    private val _navigateToActiveWorkout = MutableLiveData<String?>()
    val navigateToActiveWorkout: LiveData<String?> = _navigateToActiveWorkout

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() { _error.value = null }

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            try {
                val result = api.getTemplates()
                _templates.value = result ?: emptyList()
            } catch (e: Exception) {
                _templates.value = emptyList()
            }
        }
    }

    fun createTemplate() {
        viewModelScope.launch {
            try {
                val request = CreateTemplateRequest(
                    name = getApplication<Application>().getString(R.string.new_template_default),
                    category = "strength",
                    splitDay = null,
                    goal = "strength",
                    notes = null
                )
                val template = api.createTemplate(request)
                if (template != null) {
                    loadTemplates()
                }
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_create_template)} ${e.localizedMessage}"
            }
        }
    }

    fun editTemplate(template: WorkoutTemplate) {
        // TODO: Navigate to edit screen
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            try {
                api.deleteTemplate(template.id)
                loadTemplates()
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_delete_template)} ${e.localizedMessage}"
            }
        }
    }

    fun startFromTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            try {
                val session = api.startFromTemplate(template.id)
                if (session != null) {
                    _navigateToActiveWorkout.value = session.id
                }
            } catch (e: Exception) {
                _error.value = "${getApplication<Application>().getString(R.string.failed_start_template)} ${e.localizedMessage}"
            }
        }
    }

    fun onNavigated() {
        _navigateToActiveWorkout.value = null
    }
}
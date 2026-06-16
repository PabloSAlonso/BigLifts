package com.biglifts.workouttracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    val uiState: kotlinx.coroutines.flow.StateFlow<AuthUiState>
        get() = _uiState

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    fun completeOnboarding(
        age: Int, gender: String, heightCm: Double, weightKg: Double,
        targetWeightKg: Double?, activityLevel: String, experience: String,
        goal: String, daysPerWeek: Int, injuries: String?, equipment: String?
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val profile = apiClient.completeOnboarding(
                    com.biglifts.workouttracker.data.api.OnboardingRequest(
                        age = age,
                        gender = gender,
                        height_cm = heightCm,
                        current_weight_kg = weightKg,
                        target_weight_kg = targetWeightKg,
                        activity_level = activityLevel,
                        training_experience = experience,
                        primary_goal = goal,
                        training_days_per_week = daysPerWeek,
                        injuries = injuries,
                        available_equipment = equipment
                    )
                )
                _uiState.value = AuthUiState.OnboardingComplete
            } catch (e: com.biglifts.workouttracker.data.api.ApiException) {
                _uiState.value = AuthUiState.Error(e.message)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(getApplication<Application>().getString(R.string.connection_error))
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
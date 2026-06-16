package com.biglifts.workouttracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.ApiException
import com.biglifts.workouttracker.data.api.AuthRequest
import com.biglifts.workouttracker.data.api.OnboardingRequest
import com.biglifts.workouttracker.data.models.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    val isLoggedIn: Boolean get() = apiClient.isLoggedIn()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = apiClient.login(email, password)
                if (response.error != null) {
                    _uiState.value = AuthUiState.Error(response.error)
                } else {
                    _uiState.value = AuthUiState.Success
                }
            } catch (e: ApiException) {
                _uiState.value = AuthUiState.Error(parseError(e.message))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Connection error. Please try again.")
            }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = apiClient.register(email, password, username)
                if (response.error != null) {
                    _uiState.value = AuthUiState.Error(response.error)
                } else {
                    _uiState.value = AuthUiState.Registered
                }
            } catch (e: ApiException) {
                _uiState.value = AuthUiState.Error(parseError(e.message))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Connection error. Please try again.")
            }
        }
    }

    fun completeOnboarding(
        age: Int, gender: String, heightCm: Double, weightKg: Double,
        targetWeightKg: Double?, activityLevel: String, experience: String,
        goal: String, daysPerWeek: Int, injuries: String?, equipment: String?
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val profile = apiClient.completeOnboarding(
                    OnboardingRequest(
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
                _profile.value = profile
                _uiState.value = AuthUiState.OnboardingComplete
            } catch (e: ApiException) {
                _uiState.value = AuthUiState.Error(parseError(e.message))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Connection error. Please try again.")
            }
        }
    }

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() { _error.value = null }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _profile.value = apiClient.getProfile()
            } catch (e: Exception) {
                _error.value = "Failed to load profile"
            }
        }
    }

    fun logout() {
        apiClient.clearTokens()
        _uiState.value = AuthUiState.LoggedOut
        _profile.value = null
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun parseError(error: String): String {
        return when {
            error.contains("already registered") -> "Email already registered"
            error.contains("Invalid login") -> "Invalid email or password"
            error.contains("Password should") -> "Password must be at least 6 characters"
            else -> error.take(100)
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object Registered : AuthUiState()
    object OnboardingComplete : AuthUiState()
    object LoggedOut : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
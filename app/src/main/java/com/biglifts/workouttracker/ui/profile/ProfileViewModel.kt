package com.biglifts.workouttracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.models.Profile
import com.biglifts.workouttracker.data.repositories.AuthRepository
import com.biglifts.workouttracker.data.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.id ?: return@launch
            _profile.value = profileRepository.getProfile(userId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            _profile.value = profileRepository.updateProfile(profile)
        }
    }
}
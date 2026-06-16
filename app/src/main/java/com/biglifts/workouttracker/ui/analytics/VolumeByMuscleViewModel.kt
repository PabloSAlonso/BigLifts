package com.biglifts.workouttracker.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.MuscleVolume
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolumeByMuscleViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val api = ApiClient(application)

    private val _volumeData = MutableLiveData<List<MuscleVolume>>(emptyList())
    val volumeData: LiveData<List<MuscleVolume>> = _volumeData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() { _error.value = null }

    init {
        loadVolumeData()
    }

    fun loadVolumeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getVolumeAnalytics()
                _volumeData.value = response.volumeByMuscle
            } catch (e: Exception) {
                _volumeData.value = emptyList()
                _error.value = "${getApplication<Application>().getString(R.string.failed_load_volume)} ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
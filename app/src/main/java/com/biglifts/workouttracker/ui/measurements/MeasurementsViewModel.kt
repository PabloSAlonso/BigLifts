package com.biglifts.workouttracker.ui.measurements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.api.CreateBodyMeasurementRequest
import com.biglifts.workouttracker.data.models.BodyMeasurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiClient = ApiClient(application)

    private val _measurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())
    val measurements: StateFlow<List<BodyMeasurement>> = _measurements

    fun loadMeasurements() {
        viewModelScope.launch {
            try {
                val result = apiClient.getBodyMeasurements()
                _measurements.value = result
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addMeasurement(
        weight: Double? = null,
        bodyFatPercentage: Double? = null,
        neck: Double? = null,
        shoulders: Double? = null,
        chest: Double? = null,
        arms: Double? = null,
        waist: Double? = null,
        hips: Double? = null,
        thighs: Double? = null,
        calves: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                val request = CreateBodyMeasurementRequest(
                    weight = weight,
                    bodyFatPercentage = bodyFatPercentage,
                    neck = neck,
                    shoulders = shoulders,
                    chest = chest,
                    arms = arms,
                    waist = waist,
                    hips = hips,
                    thighs = thighs,
                    calves = calves,
                    notes = notes
                )
                apiClient.createBodyMeasurement(request)
                loadMeasurements()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            try {
                apiClient.deleteBodyMeasurement(id)
                loadMeasurements()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
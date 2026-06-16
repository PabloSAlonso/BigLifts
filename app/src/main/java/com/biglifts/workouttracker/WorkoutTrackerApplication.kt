package com.biglifts.workouttracker

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.biglifts.workouttracker.data.supabase.SupabaseManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WorkoutTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SupabaseManager.initialize(this)
    }
}
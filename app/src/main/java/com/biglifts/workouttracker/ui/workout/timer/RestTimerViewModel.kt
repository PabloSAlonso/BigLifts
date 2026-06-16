package com.biglifts.workouttracker.ui.workout.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RestTimerViewModel @Inject constructor() : ViewModel() {

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _restDuration = MutableStateFlow(90)
    val restDuration: StateFlow<Int> = _restDuration

    private var timer: CountDownTimer? = null

    fun setRestDuration(seconds: Int) {
        _restDuration.value = seconds
    }

    fun startTimer(context: Context? = null) {
        timer?.cancel()
        val duration = _restDuration.value
        _timeRemaining.value = duration
        _isRunning.value = true

        timer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timeRemaining.value = 0
                _isRunning.value = false
                context?.let { playNotification(it) }
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        _isRunning.value = false
    }

    fun resumeTimer(context: Context? = null) {
        if (_timeRemaining.value > 0) {
            _isRunning.value = true
            timer = object : CountDownTimer(_timeRemaining.value * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timeRemaining.value = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    _timeRemaining.value = 0
                    _isRunning.value = false
                    context?.let { playNotification(it) }
                }
            }.start()
        }
    }

    fun stopTimer() {
        timer?.cancel()
        _timeRemaining.value = 0
        _isRunning.value = false
    }

    fun addTime(seconds: Int) {
        val current = _timeRemaining.value
        _timeRemaining.value = current + seconds
        if (_isRunning.value) {
            timer?.cancel()
            timer = object : CountDownTimer(_timeRemaining.value * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timeRemaining.value = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    _timeRemaining.value = 0
                    _isRunning.value = false
                }
            }.start()
        }
    }

    private fun playNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "rest_timer",
                "Rest Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer finished"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, "rest_timer")
            .setSmallIcon(com.biglifts.workouttracker.R.drawable.ic_timer)
            .setContentTitle("Rest Timer")
            .setContentText("Time to lift!")
            .setSound(sound)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
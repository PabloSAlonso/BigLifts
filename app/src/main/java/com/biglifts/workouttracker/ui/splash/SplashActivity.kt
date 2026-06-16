package com.biglifts.workouttracker.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val api = ApiClient(applicationContext)
            val intent = Intent(this, MainActivity::class.java).apply {
                if (!api.isLoggedIn()) {
                    putExtra("START_DESTINATION", "login")
                }
            }
            startActivity(intent)
            finish()
        }, 1500)
    }
}
package com.dicoding.capstonui.SplashScreenApp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.login.LoginActivity
import com.dicoding.capstonui.welcome.WelcomeActivity

class SplashScreenActivity : AppCompatActivity() {

    private val delayMillis: Long = 2000 // Adjust delay time as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish() // Finish current activity to prevent going back to splash screen
        }, delayMillis)

    }

}

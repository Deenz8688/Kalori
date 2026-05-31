package com.deenzstudios.kalori

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_welcome)

        Handler(Looper.getMainLooper()).postDelayed({

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()

        }, 2500) // 2.5 saat

    }
}
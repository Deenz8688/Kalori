package com.deenzstudios.kalori

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private var selectedTabId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Dapatkan semua button
        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        val navKalori = findViewById<LinearLayout>(R.id.nav_kalori)
        val navReport = findViewById<LinearLayout>(R.id.nav_report)
        val navMe = findViewById<LinearLayout>(R.id.nav_me)

        // Set click listener
        navHome.setOnClickListener { selectTab(R.id.nav_home) }
        navKalori.setOnClickListener { selectTab(R.id.nav_kalori) }
        navReport.setOnClickListener { selectTab(R.id.nav_report) }
        navMe.setOnClickListener { selectTab(R.id.nav_me) }

        // Default: pilih Home
        selectTab(R.id.nav_home)

        // ================= NOTIFIKASI =================
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        createNotificationChannel()
        setupMealReminders()
    }

    private fun selectTab(tabId: Int) {
        // Reset semua button
        resetAllTabs()

        // Set selected pada button yang diklik
        selectedTabId = tabId

        when (tabId) {
            R.id.nav_home -> {
                findViewById<LinearLayout>(R.id.nav_home).isSelected = true
                replaceFragment(HomeFragment())
            }
            R.id.nav_kalori -> {
                findViewById<LinearLayout>(R.id.nav_kalori).isSelected = true
                replaceFragment(KaloriFragment())
            }
            R.id.nav_report -> {
                findViewById<LinearLayout>(R.id.nav_report).isSelected = true
                replaceFragment(ReportFragment())
            }
            R.id.nav_me -> {
                findViewById<LinearLayout>(R.id.nav_me).isSelected = true
                replaceFragment(MeFragment())
            }
        }
    }

    private fun resetAllTabs() {
        val tabs = listOf(
            R.id.nav_home, R.id.nav_kalori,
            R.id.nav_report, R.id.nav_me
        )
        tabs.forEach { id ->
            findViewById<LinearLayout>(id).isSelected = false
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // ================= NOTIFIKASI =================
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Notifikasi Waktu Makan"
            val descriptionText = "Saluran amaran untuk reminding waktu makan harian"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT

            val channel = android.app.NotificationChannel("waktu_makan_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: android.app.NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMealReminders() {
        setAlarm(1, 8, 0, "Selamat Pagi! 🍳", "Dah sarapan? pastikan makan makanan yang berkhasiat pagi ini, jangan lupa rekot menu sarapan anda")
        setAlarm(2, 13, 0, "Waktu Makan Tengah Hari! 🍽️", "Pastikan pilih menu yang seimbang untuk tengah hari ini")
        setAlarm(3, 19, 30, "Makan Malam Dah Tiba! 🌙", "Jangan makan terlalu banyak! pastikan kalori tidak melebihi had harian anda.")
    }

    private fun setAlarm(requestCode: Int, hour: Int, minute: Int, title: String, message: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
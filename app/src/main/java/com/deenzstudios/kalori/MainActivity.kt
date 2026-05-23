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
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Buka HomeFragment dulu sebagai default
        replaceFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_kalori -> {
                    replaceFragment(KaloriFragment())
                    true
                }
                R.id.nav_report -> {
                    replaceFragment(ReportFragment())
                    true
                }
                R.id.nav_Me -> {
                    replaceFragment(MeFragment())
                    true
                }
                else -> true
            }
        }

        // ================= LANGKAH 2: Kotak Minta Izin Notifikasi =================
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

        // ================= LANGKAH 3: Bina Saluran Notifikasi =================
        createNotificationChannel()

        // 🔥 LANGKAH 7: PASANG JAM AUTOMATIK (Pagi, Tengah Hari, Malam)
        setupMealReminders()

    } // <-- Penutup onCreate

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // ================= LANGKAH 3 (FUNGSI): Kilang Saluran Notifikasi =================
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Notifikasi Waktu Makan"
            val descriptionText = "Saluran amaran untuk reminding waktu makan harian"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT

            val channel = android.app.NotificationChannel("waktu_makan_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 🔥 LANGKAH 7 (FUNGSI): Menguruskan Jam Alarm Di Latar Belakang
    private fun setupMealReminders() {

        setAlarm(1, 8, 0, "Selamat Pagi! 🍳", "Dah sarapan? pastikan makan makanan yang berkhasiat pagi ini, jangan lupa rekot menu sarapan anda")
        setAlarm(2, 13, 0, "Waktu Makan Tengah Hari! 🍽️", "Pastikan pilih menu yang seimbang untuk tengah hari ini")
        setAlarm(3, 19, 30, "Makan Malam Dah Tiba! 🌙", "Jangan makan terlalu banyak! pastikan kalori tidak melebihi had harian anda.")
    }

    // Fungsi bantuan untuk kunci jam sistem telefon
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

        // 📅 Tetapan jadual Calendar harian yang kalis ralat
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0) // 🔥 BARIS WAJIB: Kosongkan milisaat supaya perbandingan tepat!

            // Jika waktu sasaran dah terlepas untuk hari ni, baru bawa ke hari esok
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
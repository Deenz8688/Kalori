package com.deenzstudios.kalori

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        // 🚀 1. BINA INTENT UNTUK BUKA APP BILA KLIK NOTIFIKASI
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            // Pasang flag supaya kalau app dah sedia terbuka, dia tak buka screen berlapis-lapis
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 🚀 2. BUNGKUS JADI PENDING INTENT (Wajib guna FLAG_IMMUTABLE untuk Android versi baru)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code biasa
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = intent.getStringExtra("title") ?: "Waktu Makan! 🍽️"
        val message = intent.getStringExtra("message") ?: "Jangan lupa rekod kalori awak hari ni."

        // 🚀 3. SELITKAN PENDING INTENT DI DALAM BUILDER (.setContentIntent)
        val builder = NotificationCompat.Builder(context, "waktu_makan_channel")
            .setSmallIcon(R.drawable.ic_noti_makan)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // 🔥 Panggil pendingIntent yang kita buat kat atas tadi!
            .setAutoCancel(true) // Bila klik, notifikasi automatik padam dari status bar

        val notificationManager = NotificationManagerCompat.from(context)

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uniqueId = System.currentTimeMillis().toInt()
            notificationManager.notify(uniqueId, builder.build())
        }
    }
}
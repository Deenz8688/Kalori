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

        val channelId = intent.getStringExtra(EXTRA_CHANNEL) ?: CHANNEL_MEAL
        val title = intent.getStringExtra("title") ?: "Waktu Makan! 🍽️"
        val message = intent.getStringExtra("message") ?: "Jangan lupa rekod kalori awak hari ni."
        val iconRes = intent.getIntExtra(EXTRA_ICON, R.drawable.ic_noti_makan)
            .takeIf { it != 0 } ?: R.drawable.ic_noti_makan

        // Amaran kalori guna priority tinggi supaya muncul sebagai heads-up
        val isWarning = channelId == CHANNEL_WARNING
        val priority =
            if (isWarning) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        // 🚀 3. SELITKAN PENDING INTENT DI DALAM BUILDER (.setContentIntent)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
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

    companion object {
        const val CHANNEL_MEAL = "waktu_makan_channel"
        const val CHANNEL_WARNING = "amaran_kalori_channel"

        private const val EXTRA_CHANNEL = "channel"
        private const val EXTRA_ICON = "icon"

        /**
         * Hantar notifikasi amaran apabila pengambilan kalori harian melebihi sasaran TDEE.
         * Dipanggil dari KaloriFragment selepas data hidangan dikemas kini.
         */
        fun sendOverTdeeWarning(context: Context, consumedKcal: Double, tdeeKcal: Double) {
            val over = (consumedKcal - tdeeKcal).coerceAtLeast(0.0)
            val message = (
                "Kalori anda hari ini sudah melebihi sasaran TDEE sebanyak %.0f kcal " +
                    "(%.0f / %.0f kcal). Cuba kurangkan hidangan seterusnya dan luangkan " +
                    "masa untuk bergerak! 💪"
                ).format(over, consumedKcal, tdeeKcal)

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(EXTRA_CHANNEL, CHANNEL_WARNING)
                putExtra("title", "⚠️ Amaran Kalori Berlebihan!")
                putExtra("message", message)
            }
            context.sendBroadcast(intent)
        }
    }
}

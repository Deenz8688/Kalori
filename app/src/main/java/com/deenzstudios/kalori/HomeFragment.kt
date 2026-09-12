package com.deenzstudios.kalori

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.deenzstudios.kalori.data.MealRepository
import com.deenzstudios.kalori.data.ProfileRepository
import com.deenzstudios.kalori.data.WaterRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    private lateinit var txtGreeting: TextView
    private lateinit var txtHomeName: TextView
    private lateinit var txtHomeDate: TextView
    private lateinit var calorieRing: CalorieRingView
    private lateinit var txtCalorieStatus: TextView
    private lateinit var txtMealSarapan: TextView
    private lateinit var txtMealTengahHari: TextView
    private lateinit var txtMealMakanMalam: TextView
    private lateinit var txtHomeWater: TextView
    private lateinit var txtHomeWaterPercent: TextView
    private lateinit var progressWater: ProgressBar

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        txtGreeting = view.findViewById(R.id.txtGreeting)
        txtHomeName = view.findViewById(R.id.txtHomeName)
        txtHomeDate = view.findViewById(R.id.txtHomeDate)
        calorieRing = view.findViewById(R.id.calorieRing)
        txtCalorieStatus = view.findViewById(R.id.txtCalorieStatus)
        txtMealSarapan = view.findViewById(R.id.txtMealSarapan)
        txtMealTengahHari = view.findViewById(R.id.txtMealTengahHari)
        txtMealMakanMalam = view.findViewById(R.id.txtMealMakanMalam)
        txtHomeWater = view.findViewById(R.id.txtHomeWater)
        txtHomeWaterPercent = view.findViewById(R.id.txtHomeWaterPercent)
        progressWater = view.findViewById(R.id.progressWater)

        val images = listOf(
            R.drawable.poster1,
            R.drawable.poster2
        )

        val adapter = SliderAdapter(images)
        viewPager.adapter = adapter

        autoSlide()

        // 🆕 Butang Tip Kurus
        val btnTipKurus = view.findViewById<TextView>(R.id.btnTipKurus)
        btnTipKurus.setOnClickListener {
            showTipsDialog()
        }

        setGreeting()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    /** Muatkan ringkasan dashboard (kalori, hidangan & air) untuk hari ini. */
    private fun loadData() {
        if (!isAdded) return
        val context = requireContext()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = dateFormat.format(Calendar.getInstance().time)
        txtHomeDate.text = "📅 $today"

        lifecycleScope.launch {
            val profile = ProfileRepository.getProfile(context)

            // Nama pengguna
            val name = profile?.name?.trim().orEmpty()
            txtHomeName.text = if (name.isEmpty()) "Sila lengkapkan profil anda" else name

            // Sasaran kalori (TDEE) — default 2000 kcal jika profil kosong
            val targetKcal = profile?.tdee
                ?.replace("kcal", "", ignoreCase = true)
                ?.trim()
                ?.toDoubleOrNull()
                ?: 0.0
            val effectiveTarget = if (targetKcal <= 0.0) 2000.0 else targetKcal

            // Pecahan hidangan hari ini
            val sarapan = MealRepository.totalFor(context, today, MealRepository.SARAPAN)
            val tengahHari = MealRepository.totalFor(context, today, MealRepository.TENGAH_HARI)
            val makanMalam = MealRepository.totalFor(context, today, MealRepository.MAKAN_MALAM)
            val total = sarapan + tengahHari + makanMalam

            txtMealSarapan.text = "%.0f kcal".format(sarapan)
            txtMealTengahHari.text = "%.0f kcal".format(tengahHari)
            txtMealMakanMalam.text = "%.0f kcal".format(makanMalam)

            calorieRing.setProgress(total, effectiveTarget)

            val baki = effectiveTarget - total
            txtCalorieStatus.text = if (baki >= 0) {
                "Baki: %.0f kcal".format(baki)
            } else {
                "Melebihi sasaran: %.0f kcal".format(-baki)
            }

            // Air minuman hari ini
            val waterConsumed = WaterRepository.getConsumed(context, today)
            val waterTarget = profile?.waterTargetMl ?: WaterRepository.DEFAULT_TARGET_ML
            val safeWaterTarget = if (waterTarget <= 0) WaterRepository.DEFAULT_TARGET_ML else waterTarget

            txtHomeWater.text =
                "%.2f L / %.2f L".format(waterConsumed / 1000f, safeWaterTarget / 1000f)

            val percent = ((waterConsumed * 100.0) / safeWaterTarget).toInt().coerceIn(0, 100)
            progressWater.progress = percent
            txtHomeWaterPercent.text = "$percent% daripada sasaran harian"
        }
    }

    /** Ucapan mengikut waktu semasa. */
    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        txtGreeting.text = when {
            hour < 12 -> "Selamat Pagi ☀️"
            hour < 15 -> "Selamat Tengah Hari 🌤️"
            hour < 19 -> "Selamat Petang 🌇"
            else -> "Selamat Malam 🌙"
        }
    }

    private fun autoSlide() {

        val runnable = object : Runnable {

            override fun run() {

                val nextItem = (viewPager.currentItem + 1) % 2

                viewPager.currentItem = nextItem

                handler.postDelayed(this, 3000)
            }
        }

        handler.postDelayed(runnable, 3000)
    }

    private fun showTipsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_tips_kurus, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialogView.findViewById<android.widget.Button>(R.id.btnCloseDialog)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

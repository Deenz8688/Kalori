package com.deenzstudios.kalori

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.deenzstudios.kalori.data.ProfileEntity
import com.deenzstudios.kalori.data.ProfileRepository
import com.deenzstudios.kalori.data.WaterRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Tab "Air" — penjejak air minuman harian (Room/SQLite, OFFLINE).
 */
class AirFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedDate: String = ""

    private lateinit var waterRing: WaterRingView
    private lateinit var txtWaterDate: TextView
    private lateinit var txtWaterTarget: TextView
    private lateinit var txtWaterInfo: TextView
    private lateinit var edtCustomWater: EditText

    private var targetMl: Int = WaterRepository.DEFAULT_TARGET_ML
    private var consumedMl: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_air, container, false)

        waterRing = view.findViewById(R.id.waterRing)
        txtWaterDate = view.findViewById(R.id.txtWaterDate)
        txtWaterTarget = view.findViewById(R.id.txtWaterTarget)
        txtWaterInfo = view.findViewById(R.id.txtWaterInfo)
        edtCustomWater = view.findViewById(R.id.edtCustomWater)

        selectedDate = dateFormat.format(Calendar.getInstance().time)
        txtWaterDate.text = selectedDate

        txtWaterDate.setOnClickListener { showDatePicker() }
        view.findViewById<Button>(R.id.btnAdd250).setOnClickListener { addWater(250) }
        view.findViewById<Button>(R.id.btnAdd500).setOnClickListener { addWater(500) }
        view.findViewById<Button>(R.id.btnAdd1000).setOnClickListener { addWater(1000) }
        view.findViewById<Button>(R.id.btnAddCustom).setOnClickListener { addCustomWater() }
        view.findViewById<Button>(R.id.btnResetWater).setOnClickListener { resetWater() }
        view.findViewById<Button>(R.id.btnSetWaterTarget).setOnClickListener { showTargetDialog() }

        loadData()

        // ================= AUTO RESET (pukul 12:00 tengah malam) ====================
        // Sama macam Kalori: setiap 60 saat semak masa. Bila masuk 00:00,
        // tukar ke tarikh hari baru (data air hari baru = 0 ml, jadi auto reset).
        fun refreshCurrentDate() {
            if (!isAdded) return
            val realTimeCalendar = Calendar.getInstance()
            val currentHour = realTimeCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = realTimeCalendar.get(Calendar.MINUTE)

            if (currentHour == 0 && currentMinute == 0) {
                val todayDate = dateFormat.format(realTimeCalendar.time)
                if (selectedDate != todayDate) {
                    selectedDate = todayDate
                    txtWaterDate.text = todayDate
                    loadData()
                }
            }
        }

        view.postDelayed(
            object : Runnable {
                override fun run() {
                    refreshCurrentDate()
                    view.postDelayed(this, 60000)
                }
            },
            60000
        )

        return view
    }

    private fun loadData() {
        lifecycleScope.launch {
            val ctx = requireContext().applicationContext
            targetMl = ProfileRepository.getProfile(ctx)?.waterTargetMl
                ?: WaterRepository.DEFAULT_TARGET_ML
            consumedMl = WaterRepository.getConsumed(ctx, selectedDate)
            render()
        }
    }

    private fun render() {
        waterRing.setProgress(consumedMl, targetMl)
        txtWaterTarget.text = "Sasaran: %.1f liter".format(targetMl / 1000f)

        val remain = (targetMl - consumedMl).coerceAtLeast(0)
        txtWaterInfo.text = if (consumedMl >= targetMl) {
            "Syabas! Sasaran air harian tercapai ✅"
        } else {
            "Baki: %.0f ml lagi".format(remain.toFloat())
        }
    }

    private fun addWater(ml: Int) {
        lifecycleScope.launch {
            val ctx = requireContext().applicationContext
            consumedMl = WaterRepository.addWater(ctx, selectedDate, ml)
            render()
        }
    }

    private fun addCustomWater() {
        val amount = edtCustomWater.text.toString().toIntOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Sila masukkan jumlah yang sah (ml)", Toast.LENGTH_SHORT).show()
            return
        }
        edtCustomWater.setText("")
        addWater(amount)
    }

    private fun resetWater() {
        lifecycleScope.launch {
            val ctx = requireContext().applicationContext
            WaterRepository.reset(ctx, selectedDate)
            consumedMl = 0
            render()
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        // Cuba parse tarikh terpilih semasa
        try {
            cal.time = dateFormat.parse(selectedDate) ?: cal.time
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                selectedDate = dateFormat.format(chosen.time)
                txtWaterDate.text = selectedDate
                loadData()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTargetDialog() {
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Contoh: 3000"
            setText(targetMl.toString())
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Sasaran Air Harian (ml)")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newTarget = input.text.toString().toIntOrNull()
                if (newTarget == null || newTarget <= 0) {
                    Toast.makeText(requireContext(), "Sila masukkan nilai yang sah", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val ctx = requireContext().applicationContext
                    val current = ProfileRepository.getProfile(ctx) ?: ProfileEntity()
                    ProfileRepository.saveProfile(ctx, current.copy(waterTargetMl = newTarget))
                    targetMl = newTarget
                    render()
                    Toast.makeText(requireContext(), "✅ Sasaran air dikemas kini", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

package com.deenzstudios.kalori

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Intent
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.StrictMode
import android.widget.LinearLayout
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.deenzstudios.kalori.data.FoodRepository
import com.deenzstudios.kalori.data.MealRecordEntity
import com.deenzstudios.kalori.data.MealRepository
import com.deenzstudios.kalori.data.ProfileRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KaloriFragment : Fragment() {

    private var currentMealTypeForCamera = ""
    private var onAiFoodResult: ((Food) -> Unit)? = null

    // 🚀 Launcher Universal (Kamera & Galeri)
    private val universalLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data

            if (uri != null) {
                // Kes 1: User pilih dari Galeri (Uri)
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let { prosesGambarAI(it) }
            } else {
                // Kes 2: User ambil gambar Kamera (Bitmap)
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                imageBitmap?.let { prosesGambarAI(it) }
            }
        }
    }

    private fun bukaKamera() {
        // 1. Sediakan Intent Galeri
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        // 2. Sediakan Intent Kamera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 3. Gabungkan dalam System Chooser (Native Bottom Sheet)
        val chooser = Intent.createChooser(galleryIntent, "Pilih Sumber Makanan")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        try {
            universalLauncher.launch(chooser)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuka kamera/galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prosesGambarAI(bitmap: Bitmap) {
        val loadingDialog = android.app.ProgressDialog(requireContext())
        loadingDialog.setMessage("AI sedang menganalisis makanan...")
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        lifecycleScope.launch {
            val food = DeepSeekHelper.analisisGambarMakananAI(bitmap)
            loadingDialog.dismiss()

            if (food != null) {
                tunjukkanDialogPengesahanAI(food)
            } else {
                Toast.makeText(requireContext(), "AI gagal mengenali makanan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tunjukkanDialogPengesahanAI(food: Food) {
        // Pecahkan info berat dan makro untuk paparan dialog sahaja
        val parts = food.serving.split("|")
        val beratSaja = parts[0].trim()
        val makroSaja = if (parts.size > 1) parts[1].trim() else ""

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Hasil Imbasan AI")
            .setMessage("Makanan: ${food.name}\nAnggaran: $beratSaja\nKalori: ${food.calories.toInt()} kcal\n\nInfo Nutrisi:\n$makroSaja\n\nMasukkan ke $currentMealTypeForCamera?")
            .setPositiveButton("Ya, Masukkan") { _, _ ->
                onAiFoodResult?.invoke(food)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * Hantar notifikasi amaran bila jumlah kalori hari ini melebihi sasaran TDEE.
     * Sekali sehari sahaja (elak spam) dan hanya untuk tarikh hari ini.
     */
    private fun maybeNotifyOverTdee(date: String, consumedKcal: Double, tdeeKcal: Double) {
        if (!isAdded || tdeeKcal <= 0.0 || consumedKcal <= tdeeKcal) return

        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Calendar.getInstance().time)
        if (date != today) return // Jangan amaran untuk rekod tarikh lama

        val prefs = requireContext().getSharedPreferences("NotiPrefs", Context.MODE_PRIVATE)
        if (prefs.getString("last_over_tdee_date", null) == date) return // Dah bagi amaran hari ni

        prefs.edit().putString("last_over_tdee_date", date).apply()
        NotificationReceiver.sendOverTdeeWarning(requireContext(), consumedKcal, tdeeKcal)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build()
        )

        val view = inflater.inflate(R.layout.fragment_kalori, container, false)

        // ================= HUBUNGKAN KOMPONEN DASHBOARD BAHARU (LANGKAH 1) =================
        val layoutWarningProfile = view.findViewById<LinearLayout>(R.id.layoutWarningProfile)
        val layoutUtamaKalori = view.findViewById<LinearLayout>(R.id.layoutUtamaKalori)
        val btnGoToProfile = view.findViewById<Button>(R.id.btnGoToProfile)

        // Hubungkan 3 Kad Waktu Makan Yang Boleh Diklik
        val cardSarapanClick = view.findViewById<androidx.cardview.widget.CardView>(R.id.cardSarapanClick)
        val cardTengahHariClick = view.findViewById<androidx.cardview.widget.CardView>(R.id.cardTengahHariClick)
        val cardMalamClick = view.findViewById<androidx.cardview.widget.CardView>(R.id.cardMalamClick)

        // Hubungkan Teks Paparan Menu & Kalori Di Dalam Setiap Kad
        val txtCardSarapanCalori = view.findViewById<TextView>(R.id.txtCardSarapanCalori)
        val txtCardSarapanMenu = view.findViewById<TextView>(R.id.txtCardSarapanMenu)

        val txtCardTengahHariCalori = view.findViewById<TextView>(R.id.txtCardTengahHariCalori)
        val txtCardTengahHariMenu = view.findViewById<TextView>(R.id.txtCardTengahHariMenu)

        val txtCardMalamCalori = view.findViewById<TextView>(R.id.txtCardMalamCalori)
        val txtCardMalamMenu = view.findViewById<TextView>(R.id.txtCardMalamMenu)

        // Hubungkan Komponen Ringkasan Harian Kecil Di Bahagian Bawah
        val edtDate = view.findViewById<EditText>(R.id.edtDate)
        val txtSummaryDate = view.findViewById<TextView>(R.id.txtSummaryDate)
        val txtTdee = view.findViewById<TextView>(R.id.txtTdee)
        val txtBmr = view.findViewById<TextView>(R.id.txtBmr)
        val txtTotalCalories = view.findViewById<TextView>(R.id.txtTotalCalories)
        val txtBalance = view.findViewById<TextView>(R.id.txtBalance)

        // ================= PROFIL (ROOM) =================
        // Pref lama kekal sebagai sumber migrasi sekali sahaja
        val legacyPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Nilai profil dimuat dari Room secara async (diisi dlm coroutine di bawah)
        var savedTdee = "0 kcal"
        var savedBmr = "0 kcal"
        var savedWeight = "0"

        // ================= FOOD LIST =================
        val foodList = mutableListOf<Food>()

        // ================= DATE CONTROL =================
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtDate.setText(dateFormat.format(calendar.time))

        // Pembolehubah kaunter total kalori harian mengikut fasa makan
        var breakfastTotal = 0.0
        var lunchTotal = 0.0
        var dinnerTotal = 0.0

        // ================= LOAD DATA DARI ROOM (SQLITE) =================
        val appContext = requireContext().applicationContext

        fun loadDataByDate(selectedDate: String) {
            txtSummaryDate.text = selectedDate

            lifecycleScope.launch {
                // Pindahkan rekod lama SharedPreferences -> Room (sekali sahaja)
                MealRepository.migrateFromPrefs(appContext, legacyPref)

                val records = MealRepository.getByDate(appContext, selectedDate)

                val breakfastRecords = records.filter { it.mealType == MealRepository.SARAPAN }
                val lunchRecords = records.filter { it.mealType == MealRepository.TENGAH_HARI }
                val dinnerRecords = records.filter { it.mealType == MealRepository.MAKAN_MALAM }

                val breakfastText = breakfastRecords.joinToString("\n") { it.foodName }
                val lunchText = lunchRecords.joinToString("\n") { it.foodName }
                val dinnerText = dinnerRecords.joinToString("\n") { it.foodName }

                breakfastTotal = breakfastRecords.sumOf { it.calories }
                lunchTotal = lunchRecords.sumOf { it.calories }
                dinnerTotal = dinnerRecords.sumOf { it.calories }

                // 3. MASUKKAN DATA KE KAD SARAPAN
                if (breakfastText.isNotEmpty()) {
                    txtCardSarapanMenu.text = breakfastText
                    txtCardSarapanCalori.text = "%.0f kcal".format(breakfastTotal)
                    txtCardSarapanCalori.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    txtCardSarapanMenu.text = "Belum ada hidangan ditambah."
                    txtCardSarapanCalori.text = "0 kcal"
                    txtCardSarapanCalori.setTextColor(Color.parseColor("#757575"))
                }

                // 4. MASUKKAN DATA KE KAD TENGAH HARI
                if (lunchText.isNotEmpty()) {
                    txtCardTengahHariMenu.text = lunchText
                    txtCardTengahHariCalori.text = "%.0f kcal".format(lunchTotal)
                    txtCardTengahHariCalori.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    txtCardTengahHariMenu.text = "Belum ada hidangan ditambah."
                    txtCardTengahHariCalori.text = "0 kcal"
                    txtCardTengahHariCalori.setTextColor(Color.parseColor("#757575"))
                }

                // 5. MASUKKAN DATA KE KAD MAKAN MALAM
                if (dinnerText.isNotEmpty()) {
                    txtCardMalamMenu.text = dinnerText
                    txtCardMalamCalori.text = "%.0f kcal".format(dinnerTotal)
                    txtCardMalamCalori.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    txtCardMalamMenu.text = "Belum ada hidangan ditambah."
                    txtCardMalamCalori.text = "0 kcal"
                    txtCardMalamCalori.setTextColor(Color.parseColor("#757575"))
                }

                // 6. Kemas kini Ringkasan Harian Kecil dlm Dashboard bawah
                val totalCalories = breakfastTotal + lunchTotal + dinnerTotal
                val tdeeValue = savedTdee.replace("kcal", "").trim().toDoubleOrNull() ?: 0.0
                val balance = tdeeValue - totalCalories

                txtTotalCalories.text = "%.0f kcal".format(totalCalories)
                txtBalance.text = "%.0f kcal".format(balance)

                // ⚠️ AMARAN: kalau kalori melebihi TDEE, hantar notifikasi (sekali sehari)
                maybeNotifyOverTdee(selectedDate, totalCalories, tdeeValue)
            }
        }

        // ================= SEGERAK LAPORAN HARIAN KE ROOM =================
        // Kira semula total hidangan sesuatu tarikh & simpan sebagai laporan harian.
        // Wajib dipanggil selepas setiap perubahan hidangan supaya menu Laporan terisi.
        suspend fun syncReportNow(date: String) {
            val breakfast = MealRepository.totalFor(appContext, date, MealRepository.SARAPAN)
            val lunch = MealRepository.totalFor(appContext, date, MealRepository.TENGAH_HARI)
            val dinner = MealRepository.totalFor(appContext, date, MealRepository.MAKAN_MALAM)
            val grandTotal = breakfast + lunch + dinner

            val todayDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
            val existing = ReportManager.getReports(requireContext()).find { it.date == date }
            // Berat profil semasa utk hari ini; kekalkan berat lama utk tarikh lepas
            val weight = if (date == todayDateStr) "$savedWeight kg" else existing?.weight ?: "$savedWeight kg"

            ReportManager.saveReport(
                requireContext(),
                ReportData(
                    date, weight,
                    "%.0f kcal".format(breakfast),
                    "%.0f kcal".format(lunch),
                    "%.0f kcal".format(dinner),
                    "%.0f kcal".format(grandTotal),
                    savedBmr, savedTdee
                )
            )
        }

        fun syncReportForDate(date: String) {
            lifecycleScope.launch { syncReportNow(date) }
        }

        // ================= LOGIK TAMBAHAN: SIMPAN MAKANAN DARI AI (ROOM) =================
        onAiFoodResult = { food ->
            val selectedDate = edtDate.text.toString()
            val mealKey = MealRepository.normalizeMealType(currentMealTypeForCamera)

            // 🔥 POTONG INFO MAKRO: Ambil berat saja supaya list tak "panjang2"
            val beratSaja = food.serving.split("|")[0].trim()
            val cleanName = food.name.replace("'", "")
            val itemText = "• $cleanName ($beratSaja) = %.0f kcal".format(food.calories)

            lifecycleScope.launch {
                MealRepository.add(appContext, selectedDate, mealKey, itemText, food.calories)
                syncReportNow(selectedDate)
                loadDataByDate(selectedDate)
                Toast.makeText(requireContext(), "Berjaya ditambah!", Toast.LENGTH_SHORT).show()
            }
        }

        // ================= HUBUNGKAN BUTANG KAMERA DASHBOARD =================
        view.findViewById<ImageView>(R.id.btnCameraSarapan).setOnClickListener {
            currentMealTypeForCamera = "🍳 Sarapan"
            bukaKamera()
        }
        view.findViewById<ImageView>(R.id.btnCameraTengahHari).setOnClickListener {
            currentMealTypeForCamera = "🍛 Tengah Hari"
            bukaKamera()
        }
        view.findViewById<ImageView>(R.id.btnCameraMalam).setOnClickListener {
            currentMealTypeForCamera = "🌙 Makan Malam"
            bukaKamera()
        }

        // ================= MUAT PROFIL DARI ROOM =================
        lifecycleScope.launch {
            ProfileRepository.migrateFromPrefs(appContext, legacyPref)
            val profile = ProfileRepository.getProfile(appContext)

            savedTdee = profile?.tdee?.ifEmpty { "0 kcal" } ?: "0 kcal"
            savedBmr = profile?.bmr?.ifEmpty { "0 kcal" } ?: "0 kcal"
            savedWeight = profile?.weight?.ifEmpty { "0" } ?: "0"

            // ================= SEKATAN WAJIB PROFIL =================
            if (savedTdee == "0 kcal" || savedBmr == "0 kcal" || savedTdee.isEmpty()) {
                layoutWarningProfile.visibility = View.VISIBLE
                layoutUtamaKalori.visibility = View.GONE
            } else {
                layoutWarningProfile.visibility = View.GONE
                layoutUtamaKalori.visibility = View.VISIBLE
            }

            // Set nilai teks tdee dngan bmr ke ringkasan bawah dashboard
            txtTdee.text = savedTdee
            txtBmr.text = savedBmr

            // Jalankan load data permulaan untuk tarikh hari ini (selepas profil siap)
            loadDataByDate(edtDate.text.toString())

            // Pulihkan laporan bagi semua tarikh yg ada rekod hidangan (cth. data dari Fasa 3)
            MealRepository.getAllDates(appContext).forEach { syncReportNow(it) }
        }

        // ================= DATE PICKER (LANGKAH 4) =================
        edtDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    edtDate.setText(dateFormat.format(calendar.time))
                    loadDataByDate(edtDate.text.toString())
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // ================= POPUP DIALOG CONTROL (LANGKAH 5 - BAHAGIAN A) =================
        fun showTambahMakananPopup(mealType: String) {
            // 1. Cipta AlertDialog dan letakkan layout popup_tambah_makanan ke dalamnya
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.popup_tambah_makanan, null)
            val builder = android.app.AlertDialog.Builder(requireContext()).setView(dialogView)
            val alertDialog = builder.create()

            // Buat background dialog jadi transparent supaya corner radius XML kita nampak bulat cantik
            alertDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

            // 2. Hubungkan komponen-komponen XML Popup ke dalam Kotlin
            val txtPopupTitle = dialogView.findViewById<TextView>(R.id.txtPopupTitle)
            val edtBreakfastFood =
                dialogView.findViewById<AutoCompleteTextView>(R.id.edtBreakfastFood)
            val radioGroupBreakfast = dialogView.findViewById<RadioGroup>(R.id.radioGroupBreakfast)
            val radioBreakfastGram = dialogView.findViewById<RadioButton>(R.id.radioBreakfastGram)
            val radioBreakfastServing =
                dialogView.findViewById<RadioButton>(R.id.radioBreakfastServing)
            val edtBreakfastAmount = dialogView.findViewById<EditText>(R.id.edtBreakfastAmount)
            val btnAddBreakfast = dialogView.findViewById<Button>(R.id.btnAddBreakfast)
            val layoutTempList = dialogView.findViewById<LinearLayout>(R.id.layoutTempList)
            val txtBreakfastCalories = dialogView.findViewById<TextView>(R.id.txtBreakfastCalories)
            val txtBreakfastTotal = dialogView.findViewById<TextView>(R.id.txtBreakfastTotal)
            val btnCancelPopup = dialogView.findViewById<Button>(R.id.btnCancelPopup)
            val btnSaveMeal = dialogView.findViewById<Button>(R.id.btnSaveMeal)
            val btnDeleteMeal = dialogView.findViewById<Button>(R.id.btnDeleteMeal)
            // 3. Set tajuk popup mengikut kad fasa makan yang diklik
            txtPopupTitle.text = "Tambah / Edit - $mealType"

            // Sediakan pembolehubah temp tempatan dlm popup
            val tempMealList = mutableListOf<String>()
            var tempTotal = 0.0

            // Set threshold untuk auto-complete cari makanan dlm popup
            edtBreakfastFood.threshold = 1

            // 4. LOGIK CARIAN AUTOMATIK DARI ROOM (SQLITE) — OFFLINE
            edtBreakfastFood.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchText = s.toString().trim()
                    if (searchText.isEmpty()) return
                    val ctx = context?.applicationContext ?: return
                    lifecycleScope.launch {
                        val hasil = FoodRepository.search(ctx, searchText)
                        // Abaikan hasil jika teks carian telah berubah
                        if (edtBreakfastFood.text.toString().trim() != searchText) return@launch

                        foodList.clear()
                        val foodNames = mutableListOf<String>()
                        hasil.forEach { entiti ->
                            val food = Food(
                                entiti.name,
                                entiti.serving,
                                entiti.gram,
                                entiti.calories,
                                entiti.unit
                            )
                            foodList.add(food)
                            foodNames.add(food.name)
                        }

                        if (!isAdded) return@launch
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            foodNames
                        )
                        edtBreakfastFood.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
                        edtBreakfastFood.showDropDown()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // 5. LOGIK AUTO SERVING BILA NAMA MAKANAN DIPILIH / TAIP
            edtBreakfastFood.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchText = s.toString().trim()
                    val foundFood = foodList.find { it.name.equals(searchText, ignoreCase = true) }
                    if (foundFood != null && radioBreakfastServing.isChecked) {
                        edtBreakfastAmount.setText(foundFood.serving)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            radioBreakfastGram.setOnClickListener {
                edtBreakfastAmount.setText("")
                edtBreakfastAmount.hint = "cth: 250g / 250ml"
                edtBreakfastAmount.inputType = InputType.TYPE_CLASS_TEXT  // 🔥 Tukar ke teks

            }

            radioBreakfastServing.setOnClickListener {
                edtBreakfastAmount.setText("")
                edtBreakfastAmount.hint = "Masukkan hidangan (cth: 2 mangkuk / 1 pinggan)"
                edtBreakfastAmount.inputType = InputType.TYPE_CLASS_TEXT

                // 🔥 AUTO-ISI HIDANGAN DARI DATABASE (JIKA ADA)
                val currentFoodName = edtBreakfastFood.text.toString().trim()
                if (currentFoodName.isNotEmpty()) {
                    val foundFood = foodList.find { it.name.equals(currentFoodName, ignoreCase = true) }
                    if (foundFood != null) {
                        edtBreakfastAmount.setText(foundFood.serving)
                    }
                }
            }
            // ================= 🔥 BUTANG TAMBAH MAKANAN (HYBRID: DATABASE → AI) =================
            btnAddBreakfast.setOnClickListener {
                val searchText = edtBreakfastFood.text.toString().trim()
                val amountText = edtBreakfastAmount.text.toString().trim()

                if (searchText.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Sila masukkan nama makanan",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (amountText.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Sila masukkan kuantiti (cth: 250g / 1 pinggan)",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // 🔥 Fungsi ekstrak nombor
                fun extractNumber(text: String): Double {
                    val regex = Regex("\\d+(\\.\\d+)?")
                    return regex.find(text)?.value?.toDoubleOrNull() ?: 1.0
                }

                // 🔍 LANGKAH 1: CUBA CARI DALAM DATABASE DULU (foodList)
                val foundInDb = foodList.find { it.name.equals(searchText, ignoreCase = true) }

                if (foundInDb != null) {
                    // ✅ JUMPA DALAM DATABASE → GUNA DATA DATABASE (CEPAT & PERCUMA)
                    val finalAmount = extractNumber(amountText)  // 🔥 Guna fungsi
                    var calories = 0.0

                    if (radioBreakfastGram.isChecked) {
                        // Gram mode: guna gram dari database
                        calories = (finalAmount / foundInDb.gram) * foundInDb.calories
                    } else {
                        // Hidangan mode: darab dengan hidangan
                        calories = foundInDb.calories * finalAmount
                    }

                    tempTotal += calories
                    val itemLayout = LinearLayout(requireContext())
                    itemLayout.orientation = LinearLayout.HORIZONTAL

                    val txtItem = TextView(requireContext())
                    txtItem.layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    // 🔥 Papar teks asal (termasuk unit)
                    val displayUnit = amountText
                    val itemText = "• ${foundInDb.name} ($displayUnit) = %.0f kcal".format(calories)

                    txtItem.text = itemText
                    txtItem.textSize = 14f
                    txtItem.setTextColor(Color.BLACK)

                    tempMealList.add(itemText)

                    val btnDeleteItem = Button(requireContext())
                    btnDeleteItem.text = "🗑️"
                    btnDeleteItem.textSize = 12f
                    btnDeleteItem.background = null
                    btnDeleteItem.setBackgroundColor(Color.TRANSPARENT)

                    val itemCalories = calories
                    btnDeleteItem.setOnClickListener {
                        tempTotal -= itemCalories
                        tempMealList.remove(itemText)
                        layoutTempList.removeView(itemLayout)
                        txtBreakfastTotal.text = "Jumlah Semasa: %.0f kcal".format(tempTotal)
                    }

                    itemLayout.addView(txtItem)
                    itemLayout.addView(btnDeleteItem)
                    layoutTempList.addView(itemLayout)

                    txtBreakfastCalories.text = "Kalori: %.0f kcal".format(calories)
                    txtBreakfastTotal.text = "Jumlah Semasa: %.0f kcal".format(tempTotal)

                    // Kosongkan ruangan
                    edtBreakfastFood.setText("")
                    edtBreakfastAmount.setText("")
                    edtBreakfastFood.requestFocus()

                } else {
                    // ❌ TAK JUMPA DALAM DATABASE → GUNA DEEPSEEK AI (FALLBACK)
                    val fullPromptQuery = "$searchText sebanyak $amountText"

                    txtBreakfastCalories.text = "⏳ AI sedang mengira..."

                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val foundFood = DeepSeekHelper.dapatkanKaloriDariAI(fullPromptQuery)

                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (foundFood != null) {
                                // 🔥 GUNA TERUS KALORI DARI AI (DAH KIRA UNTUK KESELURUHAN HIDANGAN)
                                val calories = foundFood.calories

                                tempTotal += calories
                                val itemLayout = LinearLayout(requireContext())
                                itemLayout.orientation = LinearLayout.HORIZONTAL

                                val txtItem = TextView(requireContext())
                                txtItem.layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )

                                val displayUnit = amountText
                                val itemText =
                                    "• ${foundFood.name} ($displayUnit) = %.0f kcal".format(calories)

                                txtItem.text = itemText
                                txtItem.textSize = 14f
                                txtItem.setTextColor(Color.BLACK)

                                tempMealList.add(itemText)

                                val btnDeleteItem = Button(requireContext())
                                btnDeleteItem.text = "🗑️"
                                btnDeleteItem.textSize = 12f
                                btnDeleteItem.background = null
                                btnDeleteItem.setBackgroundColor(Color.TRANSPARENT)

                                val itemCalories = calories
                                btnDeleteItem.setOnClickListener {
                                    tempTotal -= itemCalories
                                    tempMealList.remove(itemText)
                                    layoutTempList.removeView(itemLayout)
                                    txtBreakfastTotal.text =
                                        "Jumlah Semasa: %.0f kcal".format(tempTotal)
                                }

                                itemLayout.addView(txtItem)
                                itemLayout.addView(btnDeleteItem)
                                layoutTempList.addView(itemLayout)

                                txtBreakfastCalories.text = "Kalori: %.0f kcal".format(calories)
                                txtBreakfastTotal.text =
                                    "Jumlah Semasa: %.0f kcal".format(tempTotal)

                                edtBreakfastFood.setText("")
                                edtBreakfastAmount.setText("")
                                edtBreakfastFood.requestFocus()
                            } else {
                                txtBreakfastCalories.text = "❌ Gagal mendapatkan data kalori"
                                Toast.makeText(
                                    requireContext(),
                                    "Makanan tidak dikenali. Cuba taip lebih jelas.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            // 9. LOGIK BUTANG TUTUP / BATAL POPUP
            btnCancelPopup.setOnClickListener {
                alertDialog.dismiss() // Tutup popup tanpa save apa-apa
            }

            // ================= LOGIK BUTANG PADAM / KOSONGKAN HIDANGAN FASA INI =================
            btnDeleteMeal.setOnClickListener {
                val selectedDate = edtDate.text.toString()
                val mealKey = MealRepository.normalizeMealType(mealType)

                lifecycleScope.launch {
                    // 1. Padam semua rekod fasa ini dari Room
                    MealRepository.deleteMeal(appContext, selectedDate, mealKey)

                    // 2. Kira semula & kemas kini laporan harian
                    syncReportNow(selectedDate)

                    // 3. Segarkan dashboard utama dngan tutup dialog
                    loadDataByDate(selectedDate)
                    alertDialog.dismiss()
                    Toast.makeText(requireContext(), "$mealType berjaya dikosongkan!", Toast.LENGTH_SHORT).show()
                }
            }
            // 10. LOGIK BUTANG SIMPAN MENU KE DATABASE ROOM (SQLITE) — OFFLINE
            btnSaveMeal.setOnClickListener {
                val selectedDate = edtDate.text.toString()
                val mealKey = MealRepository.normalizeMealType(mealType)

                if (tempMealList.isEmpty()) {
                    Toast.makeText(requireContext(), "Tiada item untuk disimpan.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Tukar setiap teks paparan item ("• Nama (unit) = 123 kcal") jadi rekod Room
                val calRegex = Regex("=\\s*([0-9]+(?:\\.[0-9]+)?)\\s*kcal")
                val records = tempMealList.map { itemText ->
                    val calories = calRegex.find(itemText)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    MealRecordEntity(
                        date = selectedDate,
                        mealType = mealKey,
                        foodName = itemText,
                        calories = calories
                    )
                }

                lifecycleScope.launch {
                    MealRepository.addAll(appContext, records)
                    syncReportNow(selectedDate)
                    loadDataByDate(selectedDate)
                }

                alertDialog.dismiss() // Tutup popup selepas tamat menyimpan data dngan jaya
            }

            // 🚀 LANGKAH BONUS: PAPARKAN POPUP DIALOG KE SKRIN TELEFON
            alertDialog.show()
        } // Penutup rasmi fungsi gergasi showTambahMakananPopup

        // ================= KAWALAN KLIK KAD DASHBOARD (LANGKAH 6) =================

        // 1. Klik Kad Sarapan -> Buka Popup Sarapan
        cardSarapanClick.setOnClickListener {
            showTambahMakananPopup("🍳 Sarapan")
        }

        // 2. Klik Kad Tengah Hari -> Buka Popup Tengah Hari
        cardTengahHariClick.setOnClickListener {
            showTambahMakananPopup("🍛 Tengah Hari")
        }

        // 3. Klik Kad Malam -> Buka Popup Makan Malam
        cardMalamClick.setOnClickListener {
            showTambahMakananPopup("🌙 Makan Malam")
        }

        // ================= AUTO RESET ====================
        fun refreshCurrentDate() {
            if (!isAdded) return
            val realTimeCalendar = Calendar.getInstance()
            val currentHour = realTimeCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = realTimeCalendar.get(Calendar.MINUTE)

            if (currentHour == 0 && currentMinute == 0) {
                val todayDate = dateFormat.format(realTimeCalendar.time)
                if (edtDate.text.toString() != todayDate) {
                    edtDate.setText(todayDate)
                    loadDataByDate(todayDate)
                }
            }
        }

        // ================= AUTO REFRESH =================
        view.postDelayed(
            object : Runnable {
                override fun run() {
                    refreshCurrentDate() // ✅ Settle! Dah tak merah sebab fungsi kat atas dah wujud
                    view.postDelayed(this, 60000)
                }
            },
            60000
        )

        return view
    }
}
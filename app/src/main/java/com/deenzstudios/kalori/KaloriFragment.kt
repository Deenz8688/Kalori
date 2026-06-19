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
import java.net.URL
import java.net.URLEncoder // 🔥 Ditambah untuk fungsi bungkusan URL Encode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.StrictMode
import android.widget.LinearLayout
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KaloriFragment : Fragment() {

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

        // ================= VIEW PROFILE PREFERENCES =================
        val profilePref = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val currentTdee = profilePref.getString("tdee", "0 kcal") ?: "0 kcal"
        val currentBmr = profilePref.getString("bmr", "0 kcal") ?: "0 kcal"

        // ================= JALANKAN SEKATAN WAJIB PROFIL =================
        if (currentTdee == "0 kcal" || currentBmr == "0 kcal" || currentTdee.isEmpty()) {
            layoutWarningProfile.visibility = View.VISIBLE
            layoutUtamaKalori.visibility = View.GONE

            btnGoToProfile.setOnClickListener {
                val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
                bottomNav.selectedItemId = R.id.nav_Me
            }
        } else {
            layoutWarningProfile.visibility = View.GONE
            layoutUtamaKalori.visibility = View.VISIBLE
        }

        // ================= SHARED PREF (LANGKAH 2) =================
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val savedTdee = sharedPref.getString("tdee", "0 kcal") ?: "0 kcal"
        val savedBmr = sharedPref.getString("bmr", "0 kcal") ?: "0 kcal"

        // Set nilai teks tdee dngan bmr ke ringkasan bawah dashboard
        txtTdee.text = savedTdee
        txtBmr.text = savedBmr

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

        // ================= LOAD DATA LOCAL (LANGKAH 3) =================
        fun loadDataByDate(selectedDate: String) {
            txtSummaryDate.text = selectedDate

            // 1. Tarik data teks menu makanan yang pernah disave dlm tarikh ni
            val breakfastText = sharedPref.getString("${selectedDate}_breakfast_text", "")
            val lunchText = sharedPref.getString("${selectedDate}_lunch_text", "")
            val dinnerText = sharedPref.getString("${selectedDate}_dinner_text", "")

            // 2. Tarik total kalori lama dlm tarikh ni
            // 🔄 GANTI BAHAGIAN INI SAHAJA BIAR REKOD LUNCH KEMAS BALIK:
            breakfastTotal = sharedPref.getFloat("${selectedDate}_breakfast_total", 0f).toDouble()
            lunchTotal = sharedPref.getFloat("${selectedDate}_lunch_total", 0f).toDouble() // ✅ Dah bersih!
            dinnerTotal = sharedPref.getFloat("${selectedDate}_dinner_total", 0f).toDouble()

            // 3. MASUKKAN DATA KE KAD SARAPAN
            if (!breakfastText.isNullOrEmpty()) {
                txtCardSarapanMenu.text = breakfastText
                txtCardSarapanCalori.text = "%.0f kcal".format(breakfastTotal)
                txtCardSarapanCalori.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                txtCardSarapanMenu.text = "Belum ada hidangan ditambah."
                txtCardSarapanCalori.text = "0 kcal"
                txtCardSarapanCalori.setTextColor(Color.parseColor("#757575"))
            }

            // 4. MASUKKAN DATA KE KAD TENGAH HARI
            if (!lunchText.isNullOrEmpty()) {
                txtCardTengahHariMenu.text = lunchText
                txtCardTengahHariCalori.text = "%.0f kcal".format(lunchTotal)
                txtCardTengahHariCalori.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                txtCardTengahHariMenu.text = "Belum ada hidangan ditambah."
                txtCardTengahHariCalori.text = "0 kcal"
                txtCardTengahHariCalori.setTextColor(Color.parseColor("#757575"))
            }

            // 5. MASUKKAN DATA KE KAD MAKAN MALAM
            if (!dinnerText.isNullOrEmpty()) {
                txtCardMalamMenu.text = dinnerText
                txtCardMalamCalori.text = "%.0f kcal".format(dinnerTotal)
                txtCardMalamCalori.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                txtCardMalamMenu.text = "Belum ada hidangan ditambah."
                txtCardMalamCalori.text = "0 kcal"
                txtCardMalamCalori.setTextColor(Color.parseColor("#757575"))
            }

            // 6. Kemas kini Ringkasan Harian Kecil dlm Dashboard bawah
            val totalCalories = sharedPref.getFloat("${selectedDate}_totalCalories", 0f)
            val balance = sharedPref.getFloat("${selectedDate}_balance", 0f)

            txtTotalCalories.text = "%.0f kcal".format(totalCalories)
            txtBalance.text = "%.0f kcal".format(balance)
        }

        // Jalankan load data permulaan untuk tarikh hari ini
        loadDataByDate(edtDate.text.toString())

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
        // ================= CLOUD RESTORE DATABASE (MENGALIRKAN REKOD DARI MYSQL) =================
        fun loadCloudFoods(userId: String) {
            try {
                val url = URL("https://specmb.org/kalori_api/get_all_foods.php?user_id=$userId")
                val response = url.readText()
                val jsonArray = org.json.JSONArray(response)

                // Bersihkan data lama dlm SharedPreferences telefon dlu sebelum ganti dngan data cloud
                val allKeys = sharedPref.all.keys
                for (key in allKeys) {
                    if (key.contains("_breakfast") || key.contains("_lunch") || key.contains("_dinner") || key.contains("_totalCalories") || key.contains("_balance")) {
                        editor.remove(key)
                    }
                }
                editor.apply()

                val uniqueDates = mutableSetOf<String>()

                // 🚀 FASA 1: Longgokkan semua baris data makanan dari MySQL ke SharedPreferences telefon dlu
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val mealType = obj.getString("meal_type").trim()
                    val foodName = obj.getString("food_name")
                    val calories = obj.getString("calories").toFloat()
                    val rawDate = obj.getString("food_date")
                    val parts = rawDate.split("-")
                    val foodDate = "${parts[2]}/${parts[1]}/${parts[0]}"

                    uniqueDates.add(foodDate)

                    val formattedFoodName = foodName.split(",").joinToString("\n") { "• ${it.trim()}" }

                    val currentSavedBreakfastTotal = sharedPref.getFloat("${foodDate}_breakfast_total", 0f).toDouble()
                    val currentSavedLunchTotal = sharedPref.getFloat("${foodDate}_lunch_total", 0f).toDouble()
                    val currentSavedDinnerTotal = sharedPref.getFloat("${foodDate}_dinner_total", 0f).toDouble()

                    when (mealType) {
                        "Sarapan", "🍳 Sarapan" -> {
                            val newTotal = currentSavedBreakfastTotal + calories
                            val oldBreakfast = sharedPref.getString("${foodDate}_breakfast_text", "") ?: ""
                            val newBreakfast = if (oldBreakfast.isEmpty()) formattedFoodName else oldBreakfast + "\n" + formattedFoodName
                            editor.putString("${foodDate}_breakfast_text", newBreakfast)
                            editor.putFloat("${foodDate}_breakfast_total", newTotal.toFloat())
                        }
                        "Tengah Hari", "🍛 Tengah Hari" -> {
                            val newTotal = currentSavedLunchTotal + calories
                            val oldLunch = sharedPref.getString("${foodDate}_lunch_text", "") ?: ""
                            val newLunch = if (oldLunch.isEmpty()) formattedFoodName else oldLunch + "\n" + formattedFoodName
                            editor.putString("${foodDate}_lunch_text", newLunch)
                            editor.putFloat("${foodDate}_lunch_total", newTotal.toFloat())
                        }
                        "Makan Malam", "🌙 Makan Malam" -> {
                            val newTotal = currentSavedDinnerTotal + calories
                            val oldDinner = sharedPref.getString("${foodDate}_dinner_text", "") ?: ""
                            val newDinner = if (oldDinner.isEmpty()) formattedFoodName else oldDinner + "\n" + formattedFoodName
                            editor.putString("${foodDate}_dinner_text", newDinner)
                            editor.putFloat("${foodDate}_dinner_total", newTotal.toFloat())
                        }
                    }
                }
                editor.apply()

                // 🚀 FASA 2: Kirakan Grand Total & Kunci sejarah profil dlm ReportManager bagi setiap tarikh unik
                for (foodDate in uniqueDates) {
                    var cloudWeight = "0 kg"
                    var cloudBmr = "0 kcal"
                    var cloudTdee = "0 kcal"

                    for (k in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(k)
                        val rawDate = obj.getString("food_date")
                        val parts = rawDate.split("-")
                        val dateToCheck = "${parts[2]}/${parts[1]}/${parts[0]}"

                        if (dateToCheck == foodDate) {
                            cloudWeight = obj.optString("weight", "0 kg")
                            cloudBmr = obj.optString("bmr", "0 kcal")
                            cloudTdee = obj.optString("tdee", "0 kcal")
                            break
                        }
                    }

                    val finalBreakfast = sharedPref.getFloat("${foodDate}_breakfast_total", 0f).toDouble()
                    val finalLunch = sharedPref.getFloat("${foodDate}_lunch_total", 0f).toDouble()
                    val finalDinner = sharedPref.getFloat("${foodDate}_dinner_total", 0f).toDouble()

                    val grandTotal = finalBreakfast + finalLunch + finalDinner
                    val tdeeValue = cloudTdee.replace("kcal", "").trim().toDoubleOrNull() ?: 0.0
                    val balance = tdeeValue - grandTotal

                    editor.putFloat("${foodDate}_totalCalories", grandTotal.toFloat())
                    editor.putFloat("${foodDate}_balance", balance.toFloat())

                    val verifiedWeight = if (!cloudWeight.contains("kg") && cloudWeight != "NULL" && cloudWeight.isNotEmpty()) {
                        "$cloudWeight kg"
                    } else if (cloudWeight == "NULL" || cloudWeight.isEmpty()) {
                        "0 kg"
                    } else {
                        cloudWeight
                    }

                    val reportData = ReportData(
                        foodDate,
                        verifiedWeight,
                        "%.0f kcal".format(finalBreakfast),
                        "%.0f kcal".format(finalLunch),
                        "%.0f kcal".format(finalDinner),
                        "%.0f kcal".format(grandTotal),
                        cloudBmr,
                        cloudTdee
                    )
                    ReportManager.saveReport(requireContext(), reportData)
                }
                editor.apply()

                // Segarkan data dashboard mengikut tarikh kotak teks semasa dlm phone
                loadDataByDate(edtDate.text.toString())

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 🚀 PANGGIL KOD DATABASE BILA USER LOG MASUK
        val loginPref = requireActivity().getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
        val userId = loginPref.getString("userId", "") ?: ""
        if (userId.isNotEmpty()) {
            loadCloudFoods(userId)
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

            // 4. LOGIK CARIAN AUTOMATIK DARI DATABASE MYSQL (API SEARCH)
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
                    if (searchText.length >= 1) {
                        try {
                            val url = URL(
                                "https://specmb.org/kalori_api/search_food.php?q=${
                                    URLEncoder.encode(
                                        searchText,
                                        "UTF-8"
                                    )
                                }"
                            )
                            val response = url.readText()
                            val jsonArray = org.json.JSONArray(response)

                            foodList.clear()
                            val foodNames = mutableListOf<String>()

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val food = Food(
                                    obj.getString("Makanan"),
                                    obj.getString("Hidangan"),
                                    obj.getDouble("Berat"),
                                    obj.getDouble("Kalori"),
                                    obj.getString("Unit")
                                )
                                foodList.add(food)
                                foodNames.add(food.name)
                            }

                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                foodNames
                            )
                            edtBreakfastFood.setAdapter(adapter)
                            adapter.notifyDataSetChanged()
                            edtBreakfastFood.showDropDown()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                // 🔥 Tukar hint supaya user tahu mereka boleh taip sebutan hidangan secara manual
                edtBreakfastAmount.hint = "Masukkan hidangan (cth: 2 mangkuk / 1 pinggan)"
                // 🔥 Tukar jadi TYPE_CLASS_TEXT supaya kotak input boleh terima huruf/tulisan!
                edtBreakfastAmount.inputType = InputType.TYPE_CLASS_TEXT
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
                    // ❌ TAK JUMPA DALAM DATABASE → GUNA GEMINI AI (FALLBACK)
                    val fullPromptQuery = "$searchText sebanyak $amountText"

                    txtBreakfastCalories.text = "⏳ AI sedang mengira..."

                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val foundFood = GeminiHelper.dapatkanKaloriDariAI(fullPromptQuery)

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

                // 1. Bersihkan pembolehubah kaunter & SharedPreferences tempatan mengikut jenis fasa
                when (mealType) {
                    "Sarapan", "🍳 Sarapan" -> {
                        breakfastTotal = 0.0
                        editor.remove("${selectedDate}_breakfast_text")
                        editor.remove("${selectedDate}_breakfast_total")
                    }
                    "Tengah Hari", "🍛 Tengah Hari" -> {
                        lunchTotal = 0.0
                        editor.remove("${selectedDate}_lunch_text")
                        editor.remove("${selectedDate}_lunch_total")
                    }
                    "Makan Malam", "🌙 Makan Malam" -> {
                        dinnerTotal = 0.0
                        editor.remove("${selectedDate}_dinner_text")
                        editor.remove("${selectedDate}_dinner_total")
                    }
                }

                // 2. Kira semula baki grand total harian
                val grandTotal = breakfastTotal + lunchTotal + dinnerTotal
                val tdeeValue = savedTdee.replace("kcal", "").trim().toDoubleOrNull() ?: 0.0
                val balance = tdeeValue - grandTotal

                editor.putFloat("${selectedDate}_totalCalories", grandTotal.roundToInt().toFloat())
                editor.putFloat("${selectedDate}_balance", balance.toFloat())
                editor.apply()

                // 3. Hantar arahan padam terus ke server MySQL cloud api awak
                try {
                    val mealWithoutEmoji = when (mealType) {
                        "Sarapan", "🍳 Sarapan" -> "Sarapan"
                        "Tengah Hari", "🍛 Tengah Hari" -> "Tengah Hari"
                        else -> "Makan Malam"
                    }
                    val dateParts = selectedDate.split("/")
                    val mysqlDate = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
                    val url = URL("https://specmb.org/kalori_api/delete_food.php")
                    val connection = url.openConnection()
                    connection.doOutput = true

                    val loginPref2 = requireActivity().getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
                    val userId2 = loginPref2.getString("userId", "") ?: ""
                    val postData = "user_id=$userId2&meal_type=$mealWithoutEmoji&food_date=$mysqlDate"

                    connection.getOutputStream().write(postData.toByteArray())
                    val response = connection.getInputStream().bufferedReader().readText()
                    android.util.Log.d("DELETE_FOOD", "Respon Padam MySQL: $response")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 4. Kunci data dlm ReportManager sejarah kelmarin
                val todayDateStr2 = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                val existingReports2 = ReportManager.getReports(requireContext())
                val oldReportForThisDate2 = existingReports2.find { it.date == selectedDate }

                val currentWeight = if (selectedDate == todayDateStr2) {
                    val profilePref2 = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                    profilePref2.getString("weight", "0") + " kg"
                } else {
                    oldReportForThisDate2?.weight ?: (profilePref.getString("weight", "0") + " kg")
                }

                val reportData = ReportData(
                    selectedDate, currentWeight,
                    "%.0f kcal".format(breakfastTotal), "%.0f kcal".format(lunchTotal), "%.0f kcal".format(dinnerTotal),
                    "%.0f kcal".format(grandTotal), savedBmr, savedTdee
                )
                ReportManager.saveReport(requireContext(), reportData)

                // 5. Segarkan dashboard utama dngan tutup dialog
                loadDataByDate(selectedDate)
                alertDialog.dismiss()
                Toast.makeText(requireContext(), "$mealType berjaya dikosongkan!", Toast.LENGTH_SHORT).show()
            }
            // 10. LOGIK BUTANG SIMPAN MENU KE DATABASE MYSQL
            btnSaveMeal.setOnClickListener {
                val selectedDate = edtDate.text.toString()

                val oldText = when (mealType) {
                    "Sarapan", "🍳 Sarapan" -> sharedPref.getString("${selectedDate}_breakfast_text", "")
                    "Tengah Hari", "🍛 Tengah Hari" -> sharedPref.getString("${selectedDate}_lunch_text", "")
                    else -> sharedPref.getString("${selectedDate}_dinner_text", "")
                } ?: ""

                val combinedText = if (oldText.isNotEmpty()) oldText + "\n" + tempMealList.joinToString("\n") else tempMealList.joinToString("\n")

                val oldTotal = when (mealType) {
                    "Sarapan", "🍳 Sarapan" -> breakfastTotal
                    "Tengah Hari", "🍛 Tengah Hari" -> lunchTotal
                    else -> dinnerTotal
                }

                val newTotal = oldTotal + tempTotal

                // Kemas kini pembolehubah kaunter & SharedPreferences lokal mengikut jenis fasa makanan
                when (mealType) {
                    "Sarapan", "🍳 Sarapan" -> {
                        breakfastTotal = newTotal
                        editor.putString("${selectedDate}_breakfast_text", combinedText)
                        editor.putFloat("${selectedDate}_breakfast_total", newTotal.toFloat())
                    }
                    "Tengah Hari", "🍛 Tengah Hari" -> {
                        lunchTotal = newTotal
                        editor.putString("${selectedDate}_lunch_text", combinedText)
                        editor.putFloat("${selectedDate}_lunch_total", newTotal.toFloat())
                    }
                    "Makan Malam", "🌙 Makan Malam" -> {
                        dinnerTotal = newTotal
                        editor.putString("${selectedDate}_dinner_text", combinedText)
                        editor.putFloat("${selectedDate}_dinner_total", newTotal.toFloat())
                    }
                }

                val grandTotal = breakfastTotal + lunchTotal + dinnerTotal
                val tdeeValue = savedTdee.replace("kcal", "").trim().toDoubleOrNull() ?: 0.0
                val balance = tdeeValue - grandTotal

                editor.putFloat("${selectedDate}_totalCalories", grandTotal.toFloat())
                editor.putFloat("${selectedDate}_balance", balance.toFloat())
                editor.putString("${selectedDate}_tdee", savedTdee)
                editor.putString("${selectedDate}_bmr", savedBmr)
                editor.apply()

                // Hantar rekod ke pelayan MySQL cloud api dngan selamat
                try {
                    val loginPref2 = requireActivity().getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
                    val userId2 = loginPref2.getString("userId", "") ?: ""

                    val mealWithoutEmoji = when(mealType) {
                        "Sarapan", "🍳 Sarapan" -> "Sarapan"
                        "Tengah Hari", "🍛 Tengah Hari" -> "Tengah Hari"
                        else -> "Makan Malam"
                    }

                    val foodName = tempMealList.joinToString(", ")
                    val url = URL("https://specmb.org/kalori_api/save_food.php")
                    val connection = url.openConnection()
                    connection.doOutput = true

                    val dateParts = selectedDate.split("/")
                    val mysqlDate = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"

                    val currentWeight5 = sharedPref.getString("weight", "0") + " kg"
                    val currentBmr5 = sharedPref.getString("bmr", "0 kcal") ?: "0 kcal"
                    val currentTdee5 = sharedPref.getString("tdee", "0 kcal") ?: "0 kcal"

                    val encodedMeal = URLEncoder.encode(mealWithoutEmoji, "UTF-8")
                    val encodedFood = URLEncoder.encode(foodName, "UTF-8")
                    val encodedWeight = URLEncoder.encode(currentWeight5, "UTF-8")
                    val encodedBmr = URLEncoder.encode(currentBmr5, "UTF-8")
                    val encodedTdee = URLEncoder.encode(currentTdee5, "UTF-8")

                    val postData = "user_id=$userId2" +
                            "&meal_type=$encodedMeal" +
                            "&food_name=$encodedFood" +
                            "&calories=${tempTotal.toInt()}" +
                            "&food_date=$mysqlDate" +
                            "&weight=$encodedWeight" +
                            "&bmr=$encodedBmr" +
                            "&tdee=$encodedTdee"

                    connection.getOutputStream().write(postData.toByteArray())
                    val response = connection.getInputStream().bufferedReader().readText()
                    android.util.Log.d("SAVE_FOOD", "Respon MySQL: $response")

                    // Panggil semula logik penyelarasan cloud jika data berjaya masuk ke pelayan web
                    if (response.trim().contains("Food Saved") && userId2.isNotEmpty()) {
                        loadCloudFoods(userId2)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Segarkan paparan data pada 3 kad utama di dashboard phone
                loadDataByDate(selectedDate)
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
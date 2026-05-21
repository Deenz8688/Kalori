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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.StrictMode


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

        val view =
            inflater.inflate(
                R.layout.fragment_kalori,
                container,
                false
            )

        // ================= VIEW =================

        val edtDate =
            view.findViewById<EditText>(
                R.id.edtDate
            )

        val txtSummaryDate =
            view.findViewById<TextView>(
                R.id.txtSummaryDate
            )

        val txtTdee =
            view.findViewById<TextView>(
                R.id.txtTdee
            )

        val txtBmr =
            view.findViewById<TextView>(
                R.id.txtBmr
            )

        val txtTotalCalories =
            view.findViewById<TextView>(
                R.id.txtTotalCalories
            )

        val txtBalance =
            view.findViewById<TextView>(
                R.id.txtBalance
            )

        val edtBreakfastFood =
            view.findViewById<AutoCompleteTextView>(
                R.id.edtBreakfastFood
            )

        val edtBreakfastAmount =
            view.findViewById<AutoCompleteTextView>(
                R.id.edtBreakfastAmount
            )

        val btnAddBreakfast =
            view.findViewById<Button>(
                R.id.btnAddBreakfast
            )

        val btnSaveMeal =
            view.findViewById<Button>(
                R.id.btnSaveMeal
            )

        val txtBreakfastCalories =
            view.findViewById<TextView>(
                R.id.txtBreakfastCalories
            )

        val txtBreakfastTotal =
            view.findViewById<TextView>(
                R.id.txtBreakfastTotal
            )

        val spinnerMeal =
            view.findViewById<Spinner>(
                R.id.spinnerMeal
            )

        val layoutTempList =
            view.findViewById<LinearLayout>(
                R.id.layoutTempList
            )

        val layoutSavedMeals =
            view.findViewById<LinearLayout>(
                R.id.layoutSavedMeals
            )

        val radioBreakfastGram =
            view.findViewById<RadioButton>(
                R.id.radioBreakfastGram
            )

        val radioBreakfastServing =
            view.findViewById<RadioButton>(
                R.id.radioBreakfastServing
            )

        // ================= SHARED PREF =================

        val sharedPref =
            requireActivity().getSharedPreferences(
                "UserProfile",
                Context.MODE_PRIVATE
            )

        val editor =
            sharedPref.edit()

        val savedTdee =
            sharedPref.getString(
                "tdee",
                "0 kcal"
            ) ?: "0 kcal"

        val savedBmr =
            sharedPref.getString(
                "bmr",
                "0 kcal"
            ) ?: "0 kcal"

        txtTdee.text =
            "TDEE: $savedTdee"

        txtBmr.text =
            "BMR: $savedBmr"

        // ================= FOOD LIST =================

        val foodList =
            mutableListOf<Food>()

        try {

            val url =
                URL(
                    "https://docs.google.com/spreadsheets/d/1eQtvknyaQJNjwr8AIM5eRk7H_BA1D95gGx5fTM1Etmc/export?format=csv"
                )

            val inputStream =
                url.openStream()

            val reader =
                BufferedReader(
                    InputStreamReader(inputStream)
                )

            reader.readLine()

            var line: String?

            while (
                reader.readLine()
                    .also { line = it } != null
            ) {

                val row =
                    line!!.split(",")

                if (row.size >= 4) {

                    foodList.add(

                        Food(

                            row[0].trim(),

                            row[1].trim(),

                            row[2]
                                .trim()
                                .toDoubleOrNull()
                                ?: 0.0,

                            row[3]
                                .trim()
                                .toDoubleOrNull()
                                ?: 0.0
                        )
                    )
                }
            }

            reader.close()

        } catch (e: Exception) {

            e.printStackTrace()
        }

        // ================= AUTOCOMPLETE =================

        val foodNames =
            foodList.map { it.name }

        val foodAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                foodNames
            )

        edtBreakfastFood.setAdapter(
            foodAdapter
        )

        edtBreakfastFood.threshold = 1

        // ================= SPINNER =================

        val mealList = listOf(

            "🍳 Sarapan",
            "🍛 Tengah Hari",
            "🌙 Makan Malam"
        )

        spinnerMeal.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                mealList
            )

        // ================= DATE =================

        val calendar =
            Calendar.getInstance()

        val dateFormat =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

        edtDate.setText(
            dateFormat.format(calendar.time)
        )

        // ================= TEMP DATA =================

        val tempMealList =
            mutableListOf<String>()

        var tempTotal = 0.0

        var breakfastTotal = 0.0
        var lunchTotal = 0.0
        var dinnerTotal = 0.0

        var breakfastCard: LinearLayout? = null
        var lunchCard: LinearLayout? = null
        var dinnerCard: LinearLayout? = null

        // ================= CREATE CARD =================

        fun createCard(
            title: String,
            foods: String?,
            total: Double
        ): LinearLayout {

            val card =
                LinearLayout(requireContext())

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                40,
                40,
                40,
                40
            )

            card.setBackgroundColor(
                Color.WHITE
            )

            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            params.setMargins(
                0,
                0,
                0,
                30
            )

            card.layoutParams = params

            val txtMeal =
                TextView(requireContext())

            txtMeal.text = title

            txtMeal.textSize = 22f

            txtMeal.setTextColor(
                Color.BLACK
            )

            val txtFood =
                TextView(requireContext())

            txtFood.text = foods

            txtFood.textSize = 15f

            txtFood.setTextColor(
                Color.BLACK
            )

            val txtCalories =
                TextView(requireContext())

            txtCalories.text =
                "Jumlah Kalori: %.0f kcal"
                    .format(total)

            txtCalories.textSize = 18f

            txtCalories.setTextColor(
                Color.parseColor("#4CAF50")
            )

            val btnDelete =
                Button(requireContext())

            btnDelete.text =
                "❌ Buang"

            btnDelete.setBackgroundColor(
                Color.parseColor("#F44336")
            )

            btnDelete.setTextColor(
                Color.WHITE
            )

            btnDelete.setOnClickListener {

                val selectedDate =
                    edtDate.text.toString()

                layoutSavedMeals.removeView(card)

                when (title) {

                    "🍳 Sarapan" -> {

                        breakfastCard = null
                        breakfastTotal = 0.0

                        editor.remove(
                            "${selectedDate}_breakfast_text"
                        )

                        editor.remove(
                            "${selectedDate}_breakfast_total"
                        )
                    }

                    "🍛 Tengah Hari" -> {

                        lunchCard = null
                        lunchTotal = 0.0

                        editor.remove(
                            "${selectedDate}_lunch_text"
                        )

                        editor.remove(
                            "${selectedDate}_lunch_total"
                        )
                    }

                    "🌙 Makan Malam" -> {

                        dinnerCard = null
                        dinnerTotal = 0.0

                        editor.remove(
                            "${selectedDate}_dinner_text"
                        )

                        editor.remove(
                            "${selectedDate}_dinner_total"
                        )
                    }
                }

                val grandTotal =

                    breakfastTotal +
                            lunchTotal +
                            dinnerTotal

                val tdeeValue =
                    savedTdee
                        .replace(
                            "kcal",
                            ""
                        )
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0

                val balance =
                    tdeeValue - grandTotal

                editor.putFloat(
                    "${selectedDate}_totalCalories",
                    grandTotal.toFloat()
                )

                editor.putFloat(
                    "${selectedDate}_balance",
                    balance.toFloat()
                )

                editor.apply()

                txtTotalCalories.text =
                    "Jumlah Kalori: %.0f kcal"
                        .format(grandTotal)

                txtBalance.text =
                    "Baki Kalori: %.0f kcal"
                        .format(balance)
            }

            card.addView(txtMeal)
            card.addView(txtFood)
            card.addView(txtCalories)
            card.addView(btnDelete)

            return card
        }

        // ================= LOAD DATE =================

        fun loadDataByDate(
            selectedDate: String
        ) {

            txtSummaryDate.text =
                selectedDate

            layoutSavedMeals.removeAllViews()

            breakfastCard = null
            lunchCard = null
            dinnerCard = null

            val breakfastText =
                sharedPref.getString(
                    "${selectedDate}_breakfast_text",
                    ""
                )

            val lunchText =
                sharedPref.getString(
                    "${selectedDate}_lunch_text",
                    ""
                )

            val dinnerText =
                sharedPref.getString(
                    "${selectedDate}_dinner_text",
                    ""
                )

            breakfastTotal =
                sharedPref.getFloat(
                    "${selectedDate}_breakfast_total",
                    0f
                ).toDouble()

            lunchTotal =
                sharedPref.getFloat(
                    "${selectedDate}_lunch_total",
                    0f
                ).toDouble()

            dinnerTotal =
                sharedPref.getFloat(
                    "${selectedDate}_dinner_total",
                    0f
                ).toDouble()

            if (!breakfastText.isNullOrEmpty()) {

                val card =
                    createCard(
                        "🍳 Sarapan",
                        breakfastText,
                        breakfastTotal
                    )

                layoutSavedMeals.addView(card)

                breakfastCard = card
            }

            if (!lunchText.isNullOrEmpty()) {

                val card =
                    createCard(
                        "🍛 Tengah Hari",
                        lunchText,
                        lunchTotal
                    )

                layoutSavedMeals.addView(card)

                lunchCard = card
            }

            if (!dinnerText.isNullOrEmpty()) {

                val card =
                    createCard(
                        "🌙 Makan Malam",
                        dinnerText,
                        dinnerTotal
                    )

                layoutSavedMeals.addView(card)

                dinnerCard = card
            }

            val totalCalories =
                sharedPref.getFloat(
                    "${selectedDate}_totalCalories",
                    0f
                )

            val balance =
                sharedPref.getFloat(
                    "${selectedDate}_balance",
                    0f
                )

            txtTotalCalories.text =
                "Jumlah Kalori: %.0f kcal"
                    .format(totalCalories)

            txtBalance.text =
                "Baki Kalori: %.0f kcal"
                    .format(balance)
        }

        loadDataByDate(
            edtDate.text.toString()
        )

        // ================= DATE PICKER =================

        edtDate.setOnClickListener {

            DatePickerDialog(

                requireContext(),

                { _, year, month, dayOfMonth ->

                    calendar.set(
                        year,
                        month,
                        dayOfMonth
                    )

                    edtDate.setText(
                        dateFormat.format(calendar.time)
                    )

                    loadDataByDate(
                        edtDate.text.toString()
                    )
                },

                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)

            ).show()
        }

        // ================= AUTO RESET DAY =================

        fun refreshCurrentDate() {

            val todayDate =
                dateFormat.format(
                    Calendar.getInstance().time
                )

            if (
                edtDate.text.toString()
                != todayDate
            ) {

                edtDate.setText(todayDate)

                loadDataByDate(todayDate)
            }
        }

        // ================= SPINNER =================

        spinnerMeal.onItemSelectedListener =

            object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    edtBreakfastFood.setText("")
                    edtBreakfastAmount.setText("")

                    layoutTempList.removeAllViews()

                    tempMealList.clear()

                    tempTotal = 0.0

                    txtBreakfastCalories.text =
                        "Kalori: 0 kcal"

                    txtBreakfastTotal.text =
                        "Jumlah Semasa: 0 kcal"
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        // ================= AUTO SERVING =================

        edtBreakfastFood.addTextChangedListener(

            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val searchText =
                        s.toString().trim()

                    val foundFood =
                        foodList.find {

                            it.name.equals(
                                searchText,
                                ignoreCase = true
                            )
                        }

                    if (
                        foundFood != null &&
                        radioBreakfastServing.isChecked
                    ) {

                        edtBreakfastAmount.setText(
                            foundFood.serving
                        )
                    }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        // ================= RADIO =================

        radioBreakfastGram.setOnClickListener {

            edtBreakfastAmount.setText("")

            edtBreakfastAmount.hint =
                "Masukkan gram"

            edtBreakfastAmount.inputType =

                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        radioBreakfastServing.setOnClickListener {

            val searchText =
                edtBreakfastFood.text
                    .toString()
                    .trim()

            val foundFood =
                foodList.find {

                    it.name.equals(
                        searchText,
                        ignoreCase = true
                    )
                }

            if (foundFood != null) {

                val servingAdapter =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        listOf(foundFood.serving)
                    )

                edtBreakfastAmount.setAdapter(
                    servingAdapter
                )

                edtBreakfastAmount.setText(
                    foundFood.serving
                )

                edtBreakfastAmount.inputType = 0
            }
        }

        // ================= ADD FOOD =================

        btnAddBreakfast.setOnClickListener {

            val searchText =
                edtBreakfastFood.text
                    .toString()
                    .trim()

            var amount = 0.0

            val foundFood =
                foodList.find {

                    it.name.equals(
                        searchText,
                        ignoreCase = true
                    )
                }

            if (foundFood != null) {

                var calories = 0.0

                if (radioBreakfastGram.isChecked()) {

                    amount =
                        edtBreakfastAmount.text
                            .toString()
                            .toDoubleOrNull()
                            ?: 0.0

                    calories =
                        (amount / foundFood.gram) *
                                foundFood.calories

                } else {

                    amount = 1.0

                    calories =
                        foundFood.calories
                }

                tempTotal += calories

                val itemLayout =
                    LinearLayout(requireContext())

                itemLayout.orientation =
                    LinearLayout.HORIZONTAL

                val txtItem =
                    TextView(requireContext())

                txtItem.layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                val unit =

                    if (radioBreakfastGram.isChecked) {

                        "${amount}g"

                    } else {

                        foundFood.serving
                    }

                val itemText =

                    "• ${foundFood.name}" +
                            " ($unit)" +
                            " = %.0f kcal"
                                .format(calories)

                txtItem.text =
                    itemText

                txtItem.textSize =
                    14f

                txtItem.setTextColor(
                    Color.BLACK
                )

                tempMealList.add(itemText)

                val btnDelete =
                    Button(requireContext())

                btnDelete.text = "🗑️"

                btnDelete.textSize = 12f

                btnDelete.background = null

                btnDelete.setBackgroundColor(
                    Color.TRANSPARENT
                )

                val itemCalories =
                    calories

                btnDelete.setOnClickListener {

                    tempTotal -= itemCalories

                    tempMealList.remove(itemText)

                    layoutTempList.removeView(
                        itemLayout
                    )

                    txtBreakfastTotal.text =
                        "Jumlah Semasa: %.0f kcal"
                            .format(tempTotal)
                }

                itemLayout.addView(txtItem)

                itemLayout.addView(btnDelete)

                layoutTempList.addView(itemLayout)

                txtBreakfastCalories.text =
                    "Kalori: %.0f kcal"
                        .format(calories)

                txtBreakfastTotal.text =
                    "Jumlah Semasa: %.0f kcal"
                        .format(tempTotal)

                edtBreakfastFood.setText("")

                edtBreakfastAmount.setText("")

                edtBreakfastFood.requestFocus()
            }
        }

        // ================= SAVE =================

        btnSaveMeal.setOnClickListener {

            val selectedMeal =
                spinnerMeal.selectedItem.toString()

            val selectedDate =
                edtDate.text.toString()

            val oldText =

                when (selectedMeal) {

                    "🍳 Sarapan" ->

                        sharedPref.getString(
                            "${selectedDate}_breakfast_text",
                            ""
                        )

                    "🍛 Tengah Hari" ->

                        sharedPref.getString(
                            "${selectedDate}_lunch_text",
                            ""
                        )

                    else ->

                        sharedPref.getString(
                            "${selectedDate}_dinner_text",
                            ""
                        )
                } ?: ""

            val combinedText =

                if (oldText.isNotEmpty()) {

                    oldText + "\n" +
                            tempMealList.joinToString("\n")

                } else {

                    tempMealList.joinToString("\n")
                }

            val oldTotal =

                when (selectedMeal) {

                    "🍳 Sarapan" -> breakfastTotal

                    "🍛 Tengah Hari" -> lunchTotal

                    else -> dinnerTotal
                }

            val newTotal =
                oldTotal + tempTotal

            val card =
                createCard(
                    selectedMeal,
                    combinedText,
                    newTotal
                )

            when (selectedMeal) {

                "🍳 Sarapan" -> {

                    breakfastCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    breakfastCard = card

                    breakfastTotal = newTotal

                    editor.putString(
                        "${selectedDate}_breakfast_text",
                        combinedText
                    )

                    editor.putFloat(
                        "${selectedDate}_breakfast_total",
                        newTotal.toFloat()
                    )
                }

                "🍛 Tengah Hari" -> {

                    lunchCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    lunchCard = card

                    lunchTotal = newTotal

                    editor.putString(
                        "${selectedDate}_lunch_text",
                        combinedText
                    )

                    editor.putFloat(
                        "${selectedDate}_lunch_total",
                        newTotal.toFloat()
                    )
                }

                "🌙 Makan Malam" -> {

                    dinnerCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    dinnerCard = card

                    dinnerTotal = newTotal

                    editor.putString(
                        "${selectedDate}_dinner_text",
                        combinedText
                    )

                    editor.putFloat(
                        "${selectedDate}_dinner_total",
                        newTotal.toFloat()
                    )
                }
            }

            val grandTotal =

                breakfastTotal +
                        lunchTotal +
                        dinnerTotal

            val tdeeValue =
                savedTdee
                    .replace(
                        "kcal",
                        ""
                    )
                    .trim()
                    .toDoubleOrNull()
                    ?: 0.0

            val balance =
                tdeeValue - grandTotal

            editor.putFloat(
                "${selectedDate}_totalCalories",
                grandTotal.toFloat()
            )

            editor.putFloat(
                "${selectedDate}_balance",
                balance.toFloat()
            )

            editor.putString(
                "${selectedDate}_tdee",
                savedTdee
            )

            editor.putString(
                "${selectedDate}_bmr",
                savedBmr
            )

            editor.apply()

            txtTotalCalories.text =
                "Jumlah Kalori: %.0f kcal"
                    .format(grandTotal)

            txtBalance.text =
                "Baki Kalori: %.0f kcal"
                    .format(balance)

            layoutSavedMeals.addView(card)

            layoutTempList.removeAllViews()

            tempMealList.clear()

            tempTotal = 0.0

            txtBreakfastCalories.text =
                "Kalori: 0 kcal"

            txtBreakfastTotal.text =
                "Jumlah Semasa: 0 kcal"

            edtBreakfastFood.setText("")

            edtBreakfastAmount.setText("")

            edtBreakfastFood.requestFocus()
        }

        // ================= AUTO REFRESH =================

        view.postDelayed(

            object : Runnable {

                override fun run() {

                    refreshCurrentDate()

                    view.postDelayed(
                        this,
                        60000
                    )
                }
            },

            60000
        )

        return view
    }
}
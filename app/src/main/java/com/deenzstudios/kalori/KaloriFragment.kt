package com.deenzstudios.kalori

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

class KaloriFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(
                R.layout.fragment_kalori,
                container,
                false
            )

        // ================= VIEW =================

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
            )

        val savedBmr =
            sharedPref.getString(
                "bmr",
                "0 kcal"
            )

        txtTdee.text =
            "TDEE: $savedTdee"

        txtBmr.text =
            "BMR: $savedBmr"

        // ================= FOOD LIST =================

        val foodList =
            mutableListOf<Food>()

        try {

            val inputStream =
                requireContext()
                    .assets
                    .open("bank_kalori_makanan.csv")

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

                    val food =
                        Food(

                            name =
                                row[0].trim(),

                            serving =
                                row[1].trim(),

                            gram =
                                row[2]
                                    .trim()
                                    .toDoubleOrNull()
                                    ?: 0.0,

                            calories =
                                row[3]
                                    .trim()
                                    .toDoubleOrNull()
                                    ?: 0.0
                        )

                    foodList.add(food)
                }
            }

            reader.close()

        } catch (e: Exception) {

            e.printStackTrace()
        }

        // ================= AUTO COMPLETE =================

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

        val mealAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                mealList
            )

        spinnerMeal.adapter =
            mealAdapter

        // ================= DATA =================

        val tempMealList =
            mutableListOf<String>()

        var tempTotal = 0.0

        var breakfastTotal = 0.0

        var lunchTotal = 0.0

        var dinnerTotal = 0.0

        // ================= CARD =================

        var breakfastCard: LinearLayout? = null

        var lunchCard: LinearLayout? = null

        var dinnerCard: LinearLayout? = null

        // ================= LOAD DATA =================

        breakfastTotal =
            sharedPref.getFloat(
                "breakfast_total",
                0f
            ).toDouble()

        lunchTotal =
            sharedPref.getFloat(
                "lunch_total",
                0f
            ).toDouble()

        dinnerTotal =
            sharedPref.getFloat(
                "dinner_total",
                0f
            ).toDouble()

        val savedBreakfast =
            sharedPref.getString(
                "breakfast_text",
                ""
            )

        val savedLunch =
            sharedPref.getString(
                "lunch_text",
                ""
            )

        val savedDinner =
            sharedPref.getString(
                "dinner_text",
                ""
            )

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

            // ================= TITLE =================

            val txtMeal =
                TextView(requireContext())

            txtMeal.text = title

            txtMeal.textSize = 22f

            txtMeal.setTextColor(
                Color.BLACK
            )

            // ================= FOOD =================

            val txtFood =
                TextView(requireContext())

            txtFood.text = foods

            txtFood.textSize = 18f

            txtFood.setTextColor(
                Color.BLACK
            )

            // ================= CALORIES =================

            val txtCalories =
                TextView(requireContext())

            txtCalories.text =
                "Jumlah Kalori: %.0f kcal"
                    .format(total)

            txtCalories.textSize = 18f

            txtCalories.setTextColor(
                Color.parseColor("#4CAF50")
            )

            // ================= DELETE =================

            val btnDelete =
                Button(requireContext())

            btnDelete.text = "❌ Buang"

            btnDelete.setBackgroundColor(
                Color.parseColor("#F44336")
            )

            btnDelete.setTextColor(
                Color.WHITE
            )

            btnDelete.setOnClickListener {

                layoutSavedMeals.removeView(card)

                when (title) {

                    "🍳 Sarapan" -> {

                        breakfastCard = null

                        breakfastTotal = 0.0

                        editor.remove(
                            "breakfast_text"
                        )

                        editor.remove(
                            "breakfast_total"
                        )
                    }

                    "🍛 Tengah Hari" -> {

                        lunchCard = null

                        lunchTotal = 0.0

                        editor.remove(
                            "lunch_text"
                        )

                        editor.remove(
                            "lunch_total"
                        )
                    }

                    "🌙 Makan Malam" -> {

                        dinnerCard = null

                        dinnerTotal = 0.0

                        editor.remove(
                            "dinner_text"
                        )

                        editor.remove(
                            "dinner_total"
                        )
                    }
                }

                editor.apply()

                val grandTotal =

                    breakfastTotal +
                            lunchTotal +
                            dinnerTotal

                txtTotalCalories.text =
                    "Jumlah Kalori: %.0f kcal"
                        .format(grandTotal)

                val tdeeValue =
                    savedTdee
                        ?.replace(
                            "kcal",
                            ""
                        )
                        ?.trim()
                        ?.toDoubleOrNull()
                        ?: 0.0

                val balance =
                    tdeeValue - grandTotal

                txtBalance.text =
                    "Baki Kalori: %.0f kcal"
                        .format(balance)
            }

            // ================= ADD VIEW =================

            card.addView(txtMeal)

            card.addView(txtFood)

            card.addView(txtCalories)

            card.addView(btnDelete)

            return card
        }

        // ================= RESTORE CARD =================

        if (!savedBreakfast.isNullOrEmpty()) {

            val card =
                createCard(

                    "🍳 Sarapan",

                    savedBreakfast,

                    breakfastTotal
                )

            layoutSavedMeals.addView(card)

            breakfastCard = card
        }

        if (!savedLunch.isNullOrEmpty()) {

            val card =
                createCard(

                    "🍛 Tengah Hari",

                    savedLunch,

                    lunchTotal
                )

            layoutSavedMeals.addView(card)

            lunchCard = card
        }

        if (!savedDinner.isNullOrEmpty()) {

            val card =
                createCard(

                    "🌙 Makan Malam",

                    savedDinner,

                    dinnerTotal
                )

            layoutSavedMeals.addView(card)

            dinnerCard = card
        }

        // ================= STARTUP TOTAL =================

        val startupGrandTotal =

            breakfastTotal +
                    lunchTotal +
                    dinnerTotal

        txtTotalCalories.text =
            "Jumlah Kalori: %.0f kcal"
                .format(startupGrandTotal)

        val startupTdee =
            savedTdee
                ?.replace(
                    "kcal",
                    ""
                )
                ?.trim()
                ?.toDoubleOrNull()
                ?: 0.0

        val startupBalance =
            startupTdee - startupGrandTotal

        txtBalance.text =
            "Baki Kalori: %.0f kcal"
                .format(startupBalance)

        // ================= SPINNER CHANGE =================

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

        // ================= RADIO GRAM =================

        radioBreakfastGram.setOnClickListener {

            edtBreakfastAmount.setText("")

            edtBreakfastAmount.hint =
                "Masukkan gram"

            edtBreakfastAmount.inputType =

                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        // ================= RADIO SERVING =================

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

                val servingList =
                    listOf(foundFood.serving)

                val servingAdapter =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        servingList
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

                if (radioBreakfastGram.isChecked) {

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
                        amount *
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

                txtItem.text = itemText

                txtItem.textSize = 18f

                txtItem.setTextColor(
                    Color.BLACK
                )

                tempMealList.add(itemText)

                val btnDelete =
                    Button(requireContext())

                btnDelete.text = "🗑️"

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
            }
        }

        // ================= SAVE MENU =================

        btnSaveMeal.setOnClickListener {

            val selectedMeal =
                spinnerMeal.selectedItem.toString()

            val card =
                createCard(

                    selectedMeal,

                    tempMealList.joinToString("\n"),

                    tempTotal
                )

            when (selectedMeal) {

                "🍳 Sarapan" -> {

                    breakfastCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    breakfastCard = card

                    breakfastTotal = tempTotal

                    editor.putString(
                        "breakfast_text",
                        tempMealList.joinToString("\n")
                    )

                    editor.putFloat(
                        "breakfast_total",
                        tempTotal.toFloat()
                    )
                }

                "🍛 Tengah Hari" -> {

                    lunchCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    lunchCard = card

                    lunchTotal = tempTotal

                    editor.putString(
                        "lunch_text",
                        tempMealList.joinToString("\n")
                    )

                    editor.putFloat(
                        "lunch_total",
                        tempTotal.toFloat()
                    )
                }

                "🌙 Makan Malam" -> {

                    dinnerCard?.let {

                        layoutSavedMeals.removeView(it)
                    }

                    dinnerCard = card

                    dinnerTotal = tempTotal

                    editor.putString(
                        "dinner_text",
                        tempMealList.joinToString("\n")
                    )

                    editor.putFloat(
                        "dinner_total",
                        tempTotal.toFloat()
                    )
                }
            }

            editor.apply()

            val grandTotal =

                breakfastTotal +
                        lunchTotal +
                        dinnerTotal

            txtTotalCalories.text =
                "Jumlah Kalori: %.0f kcal"
                    .format(grandTotal)

            val tdeeValue =
                savedTdee
                    ?.replace(
                        "kcal",
                        ""
                    )
                    ?.trim()
                    ?.toDoubleOrNull()
                    ?: 0.0

            val balance =
                tdeeValue - grandTotal

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
        }

        return view
    }
}
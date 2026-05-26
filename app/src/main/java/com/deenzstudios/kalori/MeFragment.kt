package com.deenzstudios.kalori

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import android.net.Uri


class MeFragment : Fragment() {

    // 1. Isytihar pemboleh ubah di atas sekali dalam kelas
    private lateinit var pickImageLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private var imageUriString: String? = null

    // 2. Wajib daftarkan launcher di dalam onCreate!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // 1. Simpan string URI ke variable lokal awak
                imageUriString = it.toString()

                try {
                    // 2. 🔥 UBAT UTAMA: Minta kebenaran kekal dari OS Android guna 'it' (iaitu URI gambar)
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    requireContext().contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 3. Ambil view gambar dari fragment untuk dipaparkan terus pada borang
                val imgFormProfile = view?.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgFormProfile)

                // 4. Setkan gambar pada komponen UI borang guna 'it'
                imgFormProfile?.setImageURI(it)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_me, container, false)

        // 3. Cari ID komponen gambar profil dalam onCreateView
        val imgFormProfile = view.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgFormProfile)
        val imgProfileView = view.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgProfileView)

        // 4. Aksi apabila pengguna KLIK pada gambar profil bulat di borang
        imgFormProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // --- ID Komponen Asal ---
        val edtWeight = view.findViewById<EditText>(R.id.edtWeight)
        val edtName = view.findViewById<EditText>(R.id.edtName)
        val edtHeight = view.findViewById<EditText>(R.id.edtHeight)
        val edtAge = view.findViewById<EditText>(R.id.edtAge)
        val spinnerActivity = view.findViewById<Spinner>(R.id.spinnerActivity)

        val radioMale = view.findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = view.findViewById<RadioButton>(R.id.radioFemale)

        val btnCalculate = view.findViewById<Button>(R.id.btnCalculate)

        val profileLayout = view.findViewById<LinearLayout>(R.id.profileLayout)
        val formLayout = view.findViewById<LinearLayout>(R.id.formLayout)
        val txtProfileName = view.findViewById<TextView>(R.id.txtProfileName)
        val txtProfileWeight = view.findViewById<TextView>(R.id.txtProfileWeight)
        val txtProfileHeight = view.findViewById<TextView>(R.id.txtProfileHeight)
        val txtProfileAge = view.findViewById<TextView>(R.id.txtProfileAge)
        val txtProfileGender = view.findViewById<TextView>(R.id.txtProfileGender)
        val txtProfileActivity = view.findViewById<TextView>(R.id.txtProfileActivity)
        val txtProfileBMI = view.findViewById<TextView>(R.id.txtProfileBMI)
        val txtProfileBMR = view.findViewById<TextView>(R.id.txtProfileBMR)
        val txtProfileTDEE = view.findViewById<TextView>(R.id.txtProfileTDEE)

        val btnEdit = view.findViewById<Button>(R.id.btnEdit)

        val txtAccountStatus =
            view.findViewById<TextView>(
                R.id.txtAccountStatus
            )

        val btnLoginAccount =
            view.findViewById<Button>(
                R.id.btnLoginAccount
            )

        

        val btnLogout =
            view.findViewById<Button>(
                R.id.btnLogout
            )

        val btnQuickLogin =
            view.findViewById<Button>(
                R.id.btnQuickLogin
            )

        val sharedPref = requireActivity().getSharedPreferences(
            "UserProfile",
            android.content.Context.MODE_PRIVATE
        )

        val activityLevels = arrayOf(
            "Tidak Aktif",
            "Ringan",
            "Sederhana",
            "Aktif",
            "Sangat Aktif"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            activityLevels
        )

        spinnerActivity.adapter = adapter

        val loginPref =
            requireActivity().getSharedPreferences(
                "LoginSession",
                android.content.Context.MODE_PRIVATE
            )

        val isLoggedIn =
            loginPref.getBoolean(
                "isLoggedIn",
                false
            )

        val userEmail =
            loginPref.getString(
                "userEmail",
                ""
            )

        if (isLoggedIn) {

            txtAccountStatus.text =
                "Log Masuk Sebagai:\n$userEmail"

            btnLoginAccount.visibility =
                View.GONE



            btnLogout.visibility =
                View.VISIBLE

        } else {

            txtAccountStatus.text =
                "Anda menggunakan mod tetamu"

            btnLoginAccount.visibility =
                View.VISIBLE

            

            btnLogout.visibility =
                View.GONE
        }

        // ================= AUTO LOAD DATA TEKS (Apabila Fragment Dibuka) =================
        val savedName = sharedPref.getString("name", null)

        if (savedName != null) {
            profileLayout.visibility = View.VISIBLE
            formLayout.visibility = View.GONE

            // Memaparkan nama terus tanpa perkataan "Nama: "
            txtProfileName.text = sharedPref.getString("name", "")?.uppercase()

            val savedBmi = sharedPref.getString("bmi", "")
            val savedStatus = sharedPref.getString("bmiStatus", "")
            val savedColor = sharedPref.getInt("bmiColor", android.graphics.Color.BLACK)

            txtProfileBMI.text = "BMI: $savedBmi ($savedStatus)"
            txtProfileBMI.setTextColor(savedColor)
            txtProfileBMR.text = "BMR: " + sharedPref.getString("bmr", "")
            txtProfileTDEE.text = "TDEE: " + sharedPref.getString("tdee", "")
            txtProfileWeight.text = "Berat: " + sharedPref.getString("weight", "") + " kg"
            txtProfileHeight.text = "Tinggi: " + sharedPref.getString("height", "") + " cm"
            txtProfileAge.text = "Umur: " + sharedPref.getString("age", "") + " Tahun"
            txtProfileGender.text = "Jantina: " + sharedPref.getString("gender", "")
            txtProfileActivity.text = "Aktiviti: " + sharedPref.getString("activity", "")
        }

        // ================= AKSI BUTANG CALCULATE / SAVE PROFILE =================
        btnCalculate.setOnClickListener {
            val editor = sharedPref.edit()

            // 1. Simpan string lokasi gambar ke SharedPreferences
            if (imageUriString != null) {
                editor.putString("profile_image", imageUriString)
            }

            val weight = edtWeight.text.toString().toDoubleOrNull()
            val height = edtHeight.text.toString().toDoubleOrNull()
            val age = edtAge.text.toString().toIntOrNull()
            val name = edtName.text.toString()

            if (name.isNotEmpty() && weight != null && height != null && age != null) {

                // BMI
                val heightMeter = height / 100
                val bmi = weight / (heightMeter * heightMeter)

                // BMR ikut jantina
                val bmr = if (radioMale.isChecked) {
                    88.36 + (13.4 * weight) + (4.8 * height) - (5.7 * age)
                } else if (radioFemale.isChecked) {
                    447.6 + (9.2 * weight) + (3.1 * height) - (4.3 * age)
                } else {
                    0.0
                }

                if (bmr > 0) {
                    val multiplier = when (spinnerActivity.selectedItem.toString()) {
                        "Tidak Aktif" -> 1.2
                        "Ringan" -> 1.375
                        "Sederhana" -> 1.55
                        "Aktif" -> 1.725
                        else -> 1.9
                    }

                    val tdee = bmr * multiplier

                    val gender =
                        if (radioMale.isChecked)
                            "Lelaki"
                        else
                            "Perempuan"

                    Toast.makeText(requireContext(), "Profile berjaya disimpan", Toast.LENGTH_SHORT).show()


                    val loginPref =
                        requireActivity().getSharedPreferences(
                            "LoginSession",
                            android.content.Context.MODE_PRIVATE
                        )

                    val isLoggedIn =
                        loginPref.getBoolean(
                            "isLoggedIn",
                            false
                        )

                    if (isLoggedIn) {

                        val userId =
                            loginPref.getString(
                                "userId",
                                ""
                            ) ?: ""

                        Thread {

                            try {

                                val url =
                                    java.net.URL(
                                        "https://specmb.org/kalori_api/save_profile.php"
                                    )

                                val postData =
                                    "user_id=" +
                                            java.net.URLEncoder.encode(userId, "UTF-8") +

                                            "&full_name=" +
                                            java.net.URLEncoder.encode(name, "UTF-8") +

                                            "&profile_image=" +
                                            java.net.URLEncoder.encode(imageUriString ?: "", "UTF-8") +

                                            "&activity_level=" +
                                            java.net.URLEncoder.encode(
                                                spinnerActivity.selectedItem.toString(),
                                                "UTF-8"
                                            ) +

                                            "&gender=" +
                                            java.net.URLEncoder.encode(gender, "UTF-8") +

                                            "&age=" +
                                            java.net.URLEncoder.encode(age.toString(), "UTF-8") +

                                            "&weight=" +
                                            java.net.URLEncoder.encode(weight.toString(), "UTF-8") +

                                            "&height=" +
                                            java.net.URLEncoder.encode(height.toString(), "UTF-8") +

                                            "&bmi=" +
                                            java.net.URLEncoder.encode(
                                                "%.2f".format(bmi),
                                                "UTF-8"
                                            ) +

                                            "&bmr=" +
                                            java.net.URLEncoder.encode(
                                                "%.0f".format(bmr),
                                                "UTF-8"
                                            ) +

                                            "&tdee=" +
                                            java.net.URLEncoder.encode(
                                                "%.0f".format(tdee),
                                                "UTF-8"
                                            )

                                val conn =
                                    url.openConnection()
                                            as java.net.HttpURLConnection

                                conn.requestMethod = "POST"

                                conn.doOutput = true

                                conn.outputStream.write(
                                    postData.toByteArray()
                                )

                                conn.inputStream.bufferedReader()
                                    .readText()

                            } catch (e: Exception) {

                                e.printStackTrace()
                            }

                        }.start()
                    }
                    
                    profileLayout.visibility = View.VISIBLE
                    formLayout.visibility = View.GONE

                    // 2. Papar nama terus dalam huruf besar sejurus selepas save
                    txtProfileName.text = name.uppercase()

                    

                    txtProfileWeight.text = "Berat: $weight kg"
                    txtProfileHeight.text = "Tinggi: $height cm"
                    txtProfileAge.text = "Umur: $age Tahun"
                    txtProfileGender.text = "Jantina: $gender"
                    txtProfileActivity.text = "Aktiviti: ${spinnerActivity.selectedItem}"

                    val bmiStatus: String
                    val bmiColor: Int

                    when {
                        bmi < 18.5 -> {
                            bmiStatus = "KURUS"
                            bmiColor = android.graphics.Color.RED
                        }
                        bmi < 25 -> {
                            bmiStatus = "NORMAL"
                            bmiColor = android.graphics.Color.parseColor("#4CAF50")
                        }
                        bmi < 30 -> {
                            bmiStatus = "BERLEBIHAN"
                            bmiColor = android.graphics.Color.parseColor("#F0583A")
                        }
                        bmi < 35 -> {
                            bmiStatus = "OBESITI 1"
                            bmiColor = android.graphics.Color.parseColor("#E60E0E")
                        }
                        else -> {
                            bmiStatus = "OBESITI 2"
                            bmiColor = android.graphics.Color.parseColor("#AB0F0F")
                        }
                    }

                    txtProfileBMI.text = "BMI: %.2f".format(bmi) + " ($bmiStatus)"
                    txtProfileBMI.setTextColor(bmiColor)
                    txtProfileBMR.text = "BMR: %.0f kcal".format(bmr)
                    txtProfileTDEE.text = "TDEE: %.0f kcal".format(tdee)

                    // 3. Kemas kini imej pada paparan profile secara real-time
                    if (imageUriString != null) {
                        imgProfileView.setImageURI(Uri.parse(imageUriString))
                    }

                    // Simpan semua data ke SharedPreferences secara kekal
                    editor.putString("name", name)
                        .putString("bmi", "%.2f".format(bmi))
                        .putString("bmiStatus", bmiStatus)
                        .putInt("bmiColor", bmiColor)
                        .putString("bmr", "%.0f kcal".format(bmr))
                        .putString("tdee", "%.0f kcal".format(tdee))
                        .putString("weight", weight.toString())
                        .putString("height", height.toString())
                        .putString("age", age.toString())
                        .putString("gender", gender)
                        .putString("activity", spinnerActivity.selectedItem.toString())
                        .apply()
                }
            } else {
                Toast.makeText(requireContext(), "Sila lengkapkan semua maklumat", Toast.LENGTH_SHORT).show()
            }
        }

        // ================= AUTO LOAD DATA GAMBAR (Apabila Fragment Dibuka) =================
        val savedImageUriString = sharedPref.getString("profile_image", null)

        if (savedImageUriString != null) {
            val imageUri = Uri.parse(savedImageUriString)

            try {
                // 🟢 Cuba paparkan pada kedua-dua tempat (Borang dan Kad Paparan)
                imgFormProfile.setImageURI(imageUri)
                imgProfileView.setImageURI(imageUri)
            } catch (e: SecurityException) {
                // 🔴 Kalau Android sekat kebenaran akses fail lama, dia masuk sini (App TIDAK AKAN crash!)
                // Sistem akan gantikan dengan gambar robot hijau standard sementara
                imgFormProfile.setImageResource(R.drawable.ic_launcher_foreground)
                imgProfileView.setImageResource(R.drawable.ic_launcher_foreground)
                e.printStackTrace()
            }
        }

        // ================= AKSI BUTANG EDIT PROFILE =================
        btnEdit.setOnClickListener {
            profileLayout.visibility = View.GONE
            formLayout.visibility = View.VISIBLE

            edtName.setText(sharedPref.getString("name", ""))
            edtWeight.setText(sharedPref.getString("weight", ""))
            edtHeight.setText(sharedPref.getString("height", ""))
            edtAge.setText(sharedPref.getString("age", ""))

            val savedGender = sharedPref.getString("gender", "")

            if (savedGender == "Lelaki") {
                radioMale.isChecked = true
            } else {
                radioFemale.isChecked = true
            }

            val savedActivity = sharedPref.getString("activity", "Tidak Aktif")
            val position = activityLevels.indexOf(savedActivity)
            spinnerActivity.setSelection(position)
        }
        btnLoginAccount.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    LoginActivity::class.java
                )
            )
        }

        

        btnLogout.setOnClickListener {

            loginPref.edit().clear().apply()

            Toast.makeText(
                requireContext(),
                "Berjaya Log Out",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(
                    requireContext(),
                    LoginActivity::class.java
                )
            )

            requireActivity().finish()
        }

        btnQuickLogin.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    LoginActivity::class.java
                )
            )
        }
        
        return view
    }
}
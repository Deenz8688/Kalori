package com.deenzstudios.kalori

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.net.Uri
import com.deenzstudios.kalori.data.BackupManager
import com.deenzstudios.kalori.data.ProfileEntity
import com.deenzstudios.kalori.data.ProfileRepository
import com.deenzstudios.kalori.data.WaterRepository
import kotlinx.coroutines.launch


class MeFragment : Fragment() {



    // 1. Isytihar pemboleh ubah di atas sekali dalam kelas
    private lateinit var pickImageLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private var imageUriString: String? = null

    // Launcher backup / pulih data (fail JSON melalui Storage Access Framework)
    private lateinit var createBackupLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private lateinit var openBackupLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    private var onDataImported: (() -> Unit)? = null

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

        // ================= BACKUP: JANA FAIL JSON (SAF) =================
        createBackupLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                try {
                    val json = BackupManager.exportJson(requireContext().applicationContext)
                    requireContext().contentResolver.openOutputStream(uri)?.use {
                        it.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(requireContext(), "✅ Backup berjaya disimpan!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "❌ Gagal backup: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // ================= PULIH DATA: BACA FAIL JSON (SAF) =================
        openBackupLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                try {
                    val json = requireContext().contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: return@launch
                    BackupManager.importJson(requireContext().applicationContext, json)
                    onDataImported?.invoke()
                    Toast.makeText(requireContext(), "✅ Data berjaya dipulihkan!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "❌ Gagal pulihkan: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

        // Pref lama kekal sebagai sumber migrasi sekali sahaja
        val legacyPref = requireActivity().getSharedPreferences(
            "UserProfile",
            Context.MODE_PRIVATE
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

        // Profil semasa (dikongsi antara lambda)
        var currentProfile: ProfileEntity? = null

        fun renderProfile(profile: ProfileEntity) {
            profileLayout.visibility = View.VISIBLE
            formLayout.visibility = View.GONE

            txtProfileName.text = profile.name.uppercase()
            txtProfileBMI.text = "BMI: ${profile.bmi} (${profile.bmiStatus})"
            txtProfileBMI.setTextColor(profile.bmiColor)
            txtProfileBMR.text = "BMR: ${profile.bmr}"
            txtProfileTDEE.text = "TDEE: ${profile.tdee}"
            txtProfileWeight.text = "Berat: ${profile.weight} kg"
            txtProfileHeight.text = "Tinggi: ${profile.height} cm"
            txtProfileAge.text = "Umur: ${profile.age} Tahun"
            txtProfileGender.text = "Jantina: ${profile.gender}"
            txtProfileActivity.text = "Aktiviti: ${profile.activity}"
        }

        fun renderImage(uriString: String?) {
            if (uriString == null) return
            val imageUri = Uri.parse(uriString)
            try {
                // 🟢 Cuba paparkan pada kedua-dua tempat (Borang dan Kad Paparan)
                imgFormProfile.setImageURI(imageUri)
                imgProfileView.setImageURI(imageUri)
            } catch (e: SecurityException) {
                // 🔴 Kalau Android sekat kebenaran akses fail lama, dia masuk sini (App TIDAK AKAN crash!)
                imgFormProfile.setImageResource(R.drawable.ic_launcher_foreground)
                imgProfileView.setImageResource(R.drawable.ic_launcher_foreground)
                e.printStackTrace()
            }
        }

        // ================= AUTO LOAD DATA DARI ROOM (Apabila Fragment Dibuka) =================
        fun loadProfileFromRoom() {
            lifecycleScope.launch {
                val appCtx = requireContext().applicationContext
                ProfileRepository.migrateFromPrefs(appCtx, legacyPref)

                val profile = ProfileRepository.getProfile(appCtx)
                if (profile != null && profile.name.isNotEmpty()) {
                    currentProfile = profile
                    renderProfile(profile)
                    renderImage(profile.profileImage ?: legacyPref.getString("profile_image", null))
                } else {
                    profileLayout.visibility = View.GONE
                    formLayout.visibility = View.VISIBLE
                    renderImage(legacyPref.getString("profile_image", null))
                }
            }
        }

        // Dipanggil semula selepas data dipulihkan supaya UI dikemas kini
        onDataImported = { loadProfileFromRoom() }
        loadProfileFromRoom()

        // ================= AKSI BUTANG CALCULATE / SAVE PROFILE =================
        btnCalculate.setOnClickListener {
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

                    Toast.makeText(
                        requireContext(),
                        "Profile berjaya disimpan",
                        Toast.LENGTH_SHORT
                    ).show()

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

                    val profile = ProfileEntity(
                        name = name,
                        weight = weight.toString(),
                        height = height.toString(),
                        age = age.toString(),
                        gender = gender,
                        activity = spinnerActivity.selectedItem.toString(),
                        bmi = "%.2f".format(bmi),
                        bmiStatus = bmiStatus,
                        bmiColor = bmiColor,
                        bmr = "%.0f kcal".format(bmr),
                        tdee = "%.0f kcal".format(tdee),
                        // Kekalkan sasaran air yang telah diset di tab Air (jangan reset ke default)
                        waterTargetMl = currentProfile?.waterTargetMl
                            ?: WaterRepository.DEFAULT_TARGET_ML,
                        profileImage = imageUriString ?: currentProfile?.profileImage
                    )
                    currentProfile = profile

                    // Papar serta-merta pada kad profil
                    renderProfile(profile)

                    // Kemas kini imej pada paparan profile secara real-time
                    if (imageUriString != null) {
                        imgProfileView.setImageURI(Uri.parse(imageUriString))
                    }

                    // Simpan ke Room secara kekal
                    lifecycleScope.launch {
                        ProfileRepository.saveProfile(requireContext().applicationContext, profile)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Sila lengkapkan semua maklumat", Toast.LENGTH_SHORT).show()
            }
        }

        // ================= AKSI BUTANG EDIT PROFILE =================
        btnEdit.setOnClickListener {
            lifecycleScope.launch {
                val profile = ProfileRepository.getProfile(requireContext().applicationContext) ?: return@launch

                profileLayout.visibility = View.GONE
                formLayout.visibility = View.VISIBLE

                edtName.setText(profile.name)
                edtWeight.setText(profile.weight)
                edtHeight.setText(profile.height)
                edtAge.setText(profile.age)

                if (profile.gender == "Lelaki") {
                    radioMale.isChecked = true
                } else {
                    radioFemale.isChecked = true
                }

                val position = activityLevels.indexOf(profile.activity)
                spinnerActivity.setSelection(if (position >= 0) position else 0)
            }
        }

        // ================= AKSI BUTANG BACKUP & PULIH DATA =================
        view.findViewById<Button>(R.id.btnBackup).setOnClickListener {
            val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                .format(java.util.Date())
            createBackupLauncher.launch("kalori_backup_$stamp.json")
        }

        view.findViewById<Button>(R.id.btnRestore).setOnClickListener {
            openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }

        return view
    }

}

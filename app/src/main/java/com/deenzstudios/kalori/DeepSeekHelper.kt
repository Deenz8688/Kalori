package com.deenzstudios.kalori

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * DeepSeek API helper (OpenAI-compatible Chat Completions).
 * Dua fungsi: teks kalori + analisis imej makanan (Vision).
 * API key diambil dari local.properties -> BuildConfig.API_KEY
 */
object DeepSeekHelper {

    private val apiKey: String = BuildConfig.API_KEY

    private const val CHAT_URL = "https://api.deepseek.com/chat/completions"
    private const val MODEL = "deepseek-flash" // deepseek-flash sahaja yang sokong Vision
    private const val TIMEOUT_MS = 60_000

    // ================= TEKS: KIRA KALORI =================
    suspend fun dapatkanKaloriDariAI(inputMenuUser: String): Food? = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = "Anda pakar nutrisi Malaysia. Jawab HANYA dengan JSON yang sah, tiada ayat lain. Semua nilai mesti realistik."

            val userPrompt = """
            Kira kalori untuk: "$inputMenuUser"

            Panduan WAJIB:
            1. Tentukan kuantiti/berat sebenar yang diminta (contoh: 40g).
            2. Anggar kalori per 100g ikut data makanan Malaysia. Contoh rujukan: sayur goreng ~150-250 kcal/100g, bukan 350+ kcal/100g.
            3. Kira kalori untuk KUANTITI YANG DIMINTA SAHAJA. Jangan pulangkan kalori untuk 100g atau hidangan penuh (contoh: 40g = kalori bagi 40g).
            4. Jangan overestimate. Untuk gorengan, anggar serapan minyak secara realistik.
            5. Masukkan makro nutrisi dalam "serving" selepas tanda "|".

            Output HANYA json ini:
            {"name":"Nama Makanan","serving":"kuantiti | PROTEIN: 0g, KARBOHIDRAT: 0g, LEMAK: 0g","gram":0.0,"calories":0.0}
            """.trimIndent()

            val messages = JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", userPrompt))
            }

            val body = binaBody(messages, maxTokens = 512)
            val rawResponse = hantarPermintaan(body) ?: return@withContext null
            val rawAiText = ambilContent(rawResponse)?.takeIf { it.isNotEmpty() }
                ?: return@withContext null

            val foodJson = JSONObject(bersihkanJson(rawAiText))

            return@withContext Food(
                name = foodJson.optString("name", "Makanan"),
                serving = foodJson.optString("serving", inputMenuUser),
                gram = foodJson.optDouble("gram", 100.0),
                calories = foodJson.optDouble("calories", 0.0),
                unit = "g"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // ================= IMEJ: ANALISIS GAMBAR MAKANAN =================
    suspend fun analisisGambarMakananAI(bitmap: Bitmap): Food? = withContext(Dispatchers.IO) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val encodedImage = Base64.encodeToString(
                byteArrayOutputStream.toByteArray(),
                Base64.NO_WRAP
            )
            val dataUrl = "data:image/jpeg;base64,$encodedImage"

            val systemPrompt = "Anda pakar masakan & nutrisi (Malaysia, Asia & antarabangsa) yang sangat teliti. " +
                "Jawab HANYA dengan JSON yang sah, tiada ayat lain."

            val prompt = """
            Kenal pasti hidangan dalam gambar ini DENGAN TEPAT.

            LANGKAH 1 — TENTUKAN JENIS MASAKAN dahulu:
            - Jika ia masakan MALAYSIA / ASIA TENGGARA (kuah, sambal, santan, nasi, mi, lauk tempatan) => guna PANDUAN A.
            - Jika ia masakan BARAT / ANTARABANGSA (burger, pizza, pasta, sandwich, salad, steak, ayam goreng, kentang, sushi, dsb.) => guna PANDUAN B.
            - Jika tidak pasti, kenal pasti ikut apa yang PALING KELIHATAN dan guna nama standard paling dikenali.

            PANDUAN A — MASAKAN MALAYSIA/ASIA (buat dalam kepala, jangan tulis dalam jawapan):
            1. Perhatikan KUAH/SOS — warna, kepekatan, ada santan atau tidak:
               - Kuah hampir jernih / kuning pucat & cair, berasaskan air asam (asam keping) + kunyit + cili padi => SINGGANG / PINDANG (BUKAN kari!).
               - Kuah kuning pekat & keruh, berasaskan santan + cili => MASAK LEMAK CILI API / GULAI.
               - Kuah oren/merah kekuningan pekat, berempah (ketumbar/jintan) => KARI.
               - Kuah merah pekat & pedas, ada tomato/bendi => ASAM PEDAS.
               - Kuah kuning pekat berasaskan kunyit + santan => KARI/GULAI (hanya jika nampak rempah kari).
               - Tiada kuah / kering => GORENG, BAKAR, SAMBAL TUMIS atau KICAP.
            2. Perhatikan bahan utama: jenis ikan (kembung, tenggiri, senangin, bawal), ayam, daging, telur, tauhu, tempe atau sayur.
            3. Perhatikan bahan sampingan: bendi, terung, tomato, kentang, nanas, daun kesum, daun kunyit, cili padi.

            BEZA PENTING (jangan keliru):
            - Ikan Singgang: kuah hampir jernih/kuning muda, hirisan bawang + cili padi + asam keping + daun kunyit/kesum. TIADA santan, TIDAK berwarna oren.
            - Ikan Kari: kuah oren/merah pekat berempah, kadangkala bersantan.
            - Ikan Masak Lemak: kuah kuning pekat bersantan.
            - Ikan Asam Pedas: kuah merah pekat pedas.
            - Ikan Goreng: kering, permukaan garing, tiada kuah.

            PANDUAN B — MASAKAN BARAT/ANTARABANGSA:
            - Kenal pasti ikut NAMA STANDARD antarabangsa yang biasa dikenali, bukan nama tempatan yang dipaksa.
            - Contoh: "Cheeseburger", "Pepperoni Pizza", "Spaghetti Carbonara", "Grilled Chicken Caesar Salad", "Chicken Sandwich", "French Fries", "Fried Chicken", "Beef Steak", "Chicken Chop", "Sushi Roll".
            - Perhatikan: jenis roti/bun, jenis keju, sos (mayo, tomato, BBQ, krim), jenis daging/ayam, ada kentang/nasi/roti sisi atau tidak.
            - Anggar saiz & kalori ikut hidangan barat biasa (cth: burger standard ±250–350g, sepinggan pasta ±300g). Jangan overestimate.

            PERATURAN WAJIB (kedua-dua panduan):
            - JANGAN pulangkan nama generik seperti "Makanan", "Ikan Masak" atau "Hidangan".
            - JANGAN terus anggap semua ikan berkuah sebagai "Kari". Pilih nama PALING SEPADAN dengan warna kuah & bahan yang kelihatan.
            - Jika ragu, pilih nama yang paling sepadan dengan apa yang KELIHATAN (warna, tekstur, bahan).
            - Sertakan cara masakan dalam nama bila jelas (cth: "Ikan Singgang", "Ikan Kari", "Ayam Masak Merah", "Sambal Tumis Udang", "Grilled Chicken", "Beef Burger").
            - Anggar berat sebenar hidangan (gram) & kira kalori realistik. Jangan overestimate.

            Balas HANYA dengan JSON ini (tiada ayat lain):
            {
              "name": "Nama hidangan spesifik",
              "serving": "Anggaran Berat | PROTEIN: 0g, KARBOHIDRAT: 0g, LEMAK: 0g",
              "gram": 0.0,
              "calories": 0.0
            }
            """.trimIndent()

            val contentArray = JSONArray().apply {
                put(JSONObject().put("type", "text").put("text", prompt))
                put(
                    JSONObject()
                        .put("type", "image_url")
                        .put("image_url", JSONObject().put("url", dataUrl))
                )
            }

            val messages = JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", contentArray))
            }

            val body = binaBody(messages, maxTokens = 512)
            val rawResponse = hantarPermintaan(body) ?: return@withContext null
            val rawAiText = ambilContent(rawResponse)?.takeIf { it.isNotEmpty() }
                ?: return@withContext null

            val foodJson = JSONObject(bersihkanJson(rawAiText))

            return@withContext Food(
                name = foodJson.optString("name", "Makanan"),
                serving = foodJson.optString("serving", "1 hidangan"),
                gram = foodJson.optDouble("gram", 100.0),
                calories = foodJson.optDouble("calories", 0.0),
                unit = "g"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // ================= HELPER =================

    private fun binaBody(messages: JSONArray, maxTokens: Int): JSONObject =
        JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("temperature", 0.0)
            put("max_tokens", maxTokens)
            // Paksa output JSON
            put("response_format", JSONObject().put("type", "json_object"))
            // Matikan thinking mode supaya laju & murah
            put("thinking", JSONObject().put("type", "disabled"))
        }

    private fun hantarPermintaan(body: JSONObject): String? {
        val url = URL(CHAT_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.doOutput = true

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().readText()
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.readText()
            Log.e("DEEPSEEK_ERROR", "Server Ralat: $responseCode -> Mesej: $errorText")
            null
        }
    }

    private fun ambilContent(responseText: String): String? {
        val jsonResponse = JSONObject(responseText)
        val choices = jsonResponse.optJSONArray("choices") ?: return null
        if (choices.length() == 0) return null
        return choices.getJSONObject(0)
            .optJSONObject("message")
            ?.optString("content")
            ?.trim()
    }

    private fun bersihkanJson(rawAiText: String): String {
        var cleanJson = rawAiText
        if (cleanJson.contains("```")) {
            cleanJson = cleanJson.replace("```json", "").replace("```", "").trim()
        }
        return cleanJson
    }
}

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

            val prompt = """
            Anda adalah Pakar Nutrisi Malaysia yang sangat teliti. Tugas anda adalah mengenalpasti menu dalam gambar dengan tepat.

            Berikan hasil dalam format json SAHAJA:
            {
              "name": "Nama Makanan Spesifik",
              "serving": "Anggaran Berat | PROTEIN: 0g, KARBOHIDRAT: 0g, LEMAK: 0g",
              "gram": 0.0,
              "calories": 0.0
            }

            PENTING: Masukkan info makro nutrisi dalam ruangan 'serving' selepas tanda '|'.
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

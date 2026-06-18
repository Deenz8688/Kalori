package com.deenzstudios.kalori

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object GeminiHelper {

    // 🔥 Pastikan API Key sejati awak kekal dlm ni bro
    private const val API_KEY = ""

    // 🚀 TUKAR KEPADA MODEL GEMINI 2.5 FLASH (Kalis Traffic Sesak 503)
    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$API_KEY"

    suspend fun dapatkanKaloriDariAI(inputMenuUser: String): Food? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GEMINI_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val arahanPrompt = """
                Anda adalah pakar nutrisi dan database kalori makanan global. Kira kalori total dengan tepat berdasarkan menu dan kuantiti dari mana-mana jenis hidangan di dunia yang diberikan oleh user.
                Input user: "$inputMenuUser"
                
                Arahan Penting:
                1. Jika input mengandungi kuantiti hidangan bertulis (contoh: "2 mangkuk", "3 biji", "1 pinggan", "2 slices", "1 bowl"), anda mestilah mengira JUMLAH KESELURUHAN KALORI untuk gandaan kuantiti tersebut.
                2. Sila kembalikan nama makanan yang kemas, saiz hidangan (serving) yang ditaip oleh user, anggaran total berat dlm gram, dan jumlah total kalori (calories).
                3. Jika menu digabungkan dengan simbol seperti '+', anggap ia sebagai satu kombinasi makanan yang lengkap dan hitung kesemuanya sekali dlm satu JSON.
                
                Anda WAJIB membalas dalam bentuk JSON MENTAH SAHAJA tanpa teks hiasan, tanpa markdown, dan tanpa simbol ```json atau ```.
                
                Format JSON wajib tepat seperti ini:
                {"name":"Nama Makanan","serving":"Kuantiti Hidangan User","gram":150.0,"calories":350.0}
            """.trimIndent()

            val jsonRequestBody = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val partsArray = org.json.JSONArray().apply {
                        put(JSONObject().put("text", arahanPrompt))
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)
            }

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonRequestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(responseText)

                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) return@withContext null

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                var rawAiText = parts?.getJSONObject(0)?.optString("text")?.trim() ?: ""

                if (rawAiText.isEmpty()) return@withContext null

                if (rawAiText.contains("```")) {
                    rawAiText = rawAiText.replace("```json", "").replace("```", "").trim()
                }

                val foodJson = JSONObject(rawAiText)

                return@withContext Food(
                    name = foodJson.optString("name", "Makanan"),
                    serving = foodJson.optString("serving", "1 hidangan"),
                    gram = foodJson.optDouble("gram", 100.0),
                    calories = foodJson.optDouble("calories", 0.0),
                    unit = "g"
                )
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText()
                android.util.Log.e("GEMINI_ERROR", "Server Ralat: $responseCode -> Mesej: $errorText")
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}

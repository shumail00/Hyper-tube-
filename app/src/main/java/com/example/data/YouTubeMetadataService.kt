package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ResolvedYouTubeMetadata(
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val dominantColorHex: Long,
    val secondaryColorHex: Long
)

class YouTubeMetadataService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) {

    suspend fun resolveVideoMetadata(videoId: String): ResolvedYouTubeMetadata = withContext(Dispatchers.IO) {
        val canonicalWatchUrl = "https://www.youtube.com/watch?v=$videoId"
        val encodedUrl = URLEncoder.encode(canonicalWatchUrl, "UTF-8")
        val highResThumb = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

        var title = "YouTube Video • $videoId"
        var author = "YouTube Creator"
        var thumbUrl = highResThumb

        // 1. Query official YouTube oEmbed endpoint
        val endpoints = listOf(
            "https://www.youtube.com/oembed?url=$encodedUrl&format=json",
            "https://noembed.com/embed?url=$encodedUrl"
        )

        for (endpoint in endpoints) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 15; Xiaomi Pad 7 Pro)")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val json = JSONObject(bodyStr)
                            val parsedTitle = json.optString("title").trim()
                            val parsedAuthor = json.optString("author_name").trim()
                            val parsedThumb = json.optString("thumbnail_url").trim()
                            if (parsedTitle.isNotEmpty()) title = parsedTitle
                            if (parsedAuthor.isNotEmpty()) author = parsedAuthor
                            if (parsedThumb.isNotEmpty()) thumbUrl = parsedThumb
                        }
                    }
                }
                if (title != "YouTube Video • $videoId") break
            } catch (_: Exception) {
                // Continue to fallback endpoint or deterministic palette
            }
        }

        // 2. Sample real dominant & secondary ambient glow colors from the video thumbnail
        val (primaryColor, secondaryColor) = extractAmbientColorsFromThumbnail(thumbUrl, videoId)

        ResolvedYouTubeMetadata(
            videoId = videoId,
            title = title,
            channelName = author,
            thumbnailUrl = thumbUrl,
            dominantColorHex = primaryColor,
            secondaryColorHex = secondaryColor
        )
    }

    private fun extractAmbientColorsFromThumbnail(thumbnailUrl: String, videoId: String): Pair<Long, Long> {
        try {
            val request = Request.Builder()
                .url(thumbnailUrl)
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 4
                        }
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                        if (bitmap != null) {
                            val pair = sampleVibrantPalette(bitmap)
                            bitmap.recycle()
                            return pair
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to deterministic HyperOS palette if offline
        }
        return deterministicHyperPalette(videoId)
    }

    internal fun sampleVibrantPalette(bitmap: Bitmap): Pair<Long, Long> {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return 0xFF2A68F6L to 0xFF2CE5D3L

        var weightedR1 = 0f
        var weightedG1 = 0f
        var weightedB1 = 0f
        var weightSum1 = 0f

        var weightedR2 = 0f
        var weightedG2 = 0f
        var weightedB2 = 0f
        var weightSum2 = 0f

        val stepX = max(1, width / 16)
        val stepY = max(1, height / 12)
        val hsv = FloatArray(3)

        // Skip top/bottom letterbox bars (15% top/bottom)
        val startY = (height * 0.15f).toInt()
        val endY = (height * 0.85f).toInt().coerceAtLeast(startY + 1)

        for (y in startY until endY step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)

                AndroidColor.RGBToHSV(r, g, b, hsv)
                val saturation = hsv[1]
                val value = hsv[2]

                // Favor vibrant mid-to-bright pixels for HyperOS ambient lighting
                if (value in 0.18f..0.96f && saturation >= 0.15f) {
                    val weight = (saturation * 1.6f + 0.2f) * (1f - abs(value - 0.65f))
                    if (x < width / 2) {
                        weightedR1 += r * weight
                        weightedG1 += g * weight
                        weightedB1 += b * weight
                        weightSum1 += weight
                    } else {
                        weightedR2 += r * weight
                        weightedG2 += g * weight
                        weightedB2 += b * weight
                        weightSum2 += weight
                    }
                }
            }
        }

        val primary = if (weightSum1 > 0.5f) {
            boostForAmbientGlow(
                (weightedR1 / weightSum1).toInt(),
                (weightedG1 / weightSum1).toInt(),
                (weightedB1 / weightSum1).toInt()
            )
        } else {
            0xFF2A68F6L
        }

        val secondary = if (weightSum2 > 0.5f) {
            boostForAmbientGlow(
                (weightedR2 / weightSum2).toInt(),
                (weightedG2 / weightSum2).toInt(),
                (weightedB2 / weightSum2).toInt()
            )
        } else {
            0xFF2CE5D3L
        }

        return primary to secondary
    }

    private fun boostForAmbientGlow(r: Int, g: Int, b: Int): Long {
        val hsv = FloatArray(3)
        AndroidColor.RGBToHSV(
            r.coerceIn(0, 255),
            g.coerceIn(0, 255),
            b.coerceIn(0, 255),
            hsv
        )
        hsv[1] = min(1f, max(0.55f, hsv[1] * 1.25f))
        hsv[2] = min(1f, max(0.72f, hsv[2] * 1.15f))
        val boostedInt = AndroidColor.HSVToColor(0xFF, hsv)
        return boostedInt.toLong() and 0xFFFFFFFFL
    }

    fun deterministicHyperPalette(videoId: String): Pair<Long, Long> {
        val palettes = listOf(
            0xFF2A68F6L to 0xFF2CE5D3L, // Hyper Blue + Cyan
            0xFF635BFFL to 0xFF00D4FFL, // Electric Indigo + Sky
            0xFFFF4D6DL to 0xFFFF9F1CL, // Hyper Coral + Amber
            0xFF00B8A9L to 0xFF4D88FFL, // Emerald Teal + Sapphire
            0xFF8B5CF6L to 0xFFEC4899L  // Violet + Rose Glow
        )
        val idx = abs(videoId.hashCode()) % palettes.size
        return palettes[idx]
    }
}

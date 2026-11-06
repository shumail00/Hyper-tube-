package com.example.data

data class ParsedYouTubeLink(
    val videoId: String,
    val startSeconds: Int = 0,
    val canonicalUrl: String = "https://www.youtube.com/watch?v=$videoId"
)

object YouTubeUrlParser {
    // Matches 11-character YouTube video IDs
    private val VIDEO_ID_REGEX = Regex("^[a-zA-Z0-9_-]{11}$")

    // Matches URLs inside arbitrary text (such as clipboard or share text)
    private val URL_EXTRACTOR_REGEX = Regex(
        """(https?://(?:www\.|m\.)?(?:youtube\.com/(?:watch\?[^\s"']+|embed/[a-zA-Z0-9_-]{11}|shorts/[a-zA-Z0-9_-]{11}|live/[a-zA-Z0-9_-]{11})|youtu\.be/[a-zA-Z0-9_-]{11}[^\s"']*))""",
        RegexOption.IGNORE_CASE
    )

    private val PATTERNS = listOf(
        Regex("""(?:youtube\.com/watch\?(?:.*&)?v=)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        Regex("""(?:youtu\.be/)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        Regex("""(?:youtube\.com/embed/)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        Regex("""(?:youtube\.com/shorts/)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        Regex("""(?:youtube\.com/live/)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        Regex("""(?:youtube-nocookie\.com/embed/)([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE)
    )

    private val TIME_PARAM_REGEX = Regex("""[?&](?:t|start)=([0-9hms]+)""", RegexOption.IGNORE_CASE)

    fun parse(rawInput: String?): ParsedYouTubeLink? {
        if (rawInput.isNullOrBlank()) return null
        val trimmed = rawInput.trim()

        // Direct 11-char video ID support
        if (VIDEO_ID_REGEX.matches(trimmed)) {
            return ParsedYouTubeLink(videoId = trimmed)
        }

        for (pattern in PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null) {
                val id = match.groupValues[1]
                if (VIDEO_ID_REGEX.matches(id)) {
                    val startSec = extractStartSeconds(trimmed)
                    return ParsedYouTubeLink(
                        videoId = id,
                        startSeconds = startSec,
                        canonicalUrl = "https://www.youtube.com/watch?v=$id"
                    )
                }
            }
        }
        return null
    }

    fun findFirstYouTubeLinkInText(text: String?): ParsedYouTubeLink? {
        if (text.isNullOrBlank()) return null
        parse(text)?.let { return it }
        val urlMatch = URL_EXTRACTOR_REGEX.find(text) ?: return null
        return parse(urlMatch.value)
    }

    private fun extractStartSeconds(url: String): Int {
        val match = TIME_PARAM_REGEX.find(url) ?: return 0
        val raw = match.groupValues[1].lowercase()
        raw.toIntOrNull()?.let { return it }

        var totalSeconds = 0
        val hours = Regex("""(\d+)h""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = Regex("""(\d+)m""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val seconds = Regex("""(\d+)s""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        totalSeconds += hours * 3600 + minutes * 60 + seconds
        return totalSeconds
    }

    fun formatTimestamp(seconds: Int): String {
        val safeSec = seconds.coerceAtLeast(0)
        val hrs = safeSec / 3600
        val mins = (safeSec % 3600) / 60
        val secs = safeSec % 60
        return if (hrs > 0) {
            "%d:%02d:%02d".format(hrs, mins, secs)
        } else {
            "%02d:%02d".format(mins, secs)
        }
    }
}

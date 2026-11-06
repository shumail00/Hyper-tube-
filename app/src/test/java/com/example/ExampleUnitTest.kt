package com.example

import com.example.data.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun parseStandardAndShortYouTubeLinks() {
        val watch = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=aircAruvnKk&t=125s")
        assertNotNull(watch)
        assertEquals("aircAruvnKk", watch?.videoId)
        assertEquals(125, watch?.startSeconds)

        val shortLink = YouTubeUrlParser.parse("https://youtu.be/jfKfPfyJRdk?t=2m10s")
        assertNotNull(shortLink)
        assertEquals("jfKfPfyJRdk", shortLink?.videoId)
        assertEquals(130, shortLink?.startSeconds)

        val embeddedInClipboard = YouTubeUrlParser.findFirstYouTubeLinkInText(
            "Check out this lecture: https://www.youtube.com/watch?v=8jPQjjsBbIc right now!"
        )
        assertNotNull(embeddedInClipboard)
        assertEquals("8jPQjjsBbIc", embeddedInClipboard?.videoId)

        assertNull(YouTubeUrlParser.parse("https://example.com/not-youtube"))
    }
}

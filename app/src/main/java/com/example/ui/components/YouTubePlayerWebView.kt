package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.LocalHyperOSGlass
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerWebView(
    videoId: String,
    videoTitle: String,
    thumbnailUrl: String,
    initialStartSeconds: Int,
    playbackSpeed: Float,
    isLoopEnabled: Boolean,
    seekCommandTrigger: Pair<Int, Long>?,
    onTimeUpdated: (currentSeconds: Int, durationSeconds: Int) -> Unit,
    onPlayingStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = LocalHyperOSGlass.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var embedErrorCode by remember(videoId) { mutableIntStateOf(0) }
    var loadedVideoId by remember { mutableStateOf("") }

    // Synchronize videoId changes with the embedded IFrame player
    LaunchedEffect(videoId, isPlayerReady) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (isPlayerReady && videoId.isNotBlank() && loadedVideoId != videoId) {
            embedErrorCode = 0
            loadedVideoId = videoId
            wv.evaluateJavascript(
                "window.loadHyperVideo('${videoId}', ${initialStartSeconds.coerceAtLeast(0)});",
                null
            )
        }
    }

    // Synchronize seek commands from study note timecode pills or scrub bar
    LaunchedEffect(seekCommandTrigger) {
        val trigger = seekCommandTrigger ?: return@LaunchedEffect
        val wv = webViewRef ?: return@LaunchedEffect
        if (isPlayerReady) {
            wv.evaluateJavascript("window.seekHyperVideo(${trigger.first});", null)
        }
    }

    // Synchronize playback rate
    LaunchedEffect(playbackSpeed, isPlayerReady) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (isPlayerReady) {
            wv.evaluateJavascript("window.setHyperPlaybackRate(${playbackSpeed});", null)
        }
    }

    // Synchronize loop toggle
    LaunchedEffect(isLoopEnabled, isPlayerReady) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (isPlayerReady) {
            wv.evaluateJavascript("window.setHyperLoop(${isLoopEnabled});", null)
        }
    }

    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(Color(0xFF07090E))
            .border(width = 1.5.dp, color = glass.glassBorder, shape = shape)
            .testTag("youtube_player_container")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.rgb(7, 9, 14))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadsImagesAutomatically = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 15; Xiaomi Pad 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                    }

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val host = request?.url?.host?.lowercase() ?: return false
                            // Block navigating away to external feeds or recommendation pages
                            return !(host.contains("youtube.com") ||
                                host.contains("youtube-nocookie.com") ||
                                host.contains("ytimg.com") ||
                                host.contains("googlevideo.com"))
                        }
                    }

                    val mainHandler = Handler(Looper.getMainLooper())
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onReady(durationSec: Double) {
                                mainHandler.post {
                                    isPlayerReady = true
                                    loadedVideoId = videoId
                                    val dur = durationSec.takeIf { !it.isNaN() }?.roundToInt() ?: 0
                                    if (dur > 0) {
                                        onTimeUpdated(initialStartSeconds, dur)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onTime(currentSec: Double, durationSec: Double) {
                                mainHandler.post {
                                    val cur = currentSec.takeIf { !it.isNaN() }?.roundToInt() ?: 0
                                    val dur = durationSec.takeIf { !it.isNaN() }?.roundToInt() ?: 0
                                    onTimeUpdated(cur, dur)
                                }
                            }

                            @JavascriptInterface
                            fun onStateChange(stateCode: Int) {
                                mainHandler.post {
                                    // YT.PlayerState.PLAYING == 1
                                    onPlayingStateChanged(stateCode == 1)
                                }
                            }

                            @JavascriptInterface
                            fun onPlayerError(errorCode: Int) {
                                mainHandler.post {
                                    embedErrorCode = errorCode
                                }
                            }
                        },
                        "HyperBridge"
                    )

                    val html = buildYouTubeIFrameHtml(
                        initialVideoId = videoId,
                        initialStartSeconds = initialStartSeconds,
                        initialSpeed = playbackSpeed,
                        initialLoop = isLoopEnabled
                    )
                    loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                    webViewRef = this
                }
            }
        )

        // Subtle loading overlay before IFrame API initializes
        AnimatedVisibility(
            visible = !isPlayerReady && embedErrorCode == 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = videoTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x9907090E),
                                    Color(0xCC07090E)
                                )
                            )
                        )
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = glass.accentBlue,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Initializing HyperOS Theatre…",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }
        }

        // Graceful fallback if the video uploader restricted third-party embeds (Error 101 / 150)
        AnimatedVisibility(
            visible = embedErrorCode == 101 || embedErrorCode == 150,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = videoTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xD90B0D13))
                )
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Restricted embed",
                        tint = glass.accentBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Creator Restricted Embedded Playback",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You can still capture timestamped study notes here or launch this video directly in full-window view.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C0D0),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(
                            onClick = {
                                embedErrorCode = 0
                                webViewRef?.evaluateJavascript(
                                    "window.loadHyperVideo('${videoId}', ${initialStartSeconds});",
                                    null
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry embed",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Embed")
                        }
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                                )
                                runCatching { context.startActivity(intent) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = glass.accentBlue)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open direct stream",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Direct Stream")
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}

private fun buildYouTubeIFrameHtml(
    initialVideoId: String,
    initialStartSeconds: Int,
    initialSpeed: Float,
    initialLoop: Boolean
): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body {
                    width: 100%;
                    height: 100%;
                    background-color: #07090E;
                    overflow: hidden;
                    font-family: sans-serif;
                }
                #player {
                    width: 100%;
                    height: 100%;
                    border: 0;
                }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player = null;
                var isReady = false;
                var loopEnabled = $initialLoop;
                var currentRate = $initialSpeed;
                var pollTimer = null;

                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        width: '100%',
                        height: '100%',
                        videoId: '$initialVideoId',
                        host: 'https://www.youtube-nocookie.com',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 1,
                            'rel': 0,
                            'modestbranding': 1,
                            'iv_load_policy': 3,
                            'playsinline': 1,
                            'fs': 1,
                            'start': ${initialStartSeconds.coerceAtLeast(0)},
                            'origin': 'https://www.youtube-nocookie.com'
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange,
                            'onError': onPlayerError
                        }
                    });
                }

                function onPlayerReady(event) {
                    isReady = true;
                    try {
                        player.setPlaybackRate(currentRate);
                    } catch (e) {}
                    var dur = 0;
                    try { dur = player.getDuration() || 0; } catch (e) {}
                    if (window.HyperBridge) {
                        window.HyperBridge.onReady(dur);
                    }
                    if (pollTimer) clearInterval(pollTimer);
                    pollTimer = setInterval(function() {
                        if (player && isReady && player.getCurrentTime) {
                            var cur = player.getCurrentTime() || 0;
                            var d = player.getDuration() || 0;
                            if (window.HyperBridge) {
                                window.HyperBridge.onTime(cur, d);
                            }
                        }
                    }, 500);
                }

                function onPlayerStateChange(event) {
                    if (window.HyperBridge) {
                        window.HyperBridge.onStateChange(event.data);
                    }
                    // YT.PlayerState.ENDED == 0
                    if (event.data === 0 && loopEnabled && player) {
                        player.seekTo(0, true);
                        player.playVideo();
                    }
                }

                function onPlayerError(event) {
                    if (window.HyperBridge) {
                        window.HyperBridge.onPlayerError(event.data || 0);
                    }
                }

                window.loadHyperVideo = function(videoId, startSeconds) {
                    if (player && isReady && player.loadVideoById) {
                        player.loadVideoById({
                            videoId: videoId,
                            startSeconds: startSeconds || 0
                        });
                        setTimeout(function() {
                            try { player.setPlaybackRate(currentRate); } catch(e) {}
                        }, 400);
                    }
                };

                window.seekHyperVideo = function(seconds) {
                    if (player && isReady && player.seekTo) {
                        player.seekTo(seconds, true);
                        player.playVideo();
                    }
                };

                window.setHyperPlaybackRate = function(rate) {
                    currentRate = rate;
                    if (player && isReady && player.setPlaybackRate) {
                        try { player.setPlaybackRate(rate); } catch (e) {}
                    }
                };

                window.setHyperLoop = function(loop) {
                    loopEnabled = !!loop;
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}

package com.dito.app.core.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dito.app.core.data.MediaSessionEvent
import com.dito.app.core.data.RealmConfig
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView
import android.util.Log
import kotlinx.coroutines.*

class YouTubeAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "YouTubeService"

        private const val MIN_TITLE_LENGTH = 15
        private const val MAX_TITLE_LENGTH = 150
        private const val MIN_CHANNEL_LENGTH = 2
        private const val MAX_CHANNEL_LENGTH = 50

        private const val DEBOUNCE_DELAY = 500L
        private const val HYBRID_MAX_DEPTH = 5
        private const val TEXTSCAN_MAX_DEPTH = 6

        private val TITLE_IDS = listOf(
            "com.google.android.youtube:id/title",
            "com.google.android.youtube:id/video_title",
            "com.google.android.youtube:id/compact_title",
            "com.google.android.youtube:id/video_title_text",
            "com.google.android.youtube:id/text"
        )

        private val CHANNEL_IDS = listOf(
            "com.google.android.youtube:id/channel_name",
            "com.google.android.youtube:id/metadata",
            "com.google.android.youtube:id/text",
            "com.google.android.youtube:id/attributed_channel_name"
        )

        private val BLACKLIST_PATTERNS = listOf(
            "(?i)\\bhome\\b", "(?i)\\bshorts\\b", "(?i)\\bsubscriptions\\b", "(?i)\\blibrary\\b",
            "(?i)홈", "(?i)쇼츠", "(?i)구독", "(?i)보관함",
            "(?i)youtube premium", "(?i)play store",
            "(?i)좋아요", "(?i)싫어요", "(?i)공유", "(?i)저장", "(?i)visit advertiser","(?i)sponsored"
        ).map { Regex(it) }


        //채널 무효 패턴: 조회수, 광고 문구 등 채널 아닌 문자열 제외 -> 우리 서비스는 youtube premium 유저 대상?
        private val CHANNEL_INVALID_PATTERNS = listOf(
            Regex(".*\\d+[KMB]?\\s*(views?|watching|waiting).*", RegexOption.IGNORE_CASE),
            Regex(".*\\d+:\\d+.*"),
            Regex("(?i)visit advertiser"),
            Regex("(?i)sponsored")
        )
    }

    private var lastDetectedVideo: Triple<String, String, String>? = null
    private var lastEventTime = 0L
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "✅ YouTube Service 연결됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName != "com.google.android.youtube") {
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        // 디바운스 처리: 짧은 시간 내 연속 이벤트 무시
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime < DEBOUNCE_DELAY) return
        lastEventTime = currentTime

        currentJob?.cancel()
        currentJob = serviceScope.launch {
            try {
                val rootNode = rootInActiveWindow ?: return@launch
                val result = withTimeoutOrNull(1200L) { findYouTubeVideoAndChannel(rootNode) }


                if (result != null && result != lastDetectedVideo) {
                    lastDetectedVideo = result

                    val title = result.first
                    val channel = result.second
                    val detectionMethod = result.third

                    Log.i(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                    Log.i(TAG, "🎬 영상 감지!")
                    Log.i(TAG, "   제목: ${result.first}")
                    Log.i(TAG, "   채널: ${result.second}")
                    Log.i(TAG, "   방법: ${result.third}")
                    Log.i(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                    saveYouTubeVideoStart(title, channel, detectionMethod)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing event", e)
            }
        }
    }

    //제목과 채널 추출
    private suspend fun findYouTubeVideoAndChannel(node: AccessibilityNodeInfo): Triple<String, String, String>? {
        return withContext(Dispatchers.Default) {

            //1. Resource-ID 기반 탐색
            TITLE_IDS.forEach { titleId ->
                val titleNode = node.findAccessibilityNodeInfosByViewId(titleId).firstOrNull()
                val title = titleNode?.text?.toString()?.trim()
                if (!title.isNullOrEmpty() && title.length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH && !isBlacklisted(title)) {

                    // 채널 resource-id 탐색
                    CHANNEL_IDS.forEach { channelId ->
                        val channelNode = node.findAccessibilityNodeInfosByViewId(channelId).firstOrNull()
                        val channel = channelNode?.text?.toString()?.trim()
                        if (!channel.isNullOrEmpty() && isValidChannel(channel)) {
                            return@withContext Triple(title, channel, "resource-id")
                        }
                    }

                    // hybrid 탐색 (부모/자식)
                    val channelHybrid = findChannelFromTextHybrid(titleNode, HYBRID_MAX_DEPTH)
                    if (channelHybrid != null) return@withContext Triple(title, channelHybrid, "hybrid")
                }
            }

            //2. 최후 수단: 전체 텍스트 탐색
            val textNodes = mutableListOf<String>()
            fun traverse(n: AccessibilityNodeInfo, depth: Int) {
                if (depth > TEXTSCAN_MAX_DEPTH) return
                if (n.className == TextView::class.java.name) {
                    n.text?.toString()?.trim()?.let { text ->
                        if (text.length >= MIN_TITLE_LENGTH) textNodes.add(text)
                    }
                }
                for (i in 0 until n.childCount) n.getChild(i)?.let { traverse(it, depth + 1) }
            }
            traverse(node, 0)

            val filtered = textNodes.filter { t -> t.length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH && !isBlacklisted(t) }
            val title = filtered.maxByOrNull { it.length } ?: return@withContext null
            val titleIndex = textNodes.indexOf(title)
            val channelCandidates = textNodes.drop(titleIndex + 1).filter { isValidChannel(it) }
            val channel = channelCandidates.firstOrNull() ?: "알 수 없는 채널"

            Triple(title, channel, "text-scan")
        }
    }

    private fun findChannelFromTextHybrid(node: AccessibilityNodeInfo, maxDepth: Int): String? {
        val candidates = mutableListOf<String>()
        fun traverse(n: AccessibilityNodeInfo, depth: Int) {
            if (depth > maxDepth) return
            if (n.className == TextView::class.java.name) {
                n.text?.toString()?.trim()?.let { text ->
                    if (text.length in MIN_CHANNEL_LENGTH..MAX_CHANNEL_LENGTH) candidates.add(text)
                }
            }
            for (i in 0 until n.childCount) n.getChild(i)?.let { traverse(it, depth + 1) }
        }
        traverse(node, 0)
        return candidates.firstOrNull { isValidChannel(it) }
    }

    private fun isValidChannel(text: String): Boolean {
        if (text.length !in MIN_CHANNEL_LENGTH..MAX_CHANNEL_LENGTH) return false
        if (CHANNEL_INVALID_PATTERNS.any { it.containsMatchIn(text) }) return false
        if (isBlacklisted(text)) return false
        return true
    }

    private fun isBlacklisted(text: String): Boolean {
        return BLACKLIST_PATTERNS.any { it.containsMatchIn(text) }
    }

    override fun onInterrupt() {
        Log.w(TAG, "⚠️ Service Interrupted")
    }


    // youtube 영상 시작 저장
    // 영상 제목/채널 감지 시 호출 ->VIDEO_START 이벤트로 기록
    private fun saveYouTubeVideoStart(title: String, channel: String, detectionMethod: String) {
        try {
            val realm = RealmConfig.getInstance()
            val currentTime = System.currentTimeMillis()

            realm.writeBlocking {
                copyToRealm(MediaSessionEvent().apply {
                    this.eventType = "VIDEO_START"
                    this.title = title
                    this.channel = channel
                    this.appPackage = "com.google.android.youtube"
                    this.timestamp = currentTime
                    this.videoDuration = 0L
                    this.watchTime = 0L
                    this.pauseTime = 0L
                    this.date = formatDate(currentTime)
                    this.detectionMethod = detectionMethod   // resource-id, hybrid, text-scan
                    this.synced = false
                })
            }

            Log.d(TAG, "✅ Realm 저장 완료 (YouTube 시작)")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "🛑 Service Destroyed")
    }
}

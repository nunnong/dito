package com.dito.app.core.service.phone

/**
 * PlaybackProbe: YouTube 재생 상태 감지 유틸리티
 * 탐색 워처에서 "비재생 상태" 확인용
 */
object PlaybackProbe {
    @Volatile
    private var lastPlaybackTime: Long = 0L

    fun recordPlayback() {
        lastPlaybackTime = System.currentTimeMillis()
    }

    /**
     * 지정된 시간 동안 재생 없었는지 확인
     * @param durationMs 확인할 시간 (밀리초)
     * @return true면 해당 시간 동안 재생 없음 (탐색 중)
     */
    fun isNotPlayingFor(durationMs: Long): Boolean {
        if (lastPlaybackTime == 0L) return true // 한 번도 재생 안 함
        val elapsed = System.currentTimeMillis() - lastPlaybackTime
        return elapsed >= durationMs
    }

    fun reset() {
        lastPlaybackTime = 0L
    }
}

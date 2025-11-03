package com.dito.app.core.data

//앱 사용 이벤트와 미디어 세션 이벤트를 한 번에 묶어서 서버 전송
data class EventBatchRequest(
    val app_usage_events: List<AppUsageEventDto>,
    val media_session_events: List<MediaSessionEventDto>
)
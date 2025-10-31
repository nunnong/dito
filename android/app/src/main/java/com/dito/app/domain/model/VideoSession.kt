package com.dito.app.domain.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class VideoSession : RealmObject {

    @PrimaryKey
    var id: ObjectId = ObjectId()

    var title: String = ""              // 영상/곡 제목
    var channel: String = ""            // 채널/아티스트명
    var appPackage: String = ""         // 앱 패키지명


    var startTime: Long = 0L            // 재생 시작 시각
    var endTime: Long = 0L              // 재생 종료 시각
    var duration: Long = 0L             // 영상/곡 전체 길이 (ms)
    var watchTime: Long = 0L            // 실제 시청/재생 시간 (ms)
    var pauseTime: Long = 0L            // 총 일시정지 시간 (ms)

    // === AI 분석 결과 (나중에 추가) ===
    var contentType: String = "UNKNOWN" // STUDY, ENTERTAINMENT, MUSIC, UNKNOWN
    var confidence: Double = 0.0        // AI 분석 신뢰도 (0.0 ~ 1.0)


    var date: String = ""               // 날짜 (yyyy-MM-dd)
}
package com.dito.app.core.data.mission

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class MissionTrackingLog : RealmObject{

    @PrimaryKey
    var _id: ObjectId = ObjectId()

    //미션 정보
    var missionId: String = ""
    var sequence: Int = 0
    var timestamp: Long = 0L
    var logType: String = ""            //"APP_USAGE", "MEDIA_SESSION", "SCREEN_ON", "SCREEN_OFF"

    //app_usage
    var packageName: String? = null
    var appName: String? = null
    var durationSeconds: Int? = null
    var isTargetApp: Boolean = false

    //media_session
    var videoTitle: String? = null
    var channelName: String? = null
    var eventType: String? = null           // "VIDEO_START", "VIDEO_END"
    var watchTimeSeconds: Int? = null
    var contentType: String? = null         // "EDUCATIONAL", "ENTERTAINMENT", "UNKNOWN"

    //동기화
    var synced: Boolean = false
    var syncedAt: Long = 0L
    var createdAt: Long = System.currentTimeMillis()



}
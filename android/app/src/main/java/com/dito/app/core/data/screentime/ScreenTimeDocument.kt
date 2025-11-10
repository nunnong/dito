package com.dito.app.core.data.screentime

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

/**
 * 로컬 Realm용 스크린타임 Document
 * Backend API 전송 실패 시 재시도를 위한 임시 저장
 */
class ScreenTimeLocalLog : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var groupId: Long = 0
    var userId: Long = 0
    var date: String = ""  // "2025-01-07"
    var totalMinutes: Int = 0
    var recordedAt: String = ""  // ISO-8601 format
    var synced: Boolean = false  // Backend API 전송 성공 여부
}

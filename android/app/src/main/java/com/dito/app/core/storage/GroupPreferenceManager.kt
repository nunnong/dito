package com.dito.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 그룹 관련 SharedPreferences 관리
 *
 * 사용 예시:
 * ```
 * // 그룹 참여 성공 시
 * GroupPreferenceManager.setActiveGroupId(context, groupId)
 *
 * // 그룹 탈퇴 시
 * GroupPreferenceManager.clearActiveGroupId(context)
 * ```
 */
object GroupPreferenceManager {

    private const val TAG = "GroupPreferenceManager"
    private const val PREF_NAME = "dito_prefs"
    private const val KEY_ACTIVE_GROUP_ID = "active_group_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 활성 그룹 ID 저장
     * @param groupId 참여한 그룹의 ID
     */
    fun setActiveGroupId(context: Context, groupId: Long) {
        getPreferences(context).edit()
            .putLong(KEY_ACTIVE_GROUP_ID, groupId)
            .apply()
        Log.d(TAG, "✅ 활성 그룹 ID 저장: $groupId")
    }

    /**
     * 활성 그룹 ID 조회
     * @return 그룹 ID (없으면 null)
     */
    fun getActiveGroupId(context: Context): Long? {
        val groupId = getPreferences(context).getLong(KEY_ACTIVE_GROUP_ID, -1L)
        return if (groupId > 0) groupId else null
    }

    /**
     * 활성 그룹 ID 삭제 (그룹 탈퇴 시)
     */
    fun clearActiveGroupId(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_ACTIVE_GROUP_ID)
            .apply()
        Log.d(TAG, "🗑️ 활성 그룹 ID 삭제")
    }

    /**
     * 활성 그룹 여부 확인
     */
    fun hasActiveGroup(context: Context): Boolean {
        return getActiveGroupId(context) != null
    }
}

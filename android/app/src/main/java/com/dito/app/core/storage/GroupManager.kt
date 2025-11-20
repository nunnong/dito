package com.dito.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 그룹 챌린지 상태를 관리하는 클래스
 * 챌린지 생성, 시작, 종료 상태를 영구 저장
 */
@Singleton
class GroupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "group_prefs"
        private const val KEY_CHALLENGE_STATUS = "challenge_status"
        private const val KEY_GROUP_ID = "group_id"
        private const val KEY_GROUP_NAME = "group_name"
        private const val KEY_GOAL = "goal"
        private const val KEY_PENALTY = "penalty"
        private const val KEY_PERIOD = "period"
        private const val KEY_BET = "bet"
        private const val KEY_TOTAL_BET = "total_bet"
        private const val KEY_ENTRY_CODE = "entry_code"
        private const val KEY_START_DATE = "start_date"
        private const val KEY_END_DATE = "end_date"
        private const val KEY_IS_LEADER = "is_leader"

        // 프론트엔드 전용 상태 (그룹에 참여하지 않음)
        const val STATUS_NO_CHALLENGE = "NO_CHALLENGE"

        // 백엔드 API 상태와 동일하게 매칭
        const val STATUS_PENDING = "pending"           // 생성했지만 START 전
        const val STATUS_IN_PROGRESS = "in_progress"   // 진행 중
        const val STATUS_COMPLETED = "completed"       // 종료됨
        const val STATUS_CANCELLED = "cancelled"       // 취소됨
    }

    /**
     * 챌린지 상태 저장
     */
    fun saveChallengeStatus(status: String) {
        prefs.edit { putString(KEY_CHALLENGE_STATUS, status) }
    }

    /**
     * 챌린지 상태 조회
     */
    fun getChallengeStatus(): String {
        return prefs.getString(KEY_CHALLENGE_STATUS, STATUS_NO_CHALLENGE) ?: STATUS_NO_CHALLENGE
    }

    /**
     * 그룹 정보 저장 (챌린지 생성 시)
     */
    fun saveGroupInfo(
        groupId: Long,
        groupName: String,
        goal: String,
        penalty: String,
        period: Int,
        bet: Int,
        entryCode: String = "",
        startDate: String = "",
        endDate: String = "",
        isLeader: Boolean = false
    ) {
        prefs.edit {
            putLong(KEY_GROUP_ID, groupId)
            putString(KEY_GROUP_NAME, groupName)
            putString(KEY_GOAL, goal)
            putString(KEY_PENALTY, penalty)
            putInt(KEY_PERIOD, period)
            putInt(KEY_BET, bet)
            putString(KEY_ENTRY_CODE, entryCode)
            putString(KEY_START_DATE, startDate)
            putString(KEY_END_DATE, endDate)
            putBoolean(KEY_IS_LEADER, isLeader)
            putString(KEY_CHALLENGE_STATUS, STATUS_PENDING)
        }
    }

    /**
     * 그룹 ID 조회
     */
    fun getGroupId(): Long {
        return prefs.getLong(KEY_GROUP_ID, 0L)
    }

    /**
     * 그룹 이름 조회
     */
    fun getGroupName(): String {
        return prefs.getString(KEY_GROUP_NAME, "") ?: ""
    }

    /**
     * 목표 조회
     */
    fun getGoal(): String {
        return prefs.getString(KEY_GOAL, "") ?: ""
    }

    /**
     * 벌칙 조회
     */
    fun getPenalty(): String {
        return prefs.getString(KEY_PENALTY, "") ?: ""
    }

    /**
     * 기간 조회
     */
    fun getPeriod(): Int {
        return prefs.getInt(KEY_PERIOD, 0)
    }

    /**
     * 배팅 금액 조회
     */
    fun getBet(): Int {
        return prefs.getInt(KEY_BET, 0)
    }

    /**
     * 전체 배팅 금액 조회
     */
    fun getTotalBet(): Int {
        return prefs.getInt(KEY_TOTAL_BET, 0)
    }

    /**
     * 전체 배팅 금액 저장
     */
    fun saveTotalBet(totalBet: Int) {
        prefs.edit { putInt(KEY_TOTAL_BET, totalBet) }
    }

    /**
     * 입장 코드 조회
     */
    fun getEntryCode(): String {
        return prefs.getString(KEY_ENTRY_CODE, "") ?: ""
    }

    /**
     * 입장 코드 저장
     */
    fun saveEntryCode(code: String) {
        prefs.edit { putString(KEY_ENTRY_CODE, code) }
    }

    /**
     * 시작일 조회
     */
    fun getStartDate(): String {
        return prefs.getString(KEY_START_DATE, "") ?: ""
    }

    /**
     * 종료일 조회
     */
    fun getEndDate(): String {
        return prefs.getString(KEY_END_DATE, "") ?: ""
    }

    /**
     * 챌린지 시작
     */
    fun startChallenge() {
        saveChallengeStatus(STATUS_IN_PROGRESS)
    }

    /**
     * 챌린지 종료 (모든 정보 삭제)
     */
    fun endChallenge() {
        prefs.edit { clear() }
    }

    /**
     * 챌린지가 존재하는지 확인
     */
    fun hasChallenge(): Boolean {
        return getChallengeStatus() != STATUS_NO_CHALLENGE
    }

    /**
     * 챌린지가 대기 중인지 확인 (pending 상태)
     */
    fun isPending(): Boolean {
        return getChallengeStatus() == STATUS_PENDING
    }

    /**
     * 챌린지가 진행 중인지 확인
     */
    fun isInProgress(): Boolean {
        return getChallengeStatus() == STATUS_IN_PROGRESS
    }

    /**
     * 챌린지가 완료되었는지 확인
     */
    fun isCompleted(): Boolean {
        return getChallengeStatus() == STATUS_COMPLETED
    }

    /**
     * 챌린지가 취소되었는지 확인
     */
    fun isCancelled(): Boolean {
        return getChallengeStatus() == STATUS_CANCELLED
    }

    /**
     * 방장 여부 확인
     */
    fun isLeader(): Boolean {
        return prefs.getBoolean(KEY_IS_LEADER, false)
    }
}

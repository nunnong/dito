package com.dito.app.core.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableMessageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    private val nodeClient: NodeClient by lazy {
        Wearable.getNodeClient(context)
    }

    companion object {
        private const val TAG = "WearableMessageService"

        // 메시지 경로
        const val PATH_START_BREATHING = "/start_breathing"
        const val PATH_HEALTH_DATA = "/health_data"
    }

    /**
     * 연결된 워치에 호흡 운동 시작 메시지 전송
     */
    suspend fun startBreathingOnWatch(): Result<Unit> {
        return try {
            val nodes = getConnectedNodes()
            if (nodes.isEmpty()) {
                Log.w(TAG, "연결된 워치가 없습니다")
                return Result.failure(Exception("연결된 워치가 없습니다"))
            }

            Log.d(TAG, "연결된 노드 수: ${nodes.size}")

            nodes.forEach { node ->
                try {
                    Log.d(TAG, "메시지 전송 시도 - Node: ${node.displayName}, ID: ${node.id}")

                    messageClient.sendMessage(
                        node.id,
                        PATH_START_BREATHING,
                        ByteArray(0) // 빈 데이터
                    ).await()

                    Log.d(TAG, "✅ 호흡 운동 시작 메시지 전송 성공: ${node.displayName}")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 메시지 전송 실패: ${node.displayName}", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "호흡 운동 실행 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 연결된 워치에 건강 데이터 전송
     */
    suspend fun sendHealthDataToWatch(data: String): Result<Unit> {
        return try {
            val nodes = getConnectedNodes()
            if (nodes.isEmpty()) {
                Log.w(TAG, "연결된 워치가 없습니다")
                return Result.failure(Exception("연결된 워치가 없습니다"))
            }

            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(
                        node.id,
                        PATH_HEALTH_DATA,
                        data.toByteArray()
                    ).await()
                    Log.d(TAG, "워치에 건강 데이터 전송: ${node.displayName}")
                } catch (e: Exception) {
                    Log.e(TAG, "건강 데이터 전송 실패: ${node.displayName}", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "건강 데이터 전송 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 연결된 워치 노드 목록 조회
     */
    private suspend fun getConnectedNodes(): List<Node> {
        return try {
            nodeClient.connectedNodes.await()
        } catch (e: Exception) {
            Log.e(TAG, "연결된 노드 조회 실패", e)
            emptyList()
        }
    }

    /**
     * 워치가 연결되어 있는지 확인
     */
    suspend fun isWatchConnected(): Boolean {
        return getConnectedNodes().isNotEmpty()
    }
}

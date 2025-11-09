package com.dito.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dito.app.core.navigation.DitoNavGraph
import androidx.navigation.compose.rememberNavController
import com.dito.app.core.data.RealmRepository
import com.dito.app.core.service.phone.UsageStatsHelper
import dagger.hilt.android.AndroidEntryPoint
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.dito.app.core.background.EventSyncWorker
import com.dito.app.core.navigation.Route
import com.dito.app.core.repository.AuthRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // Splash → Login → (로그인 성공) → Test 화면 순서
                    DitoNavGraph(
                        navController = navController,
                        startDestination = Route.Splash.path
                    )
                }
            }
        }
    }

    fun testRealmData() {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📊 Realm 데이터 확인 시작")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        try {
            val appEvents = RealmRepository.getTodayAppEvents()
            Log.d(TAG, "")
            Log.d(TAG, "📱 오늘 앱 사용 이벤트: ${appEvents.size}개")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            appEvents.take(10).forEachIndexed { index, event ->
                Log.d(TAG, "${index + 1}. ${event.eventType}")
                Log.d(TAG, "   앱: ${event.appName}")
                Log.d(TAG, "   패키지: ${event.packageName}")
                Log.d(TAG, "   시간: ${event.duration / 1000}초")
                Log.d(TAG, "   동기화: ${if (event.synced) "완료" else "대기"}")
                Log.d(TAG, "")
            }

            val mediaEvents = RealmRepository.getTodayMediaEvents()
            Log.d(TAG, "")
            Log.d(TAG, "🎬 오늘 미디어 이벤트: ${mediaEvents.size}개")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            mediaEvents.take(10).forEachIndexed { index, event ->
                Log.d(TAG, "${index + 1}. ${event.eventType}")
                Log.d(TAG, "   제목: ${event.title}")
                Log.d(TAG, "   채널: ${event.channel}")
                Log.d(TAG, "   앱: ${event.appPackage}")
                Log.d(TAG, "   시청: ${event.watchTime / 1000}초")
                Log.d(TAG, "   감지: ${event.detectionMethod}")
                Log.d(TAG, "   동기화: ${if (event.synced) "완료" else "대기"}")
                Log.d(TAG, "")
            }

            val unsyncedApp = RealmRepository.getUnsyncedAppEvents()
            val unsyncedMedia = RealmRepository.getUnsyncedMediaEvents()

            Log.d(TAG, "")
            Log.d(TAG, "🔄 동기화 대기 중")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "   앱 이벤트: ${unsyncedApp.size}개")
            Log.d(TAG, "   미디어 이벤트: ${unsyncedMedia.size}개")
            Log.d(TAG, "   총: ${unsyncedApp.size + unsyncedMedia.size}개")
            Log.d(TAG, "")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "✅ Realm 데이터 확인 완료")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 데이터 확인 실패", e)
        }
    }

    fun clearRealmData() {
        try {
            RealmRepository.clearAll()
            Log.d(TAG, "🗑️ Realm 전체 데이터 삭제 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 삭제 실패", e)
        }
    }

    fun triggerWorkManagerManually() {
        try {
            Log.d(TAG, "🚀 WorkManager 수동 실행 요청")

            val workRequest = OneTimeWorkRequestBuilder<EventSyncWorker>()
                .build()

            WorkManager.getInstance(this)
                .enqueueUniqueWork(
                    "manual_sync",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

            Log.d(TAG, "✅ WorkManager 실행 요청 완료")
            Log.d(TAG, "📊 Logcat에서 'EventSyncWorker' 필터로 결과 확인")

        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager 실행 실패", e)
        }
    }
}


@Composable
fun DitoTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

@Composable
fun PermissionTestScreen() {
    val context = LocalContext.current
    val activity = context as? MainActivity

    NotificationPermissionRequest()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dito 권한 설정",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionCard(
            title = "📊 앱 사용량 권한",
            description = "일일 앱 사용 통계를 확인합니다",
            buttonText = "사용량 권한 설정",
            onClick = {
                if (!UsageStatsHelper.hasUsagePermission(context)) {
                    UsageStatsHelper.openUsagePermissionSettings(context)
                } else {
                    UsageStatsHelper.logDailyUsage(context)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "📱 앱 전환 추적",
            description = "실시간 앱 전환 및 사용 시간을 추적합니다",
            buttonText = "접근성 권한 설정",
            onClick = {
                requestPermission(context, PermissionType.ACCESSIBILITY)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "🎵 미디어 추적",
            description = "YouTube 시청 및 음악 재생을 추적합니다",
            buttonText = "알림 접근 권한 설정",
            onClick = {
                requestPermission(context, PermissionType.NOTIFICATION)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { activity?.testRealmData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📊 Realm 데이터 확인")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { activity?.clearRealmData() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("🗑️ Realm 데이터 삭제")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { activity?.triggerWorkManagerManually() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("🚀 배치 전송 즉시 실행 (API 테스트)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "🔔 테스트 알림",
            description = "앱에서 알림이 정상 작동하는지 확인합니다",
            buttonText = "테스트 알림 보내기",
            onClick = {
                sendTestNotification(context)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

//        PermissionCard(
//            title = "💚 헬스 정보",
//            description = "걸음 수, 심박수, 수면, 이동거리 데이터를 확인합니다",
//            buttonText = "헬스 정보 보기",
//            onClick = onNavigateToHealth
//        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText)
            }
        }
    }
}

enum class PermissionType {
    ACCESSIBILITY,
    NOTIFICATION
}

fun requestPermission(context: Context, type: PermissionType) {
    val intent = when (type) {
        PermissionType.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        PermissionType.NOTIFICATION -> Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }
    context.startActivity(intent)
}

fun sendTestNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "TestChannel"

    val channel = NotificationChannel(channelId, "Test Channel", NotificationManager.IMPORTANCE_HIGH)
    notificationManager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("테스트 알림")
        .setContentText("이 알림이 보이면 알림 권한 및 설정이 정상입니다.")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    notificationManager.notify(1001, notification)
}

@Composable
fun NotificationPermissionRequest() {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> /* 필요 시 후처리 */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DitoTheme {
        Text("Preview")
    }
}
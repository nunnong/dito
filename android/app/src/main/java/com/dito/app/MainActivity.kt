package com.dito.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.dito.app.core.service.UsageStatsHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
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
fun MainScreen() {
    val context = LocalContext.current

    // Android 13 이상 알림 권한 요청
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

        // 테스트 알림 보내기 버튼
        PermissionCard(
            title = "🔔 테스트 알림",
            description = "앱에서 알림이 정상 작동하는지 확인합니다",
            buttonText = "테스트 알림 보내기",
            onClick = {
                sendTestNotification(context)
            }
        )
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Test Channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

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
        onResult = { granted -> /* 권한 승인 여부 후처리 가능 */ }
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
        MainScreen()
    }
}

package com.dito.wear

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val PATH_START_BREATHING = "/start_breathing"
    }

    private lateinit var messageClient: MessageClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MessageClient 초기화
        messageClient = Wearable.getMessageClient(this)

        // 메시지 리스너 등록 (앱이 살아있는 동안 유지)
        messageClient.addListener(this)
        Log.d(TAG, "✅ MessageClient 리스너 등록 (onCreate)")

        setContent {
            WearApp(
                onStartBreathing = {
                    // 호흡 운동 화면으로 이동
                    startActivity(Intent(this, BreathingActivity::class.java))
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 메시지 리스너 해제
        messageClient.removeListener(this)
        Log.d(TAG, "❌ MessageClient 리스너 해제 (onDestroy)")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "📩 메시지 수신: ${messageEvent.path}")

        when (messageEvent.path) {
            PATH_START_BREATHING -> {
                Log.d(TAG, "🌬️ 호흡 운동 시작 요청 받음")
                // 호흡 운동 Activity 실행
                val intent = Intent(this, BreathingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun WearApp(
    onStartBreathing: () -> Unit
) {
    MaterialTheme {
        Scaffold(
            timeText = {
                TimeText()
            }
        ) {
            MainScreen(
                onStartBreathing = onStartBreathing
            )
        }
    }
}

@Composable
fun MainScreen(
    onStartBreathing: () -> Unit
) {
    val context = LocalContext.current

    // 이미지 리소스 확인
    val imageResourceId = context.resources.getIdentifier(
        "breathing_image",
        "drawable",
        context.packageName
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Dito",
                style = MaterialTheme.typography.title2,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // "건강 관리 앱" 텍스트 대신 이미지 표시 (이미지가 있으면)
            if (imageResourceId != 0) {
                Image(
                    painter = painterResource(id = imageResourceId),
                    contentDescription = "건강 관리 앱",
                    modifier = Modifier.size(60.dp)
                )
            } else {
                Text(
                    text = "건강 관리 앱",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Chip(
                onClick = onStartBreathing,
                label = {
                    Text(
                        text = "호흡 운동",
                        style = MaterialTheme.typography.button,
                        textAlign = TextAlign.Center
                    )
                },
                colors = ChipDefaults.primaryChipColors()
            )
        }
    }
}

package com.dito.wear

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.wear.compose.material.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class BreathingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BreathingApp()
        }
    }
}

@Composable
fun BreathingApp() {
    MaterialTheme {
        Scaffold(
            timeText = {
                TimeText()
            }
        ) {
            BreathingScreen()
        }
    }
}

enum class BreathingPhase {
    INHALE,    // 들이마시기
    HOLD,      // 참기
    EXHALE,    // 내쉬기
    REST       // 휴식
}

@Composable
fun BreathingScreen() {
    var isActive by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var countdown by remember { mutableIntStateOf(60) } // 1분 = 60초
    val context = LocalContext.current

    // AudioManager 초기화
    val audioManager = remember {
        context.getSystemService(AudioManager::class.java)
    }

    // MediaPlayer 초기화 (배경음악용)
    val mediaPlayer = remember {
        try {
            val resourceId = context.resources.getIdentifier(
                "breathing_music",
                "raw",
                context.packageName
            )
            android.util.Log.d("BreathingActivity", "음악 리소스 ID: $resourceId")

            if (resourceId != 0) {
                MediaPlayer().apply {
                    // AudioAttributes 설정
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )

                    val afd = context.resources.openRawResourceFd(resourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    prepare()
                    isLooping = true
                    setVolume(1.0f, 1.0f)

                    // 현재 미디어 볼륨 로그
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    android.util.Log.d("BreathingActivity", "MediaPlayer 초기화 성공, 미디어 볼륨: $currentVolume/$maxVolume")
                }
            } else {
                android.util.Log.e("BreathingActivity", "음악 리소스를 찾을 수 없음")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("BreathingActivity", "MediaPlayer 초기화 실패: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // AudioFocusRequest 생성
    val audioFocusRequest = remember {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    // MediaPlayer cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    // 애니메이션 - 원의 크기 변화
    val animatedScale by animateFloatAsState(
        targetValue = when (currentPhase) {
            BreathingPhase.INHALE -> 1.5f
            BreathingPhase.HOLD -> 1.5f
            BreathingPhase.EXHALE -> 0.7f
            BreathingPhase.REST -> 0.7f
        },
        animationSpec = tween(
            durationMillis = when (currentPhase) {
                BreathingPhase.INHALE -> 4000  // 4초
                BreathingPhase.HOLD -> 4000    // 4초
                BreathingPhase.EXHALE -> 4000  // 4초
                BreathingPhase.REST -> 4000    // 4초
            },
            easing = LinearEasing
        ),
        label = "breathing_animation"
    )

    // 호흡 운동 타이머 - 페이즈 전환
    LaunchedEffect(isActive) {
        if (isActive) {
            // 진동 시작 알림
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

            // AudioFocus 요청 및 배경음악 재생
            val focusResult = audioManager.requestAudioFocus(audioFocusRequest)
            android.util.Log.d("BreathingActivity", "AudioFocus 요청 결과: $focusResult")

            if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                android.util.Log.d("BreathingActivity", "음악 재생 시작, mediaPlayer null 여부: ${mediaPlayer == null}")
                mediaPlayer?.let { player ->
                    try {
                        if (!player.isPlaying) {
                            player.start()
                            android.util.Log.d("BreathingActivity", "음악 재생 중")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BreathingActivity", "음악 재생 실패: ${e.message}")
                    }
                } ?: android.util.Log.e("BreathingActivity", "MediaPlayer가 null입니다")
            } else {
                android.util.Log.e("BreathingActivity", "AudioFocus 요청 실패")
            }

            while (countdown > 0 && isActive) {
                // 각 페이즈 전환 (4초씩)
                currentPhase = BreathingPhase.INHALE
                delay(4000)

                if (!isActive) break
                currentPhase = BreathingPhase.HOLD
                delay(4000)

                if (!isActive) break
                currentPhase = BreathingPhase.EXHALE
                delay(4000)

                if (!isActive) break
                currentPhase = BreathingPhase.REST
                delay(4000)
            }

            // 종료 진동
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))

            // 배경음악 정지 및 AudioFocus 해제
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.pause()
                        player.seekTo(0)
                        android.util.Log.d("BreathingActivity", "음악 정지")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BreathingActivity", "음악 정지 실패: ${e.message}")
                }
            }
            audioManager.abandonAudioFocusRequest(audioFocusRequest)

            isActive = false
            countdown = 60
        } else {
            // 중지 버튼을 눌렀을 때도 음악 정지 및 AudioFocus 해제
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.pause()
                        player.seekTo(0)
                        android.util.Log.d("BreathingActivity", "음악 중지 (사용자 중단)")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BreathingActivity", "음악 중지 실패: ${e.message}")
                }
            }
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }

    // 카운트다운 타이머 - 1초마다 감소
    LaunchedEffect(isActive) {
        if (isActive) {
            while (countdown > 0 && isActive) {
                delay(1000) // 1초 대기
                if (isActive) {
                    countdown -= 1
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!isActive) {
            // 시작 화면
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                Text(
                    text = "호흡 운동",
                    style = MaterialTheme.typography.title2,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1분 동안\n편안하게 호흡하세요",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Chip(
                    onClick = { isActive = true },
                    label = {
                        Text(
                            text = "시작",
                            style = MaterialTheme.typography.button
                        )
                    },
                    colors = ChipDefaults.primaryChipColors()
                )

                Spacer(modifier = Modifier.weight(0.5f))
            }
        } else {
            // 호흡 운동 진행 화면
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 배경 비디오 (맨 아래 레이어)
                BackgroundVideo(isPlaying = isActive)

                // 애니메이션 원과 텍스트, 버튼을 모두 중앙에 배치
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 동심원 애니메이션
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val baseColor = when (currentPhase) {
                                BreathingPhase.INHALE -> Color(0xFF4FC3F7)  // 파란색 (들이마시기)
                                BreathingPhase.HOLD -> Color(0xFF66BB6A)     // 초록색 (참기)
                                BreathingPhase.EXHALE -> Color(0xFFFFB74D)   // 주황색 (내쉬기)
                                BreathingPhase.REST -> Color(0xFF9575CD)     // 보라색 (휴식)
                            }

                            // 3개의 동심원으로 퍼지는 효과
                            for (i in 0..2) {
                                val scale = animatedScale - (i * 0.15f)
                                if (scale > 0) {
                                    drawCircle(
                                        color = baseColor,
                                        radius = (size.minDimension / 2) * scale,
                                        alpha = (0.6f - i * 0.2f) * (scale / 1.5f)
                                    )
                                }
                            }
                        }

                        // 원 중앙에 텍스트와 버튼 배치
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 남은 시간
                            Text(
                                text = "${countdown}초",
                                style = MaterialTheme.typography.title3,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 현재 단계 텍스트
                            Text(
                                text = when (currentPhase) {
                                    BreathingPhase.INHALE -> "들이마시기"
                                    BreathingPhase.HOLD -> "참기"
                                    BreathingPhase.EXHALE -> "내쉬기"
                                    BreathingPhase.REST -> "휴식"
                                },
                                style = MaterialTheme.typography.title2,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 중지 버튼 (텍스트 바로 아래)
                            Chip(
                                onClick = {
                                    isActive = false
                                    countdown = 60
                                },
                                label = {
                                    Text(
                                        text = "중지",
                                        style = MaterialTheme.typography.caption1
                                    )
                                },
                                colors = ChipDefaults.secondaryChipColors(),
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun BackgroundVideo(isPlaying: Boolean) {
    val context = LocalContext.current

    // 비디오 리소스 확인
    val videoResourceId = remember {
        context.resources.getIdentifier(
            "breathing_video",
            "raw",
            context.packageName
        )
    }

    // 비디오가 없으면 아무것도 표시하지 않음
    if (videoResourceId == 0) return

    // ExoPlayer 초기화 - 자동 재생 시작
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResourceId")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL // 무한 반복
            volume = 0f // 음소거
            prepare()
            // 바로 재생 시작하여 첫 프레임이 미리 로드되도록 함
            play()
            android.util.Log.d("BackgroundVideo", "ExoPlayer 초기화 및 재생 시작")
        }
    }

    // 재생/일시정지 관리
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
            android.util.Log.d("BackgroundVideo", "비디오 재생")
        } else {
            exoPlayer.pause()
            android.util.Log.d("BackgroundVideo", "비디오 일시정지")
        }
    }

    // ExoPlayer cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // PlayerView를 AndroidView로 래핑
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // 컨트롤러 숨기기
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 비율 유지하며 화면 꽉 채우기
                    // 워치 화면 크기 강제 설정
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .scale(1.15f), // Compose의 scale modifier 사용 - 115% 확대
            update = { playerView ->
                // 레이아웃이 준비되면 강제 갱신
                playerView.post {
                    playerView.requestLayout()
                }
            }
        )
    }
}

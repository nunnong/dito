package com.dito.app.feature.group

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.StrokeText
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun OngoingChallengeScreen(
    viewModel: OngoingChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInfoPanelVisible by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAutoRefresh()
        }
    }

    val rankings = uiState.rankings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.test),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 32.dp)
                    .width(300.dp)
                    .clickable { isInfoPanelVisible = !isInfoPanelVisible },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.challenge),
                    contentDescription = "Challenge Sign",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (isInfoPanelVisible) ColorFilter.tint(
                        Color.Black.copy(alpha = 0.2f),
                        BlendMode.Darken
                    ) else null
                )

                if (isInfoPanelVisible) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "PERIOD : ${uiState.startDate} - ${uiState.endDate}",
                            color = Color.White,
                            style = DitoTypography.bodySmall
                        )
                        Text(
                            "GOAL : ${uiState.goal}",
                            color = Color.White,
                            style = DitoTypography.bodySmall
                        )
                        Text(
                            "PENALTY : ${uiState.penalty}",
                            color = Color.White,
                            style = DitoTypography.bodySmall
                        )
                        Text(
                            "TOTAL BETTING : ${uiState.bet}",
                            color = Color.White,
                            style = DitoTypography.bodySmall
                        )
                    }
                } else {
                    StrokeText(
                        text = uiState.groupName,
                        style = DitoTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        fillColor = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 2.dp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            if (rankings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .offset(y = (-80).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    rankings.take(4).forEach { rankingItem ->
                        val participant =
                            uiState.participants.find { it.userId == rankingItem.userId }
                        val characterName = getCharacterName(participant)

                        CharacterView(
                            characterName = characterName,
                            rank = rankingItem.rank,
                            maxRank = rankings.size.coerceAtMost(4),
                            currentAppPackage = rankingItem.currentAppPackage,
                            onClick = {
                                if (!rankingItem.isMe) {
                                    viewModel.pokeMember(rankingItem.userId)
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

//        // 참여자 4명 리스트
//        if (rankings.isNotEmpty()) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                rankings.take(4).forEach { rankingItem ->
//                    val participant =
//                        uiState.participants.find { it.userId == rankingItem.userId }
//
//                    ParticipantCard(
//                        rank = rankingItem.rank,
//                        nickname = rankingItem.nickname,
//                        profileImage = rankingItem.profileImage,
//                        totalScreenTime = rankingItem.totalScreenTimeFormatted,
//                        avgDailyScreenTime = rankingItem.avgDailyScreenTimeFormatted,
//                        currentAppName = rankingItem.currentAppName,
//                        currentAppPackage = rankingItem.currentAppPackage,
//                        isMe = rankingItem.isMe
//                    )
//                }
//            }
//        }

    }
}

//@Composable
//fun ParticipantCard(
//    rank: Int,
//    nickname: String,
//    profileImage: String?,
//    totalScreenTime: String,
//    avgDailyScreenTime: String,
//    currentAppName: String?,
//    currentAppPackage: String?,
//    isMe: Boolean
//) {
//    Box(
//        modifier = Modifier
//            .width(80.dp)
//            .height(140.dp)
//            .hardShadow(
//                offsetX = 4.dp,
//                offsetY = 4.dp,
//                cornerRadius = 8.dp,
//                color = Color.Black
//            )
//            .background(
//                color = Color.White,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .border(
//                width = 2.dp,
//                color = if (isMe) Primary else Color.Black,
//                shape = RoundedCornerShape(8.dp)
//            )
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // 상단 노란색 헤더
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        color = Primary,
//                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
//                    )
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(20.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "#${'$'}rank",
//                        style = DitoTypography.labelSmall,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                }
//                Spacer(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(1.5.dp)
//                        .background(Color.Black)
//                )
//            }
//
//            // 캐릭터 및 정보
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(vertical = 8.dp, horizontal = 4.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                // 프로필 이미지
//                Box(
//                    modifier = Modifier
//                        .size(55.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.Black),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (profileImage != null) {
//                        coil.compose.AsyncImage(
//                            model = profileImage,
//                            contentDescription = "${'$'}nickname profile",
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.FillBounds
//                        )
//                    } else {
//                        Text(
//                            text = nickname.take(1).uppercase(),
//                            style= DitoTypography.labelLarge,
//                            color = Color.Gray
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                // 닉네임
//                Text(
//                    text = nickname,
//                    style = DitoTypography.labelSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1
//                )
//
//                Spacer(modifier = Modifier.height(2.dp))
//
//                // 스크린타임
//                Text(
//                    text = totalScreenTime,
//                    style = DitoTypography.labelSmall,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1
//                )
//            }
//        }
//    }
//}

/**
 * 앱 패키지 이름에 따라 커스텀 아이콘 리소스를 반환
 */
fun getAppIconResource(packageName: String?): Int {
    return when (packageName) {
        "com.google.android.youtube" -> R.drawable.ic_youtube
        "com.instagram.android" -> R.drawable.ic_instagram
        "com.android.chrome" -> R.drawable.ic_chrome
        "com.twitter.android" -> R.drawable.ic_twitter
        else -> R.drawable.dito
    }
}

@Composable
fun InfoCard(icon: Int, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 2.dp,
                offsetY = 2.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = DitoTypography.bodyLarge,
            color = Color.Black
        )
    }
}

/**
 * 참가자의 장착 아이템에서 캐릭터 이름 추출
 */
fun getCharacterName(participant: com.dito.app.core.data.group.Participant?): String {
    if (participant == null) {
        android.util.Log.d("CharacterDebug", "participant is null")
        return "lemon" // 기본값
    }

    android.util.Log.d(
        "CharacterDebug",
        "participant: ${'$'}{participant.nickname}, equipedItems: ${'$'}{participant.equipedItems.size}"
    )

    // equipedItems에서 type이 "COSTUME"인 아이템 찾기
    val costumeItem = participant.equipedItems.find { it.type == "COSTUME" }
    if (costumeItem != null) {
        android.util.Log.d(
            "CharacterDebug",
            "costumeItem found: name=${'$'}{costumeItem.name}, type=${'$'}{costumeItem.type}"
        )

        val koreanName = costumeItem.name.split(" ").firstOrNull()?.trim() ?: ""
        android.util.Log.d("CharacterDebug", "extracted koreanName: ${'$'}koreanName")

        val englishName = mapKoreanToEnglish(koreanName)
        android.util.Log.d("CharacterDebug", "mapped englishName: ${'$'}englishName")

        return englishName
    } else {
        android.util.Log.d(
            "CharacterDebug",
            "No COSTUME item found, available types: ${'$'}{participant.equipedItems.map { it.type }}"
        )
    }

    return "lemon" // 기본값
}

/**
 * 한글 캐릭터 이름을 영어로 매핑
 */
fun mapKoreanToEnglish(koreanName: String): String {
    return when (koreanName) {
        "레몬" -> "lemon"
        "포도" -> "grape"
        "메론" -> "melon"
        "토마토" -> "tomato"
        else -> "lemon" // 기본값
    }
}

@Composable
fun CharacterView(
    characterName: String,
    rank: Int,
    maxRank: Int,
    currentAppPackage: String?,
    onClick: () -> Unit = {}
) {
    // 순위에 따른 높이 계산 (1등이 가장 높음)
    // 1등: 120dp, 2등: 100dp, 3등: 80dp, 4등: 60dp
    val baseHeight = 120.dp
    val heightReduction = (rank - 1) * 20.dp
    val characterHeight = baseHeight - heightReduction

    // 이전 순위 저장
    val previousRank = remember { mutableStateOf(rank) }

    // 애니메이션 상태
    val isAnimating = remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }

    // 지속적인 부드러운 움직임 애니메이션 (항상 실행)
    val infiniteTransition = rememberInfiniteTransition(label = "character_idle")
    val idleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_animation"
    )

    // 순위 변동 감지
    androidx.compose.runtime.LaunchedEffect(rank) {
        if (previousRank.value != rank) {
            android.util.Log.d(
                "CharacterAnim",
                "Rank changed: ${'$'}{previousRank.value} -> ${'$'}rank"
            )
            isAnimating.value = true

            // 애니메이션 실행 (1초 동안 6번 꿈틀)
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 6f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )

            isAnimating.value = false
            previousRank.value = rank
        }
    }

    // 애니메이션 중일 때만 이미지 변경
    val animPhase = if (isAnimating.value) {
        (animationProgress.value % 1f)
    } else {
        0f
    }

    val showRight = isAnimating.value && animPhase >= 0.5f

    val characterDrawable = when (characterName) {
        "lemon" -> if (showRight) R.drawable.lemon_right else R.drawable.lemon_left
        "grape" -> if (showRight) R.drawable.grape_right else R.drawable.grape_left
        "melon" -> if (showRight) R.drawable.melon_right else R.drawable.melon_left
        "tomato" -> if (showRight) R.drawable.tomato_right else R.drawable.tomato_left
        else -> if (showRight) R.drawable.lemon_right else R.drawable.lemon_left
    }

    // 위아래로 움직이는 애니메이션
    val verticalOffset = if (isAnimating.value) {
        // 순위 변경 시 큰 움직임
        val bounce = kotlin.math.sin(animPhase * Math.PI).toFloat()
        bounce * 12.dp
    } else {
        // 평상시 부드러운 움직임
        val idleBounce = kotlin.math.sin(idleAnimation * 2 * Math.PI).toFloat()
        idleBounce * 5.dp
    }

    Column(
        modifier = Modifier
            .width(60.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 캐릭터
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(characterHeight + 20.dp), // 애니메이션 공간 확보
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = characterDrawable),
                contentDescription = "${'$'}characterName character",
                modifier = Modifier
                    .size(60.dp)
                    .offset(y = -verticalOffset),
                contentScale = ContentScale.Fit
            )
        }

        // 사용중인 앱 아이콘 (캐릭터와 겹치도록 위로 올림)
        if (currentAppPackage != null) {
            Image(
                painter = painterResource(id = getAppIconResource(currentAppPackage)),
                contentDescription = "Current app",
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = (-10).dp) // 캐릭터와 겹치도록 위로 10dp 올림
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            )
        } else {
            // 앱이 없을 때 빈 공간 유지
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}




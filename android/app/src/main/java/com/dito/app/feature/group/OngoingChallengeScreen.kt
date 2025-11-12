package com.dito.app.feature.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.hardShadow
import com.dito.app.core.ui.util.rememberLifecycleEvent
import kotlinx.coroutines.launch

@Composable
fun OngoingChallengeScreen(
    viewModel: GroupChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 화면 활성화 시 즉시 조회 + 10초마다 자동 갱신
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 화면 진입 시 즉시 한 번 조회
                viewModel.loadRanking()
                android.util.Log.d("OngoingChallenge", "🎬 화면 진입 - 즉시 랭킹 조회")
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // 10초마다 자동 갱신
        val autoRefreshJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            while (true) {
                kotlinx.coroutines.delay(10 * 1000L)

                // 화면이 활성화 상태일 때만 갱신
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    viewModel.loadRanking()
                    android.util.Log.d("OngoingChallenge", "🔄 자동 갱신 (10초 주기)")
                }
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            autoRefreshJob.cancel()
            android.util.Log.d("OngoingChallenge", "🛑 자동 갱신 중단")
        }
    }

    val groupInfo = uiState.groupInfo
    val rankings = uiState.rankings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9D9D9))
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.race),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.8f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
        ) {

            // 챌린지 제목
            Text(
                text = groupInfo?.groupName ?: uiState.groupName,
                style = DitoTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // 진행 상황 바
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val daysElapsed = groupInfo?.daysElapsed ?: 0
                val daysTotal = groupInfo?.daysTotal ?: uiState.period

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (daysTotal > 0) daysElapsed.toFloat() / daysTotal.toFloat() else 0f)
                            .fillMaxHeight()
                            .background(Color.Black)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${daysElapsed}일 / ${daysTotal}일",
                    style = DitoCustomTextStyles.titleKSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Betting 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalBet = groupInfo?.totalBetCoins ?: uiState.bet
                Text(
                    text = "Betting : $totalBet",
                    style = DitoCustomTextStyles.titleDLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 랭킹 카드들 (동적으로 생성)
            if (rankings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    rankings.take(4).forEach { rankingItem ->
                        val participant =
                            uiState.participants.find { it.userId == rankingItem.userId }
                        val backgroundImgUrl =
                            participant?.equipedItems?.find { it.type == "background" }?.imgUrl
                        val costumeImgUrl =
                            participant?.equipedItems?.find { it.type == "costume" }?.imgUrl

                        val backgroundColor = when (rankingItem.rank) {
                            1 -> Color(0xFFFDD835) // 1등: 노란색
                            2, 3 -> Color.White // 2, 3등: 흰색
                            else -> Color(0xFFFF5722) // 4등 이상: 빨간색
                        }
                        val height = when (rankingItem.rank) {
                            1 -> 200.dp
                            2 -> 180.dp
                            3 -> 160.dp
                            else -> 140.dp
                        }

                        val isFirst = rankingItem.rank == 1
                        val isLast =
                            rankingItem.rank == uiState.participants.size && uiState.participants.size > 1

                        RankCard(
                            rank = rankingItem.rank.toString(),
                            name = rankingItem.nickname,
                            time = rankingItem.totalScreenTimeFormatted,
                            backgroundColor = backgroundColor,
                            height = height,
                            backgroundImgUrl = backgroundImgUrl,
                            costumeImgUrl = costumeImgUrl,
                            isFirst = isFirst,
                            isLast = isLast,
                            currentAppPackage = rankingItem.currentAppPackage
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RACE INFO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val startDate = groupInfo?.startDate ?: uiState.startDate
                val endDate = groupInfo?.endDate ?: uiState.endDate
                val penalty = groupInfo?.penaltyDescription ?: uiState.penalty
                val goal = groupInfo?.goalDescription ?: uiState.goal

                // Period
                InfoCard(
                    icon = R.drawable.period,
                    title = "PERIOD",
                    value = "$startDate - $endDate"
                )
                // Penalty
                InfoCard(icon = R.drawable.penalty, title = "PENALTY", value = penalty)
                // Goal
                InfoCard(icon = R.drawable.goal, title = "GOAL", value = goal)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 앱 패키지 이름에 따라 커스텀 아이콘 리소스를 반환
 */
fun getAppIconResource(packageName: String?): Int {
    return when (packageName) {
        "com.google.android.youtube" -> R.drawable.ic_youtube
        "com.instagram.android" -> R.drawable.ic_instagram
        "com.android.chrome" -> R.drawable.ic_chrome
        "com.twitter.android" -> R.drawable.ic_twitter
        else -> R.drawable.ic_default_app
    }
}

@Composable
fun InfoCard(icon: Int, title: String, value: String) {
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
            contentDescription = title,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$title : $value",
            style = DitoTypography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun RankCard(
    rank: String,
    name: String,
    time: String,
    backgroundColor: Color,
    height: Dp,
    backgroundImgUrl: String?,
    costumeImgUrl: String?,
    isFirst: Boolean,
    isLast: Boolean,
    currentAppPackage: String? = null
) {
    Box(contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 누적 시간
            Text(
                text = time,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 순위
            Text(
                text = rank,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 캐릭터 이미지 + 현재 앱 아이콘
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 캐릭터 이미지
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 배경 이미지 (먼저 그려짐)
                    if (backgroundImgUrl != null) {
                        coil.compose.AsyncImage(
                            model = backgroundImgUrl,
                            contentDescription = "$name background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    // 의상 이미지 (배경 위에 그려짐)
                    if (costumeImgUrl != null) {
                        coil.compose.AsyncImage(
                            model = costumeImgUrl,
                            contentDescription = "$name costume",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                // 현재 사용 중인 앱 아이콘 (캐릭터 옆에 표시)
                if (currentAppPackage != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = getAppIconResource(currentAppPackage)),
                        contentDescription = "Current App",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            // 이름
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        if (isFirst) {
            Image(
                painter = painterResource(id = R.drawable.crown),
                contentDescription = "Crown",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
            )
        }

        if (isLast) {
            Image(
                painter = painterResource(id = R.drawable.hammer),
                contentDescription = "Hammer",
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = 20.dp)
                    .rotate(-30f)
            )
        }
    }
}

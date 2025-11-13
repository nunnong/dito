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
                // 화면 진입 시 그룹 상세 정보 조회 (GroupManager에 저장)
                viewModel.refreshGroupInfo()
                android.util.Log.d("OngoingChallenge", "🎬 화면 진입 - 그룹 상세 정보 조회")

                // 화면 진입 시 즉시 한 번 랭킹 조회
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
                    android.util.Log.d("OngoingChallenge", "자동 갱신 (10초 주기)")
                }
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            autoRefreshJob.cancel()
            android.util.Log.d("OngoingChallenge", "자동 갱신 중단")
        }
    }

    val rankings = uiState.rankings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 레몬나무 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.lemontree),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(615.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 참여자 4명 리스트 (랭킹 API에서 가져온 데이터)
        if (rankings.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rankings.take(4).forEach { rankingItem ->
                    val participant =
                        uiState.participants.find { it.userId == rankingItem.userId }

                    ParticipantCard(
                        rank = rankingItem.rank,
                        nickname = rankingItem.nickname,
                        profileImage = rankingItem.profileImage,
                        totalScreenTime = rankingItem.totalScreenTimeFormatted,
                        avgDailyScreenTime = rankingItem.avgDailyScreenTimeFormatted,
                        currentAppName = rankingItem.currentAppName,
                        currentAppPackage = rankingItem.currentAppPackage,
                        isMe = rankingItem.isMe
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 그룹 정보 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // 챌린지 제목
            Text(
                text = uiState.groupName,
                style = DitoTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            // Betting 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Betting : ${uiState.bet}",
                    style = DitoCustomTextStyles.titleDLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 챌린지 정보 카드들
            InfoCard(
                icon = R.drawable.period,
                title = "PERIOD",
                value = "${uiState.startDate} - ${uiState.endDate}"
            )

            Spacer(Modifier.height(12.dp))

            InfoCard(
                icon = R.drawable.goal,
                title = "GOAL",
                value = uiState.goal
            )

            Spacer(Modifier.height(12.dp))

            InfoCard(
                icon = R.drawable.penalty,
                title = "PENALTY",
                value = uiState.penalty
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ParticipantCard(
    rank: Int,
    nickname: String,
    profileImage: String?,
    totalScreenTime: String,
    avgDailyScreenTime: String,
    currentAppName: String?,
    currentAppPackage: String?,
    isMe: Boolean
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .border(
                width = if (isMe) 3.dp else 2.dp,
                color = if (isMe) Color(0xFFFDD835) else Color.Black,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 순위 배지
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    when (rank) {
                        1 -> Color(0xFFFDD835) // 금색
                        2 -> Color(0xFFC0C0C0) // 은색
                        3 -> Color(0xFFCD7F32) // 동색
                        else -> Color(0xFFFF5722) // 빨간색
                    },
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(24.dp))
                .background(Color(0xFFE0E0E0), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (profileImage != null) {
                coil.compose.AsyncImage(
                    model = profileImage,
                    contentDescription = "$nickname profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // 기본 프로필 아이콘
                Text(
                    text = nickname.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        // 닉네임
        Text(
            text = nickname,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // 누적 시간
        Text(
            text = totalScreenTime,
            fontSize = 9.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        // 현재 사용 중인 앱 아이콘
        if (currentAppPackage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = getAppIconResource(currentAppPackage)),
                    contentDescription = "Current App",
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                if (currentAppName != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = currentAppName,
                        fontSize = 8.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
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


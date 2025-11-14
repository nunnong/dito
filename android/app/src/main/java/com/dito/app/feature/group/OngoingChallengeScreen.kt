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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.dito.app.R
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.hardShadow
import kotlinx.coroutines.launch

@Composable
fun OngoingChallengeScreen(
    viewModel: GroupChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // 화면 진입 시 즉시 조회
    androidx.compose.runtime.LaunchedEffect(Unit) {
        android.util.Log.d("OngoingChallenge", "OngoingChallengeScreen LaunchedEffect 시작")
        viewModel.refreshGroupInfo()
        viewModel.loadRanking()
        android.util.Log.d("OngoingChallenge", "초기 데이터 로드 완료")
    }

    // 10초마다 자동 갱신
    androidx.compose.runtime.LaunchedEffect(Unit) {
        android.util.Log.d("OngoingChallenge", "자동 갱신 폴링 LaunchedEffect 시작")
        while (true) {
            kotlinx.coroutines.delay(10 * 1000L)
            android.util.Log.d("OngoingChallenge", "폴링 시도 - 현재 상태: ${lifecycleOwner.lifecycle.currentState}")

            // 화면이 활성화 상태일 때만 갱신
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                viewModel.loadRanking()
                android.util.Log.d("OngoingChallenge", "자동 갱신 실행 (10초 주기)")
            } else {
                android.util.Log.d("OngoingChallenge", "화면 비활성화 상태 - 갱신 스킵")
            }
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
                .height(510.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))


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
                    text = "총 배팅 : ${uiState.bet}",
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
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(140.dp)
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (isMe) Primary else Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 노란색 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        style = DitoTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color.Black)
                )
            }

            // 캐릭터 및 정보
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImage != null) {
                        coil.compose.AsyncImage(
                            model = profileImage,
                            contentDescription = "$nickname profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        Text(
                            text = nickname.take(1).uppercase(),
                            style= DitoTypography.labelLarge,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 닉네임
                Text(
                    text = nickname,
                    style = DitoTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 스크린타임
                Text(
                    text = totalScreenTime,
                    style = DitoTypography.labelSmall,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
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
        else -> R.drawable.dito
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
            color = Color.Black
        )
    }
}


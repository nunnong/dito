package com.dito.app.feature.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.data.group.Participant
import com.dito.app.core.ui.designsystem.*
import kotlinx.coroutines.delay

@Composable
fun GroupWaitingScreen(
    viewModel: GroupChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 초기 그룹 정보 로드
    LaunchedEffect(Unit) {
        viewModel.refreshGroupInfo()
    }

    // 1초마다 참가자 목록 폴링 (PENDING 상태일 때만)
    LaunchedEffect(uiState.challengeStatus) {
        if (uiState.challengeStatus == ChallengeStatus.PENDING) {
            while (true) {
                delay(1000L) // 1초 대기

                // 화면이 활성화 상태일 때만 갱신
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    viewModel.refreshGroupInfo()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1) 전체 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.group_waiting_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2) 배경 위에 깔리는 큰 나무 이미지 (group_tree)
        Image(
            painter = painterResource(id = R.drawable.group_tree),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(396.dp)
                .height(396.dp)
                .offset(y = 90.dp),
            contentScale = ContentScale.Fit
        )

        // 메인 컨테이너
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.offset(y = (-50).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 나무 간판 + 그룹 이름
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_panel),
                        contentDescription = "Group Panel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // 간판 안 그룹명
                    StrokeText(
                        text = uiState.groupName,
                        style = DitoTypography.headlineMedium,
                        fillColor = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .width(180.dp)
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp)
                            .offset(y = 42.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // 참여코드 박스
                Row(
                    modifier = Modifier
                        .width(262.dp)
                        .height(48.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "참여코드 : ${uiState.entryCode}",
                        style = DitoCustomTextStyles.titleDLarge,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BounceClickable(
                        onClick = {
                            playPopSound(context)
                            // 클립보드 복사
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("참여코드", uiState.entryCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "참여코드가 복사되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    ) { isPressed ->
                        Image(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = "Copy Invite Code",
                            modifier = Modifier.size(24.dp),
                            alpha = if (isPressed) 0.7f else 1f,
                            colorFilter = ColorFilter.tint(OnSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 멤버 슬롯 4개
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // 방장이 왼쪽, 그 다음 들어온 순서대로 정렬 (userId 기준)
                val sortedParticipants = uiState.participants.sortedWith(
                    compareBy(
                        { it.role != "host" }, // host를 먼저
                        { it.userId }           // 그 다음 userId 오름차순 (작을수록 먼저 들어온 사람)
                    )
                )
                val baseMembers = sortedParticipants.take(4)
                val placeholderCount = (4 - baseMembers.size).coerceAtLeast(0)
                val displayMembers = baseMembers + List(placeholderCount) {
                    Participant(
                        userId = 0L,
                        nickname = "waiting...",
                        role = "member",
                        betAmount = 0,
                        equipedItems = emptyList()
                    )
                }

                displayMembers.forEach { member ->
                    MemberSlot(
                        member = member,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 하단 버튼 (방장/팀원 분기 처리)
            if (uiState.isLeader) {
                // 방장일 경우: START 버튼
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(52.dp)
                        .hardShadow(
                            DitoHardShadow.ButtonLarge.copy(
                                cornerRadius = 8.dp
                            )
                        )
                        .background(Primary, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clickable { viewModel.onChallengeStarted() }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START!",
                        style = DitoCustomTextStyles.titleDLarge,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 팀원일 경우: 대기중 텍스트
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(52.dp)
                        .hardShadow(
                            DitoHardShadow.ButtonLarge.copy(
                                cornerRadius = 8.dp
                            )
                        )
                        .background(Primary, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "방장의 시작을 기다리고 있어요!",
                        style = DitoCustomTextStyles.titleDMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MemberSlot(
    member: Participant,
    modifier: Modifier = Modifier
) {
    val isWaiting = member.nickname == "waiting..."
    val costumeUrl = member.equipedItems.find { it.type == "costume" }?.imgUrl

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 캐릭터 + 의자 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 의자(그루터기)
            Image(
                painter = painterResource(id = R.drawable.group_chair),
                contentDescription = "Chair",
                modifier = Modifier
                    .width(96.dp)
                    .height(80.dp)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Fit
            )

            // 캐릭터
            if (!isWaiting) {
                val characterModifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-50).dp)

                if (!costumeUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = costumeUrl,
                        contentDescription = "Character",
                        modifier = characterModifier,
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.dito_tmp),
                        contentDescription = "Character",
                        modifier = characterModifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // 닉네임 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .hardShadow(
                    DitoHardShadow.ButtonSmall.copy(
                        cornerRadius = 4.dp
                    )
                )
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isWaiting) {
                Image(
                    painter = painterResource(id = R.drawable.loading),
                    contentDescription = "Waiting",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = member.nickname,
                    style = DitoTypography.labelSmall,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
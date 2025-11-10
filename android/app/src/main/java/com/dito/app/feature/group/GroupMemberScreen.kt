package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing.m
import com.dito.app.core.ui.designsystem.Spacing.s
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun GroupMemberScreen(
    groupName: String = "",
    entryCode: String = "",
    period: Int = 0,
    goal: String = "",
    penalty: String = "",
    participants: List<com.dito.app.core.data.group.Participant> = emptyList(),
    onLoadParticipants: () -> Unit = {}
) {
    val participantCount = participants.size

    // 화면 진입 시 참여자 목록 조회 + 5초마다 polling
    androidx.compose.runtime.LaunchedEffect(Unit) {
        onLoadParticipants() // 초기 로드
        while (true) {
            kotlinx.coroutines.delay(5000L) // 5초 대기
            onLoadParticipants() // 갱신
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Image(
            painter = painterResource(id = R.drawable.waitingroom),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alpha = 0.8f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(90.dp))

            // 입장코드 박스
            Box(
                modifier = Modifier
                    .hardShadow(
                        offsetX = 4.dp,
                        offsetY = 4.dp,
                        cornerRadius = s,
                        color = Color.Black
                    )
                    .clip(DitoShapes.small)
                    .border(1.dp, Color.Black, DitoShapes.small)
                    .background(Color.White)
                    .padding(vertical = 12.dp, horizontal = m),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "입장코드:",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleKMedium
                    )
                    Text(
                        text = entryCode.ifEmpty { "-" },
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleKMedium
                    )
                    Image(
                        painter = painterResource(id = R.drawable.copy),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(16.dp),
                    )

                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // START 버튼 (위치 고정)
            Box(
                modifier = Modifier
                    .width(270.dp)
                    .height(80.dp)
                    .clip(DitoShapes.small)
                    .border(1.dp, Color.Black, DitoShapes.small)
                    .background(Color(0xFFE9E5D6))
                    .padding(s),
                contentAlignment = Alignment.Center
            ) {
                Row (){
                    Image(
                        painter = painterResource(id = R.drawable.blinker),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(end = m)
                    )

                    Text(
                        text = "챌린지 시작을 \n 기다리는 중입니다.",
                        style = DitoCustomTextStyles.titleKMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }

            Spacer(modifier = Modifier.height(48.dp))

            // 참가자 목록
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 참가자 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(0.5.dp, Color.Black, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dito),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "뛰콩",
                        style = DitoTypography.labelMedium,
                        color = Color.Black
                    )
                }

                // 참가자 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dito),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "뒤콩",
                        style = DitoTypography.labelMedium,
                        color = Color.Black
                    )
                }

                // 참가자 3
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dito),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "뒤콩",
                        style = DitoTypography.labelMedium,
                        color = Color.Black
                    )
                }

                // 참가자 4
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dito),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "뛰콩",
                        style = DitoTypography.labelMedium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 챌린지 정보 박스
            Box(
                modifier = Modifier
                    .width(270.dp)
                    .height(120.dp)
                    .hardShadow(
                        offsetX = 4.dp,
                        offsetY = 4.dp,
                        cornerRadius = s,
                        color = Color.Black
                    )
                    .clip(DitoShapes.small)
                    .border(1.dp, Color.Black, DitoShapes.small)
                    .background(Color.White)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Primary)
                            .padding(vertical = 8.dp, horizontal = m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "팀 이름",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )
                    }
                    // 정보 내용
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = m, vertical = m)
                    ) {
                        Text(
                            text = "PERIOD : {}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )

                        Text(
                            text = "GOAL : {}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )

                        Text(
                            text = "PENALTY : {}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )

                        Text(
                            text = "현재 참여 인원 : {}명",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )
                    }
                }
            }
        }
    }
}

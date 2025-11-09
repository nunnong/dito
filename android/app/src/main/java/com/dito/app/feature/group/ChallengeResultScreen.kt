package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.ui.designsystem.Spacing.l
import com.dito.app.core.ui.designsystem.Spacing.m
import com.dito.app.core.ui.designsystem.Spacing.s
import com.dito.app.core.ui.designsystem.Spacing.xl
import com.dito.app.core.ui.designsystem.Spacing.xs

@Preview(showBackground = true)
@Composable
fun ChallengeResultScreen(
    onNavigateToTab: (BottomTab) -> Unit = {},
    onClose: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            DitoBottomAppBar(
                selectedTab = BottomTab.GROUP,
                onTabSelected = onNavigateToTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = l)
        ) {
            // 닫기 버튼
            Image(
                painter = painterResource(R.drawable.close),
                contentDescription = "닫기",
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.End)
                    .clickable { onClose() }
                    .padding(top = m)
            )

            Spacer(modifier = Modifier.height(l))

            // 제목
            Text(
                text = "ㅇㅇ팀의\n챌린지가 종료되었습니다.",
                style = DitoCustomTextStyles.titleKLarge,
                color = OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(m))

            // 기간
            Text(
                text = "2025.11.02 - 2025.11.16",
                style = DitoCustomTextStyles.titleKSmall,
                color = OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(m))

            // 배팅 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Betting : ",
                    style = DitoCustomTextStyles.titleDLarge,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.width(xs))

                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = "레몬",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(xl))

            // 랭킹 리스트
            RankingItem(rank = 1, nickname = "뛰콩", time = "50h 01m")
            RankingItem(rank = 2, nickname = "디토", time = "48h 30m")
            RankingItem(rank = 3, nickname = "멜론", time = "45h 15m")
            RankingItem(rank = 4, nickname = "뛰콩 동생", time = "40h 20m")

            Spacer(modifier = Modifier.height(xl))

            // 가장 많이 사용한 앱
            MostUsedAppSection()

            Spacer(modifier = Modifier.height(xl))

            // 벌칙 카드
            PenaltyCardSection()

            Spacer(modifier = Modifier.height(xl))

            // 저장 버튼
            SaveButton()

            Spacer(modifier = Modifier.height(l))
        }
    }
}

@Composable
fun RankingItem(
    rank: Int,
    nickname: String,
    time: String
) {
    val rankText = when (rank) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${rank}th"
    }

    val backgroundColor = when (rank) {
        1 -> Primary
        else -> Color.White
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = m)
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .clip(DitoShapes.small)
            .border(1.dp, Color.Black, DitoShapes.small)
            .background(backgroundColor)
            .padding(vertical = m, horizontal = l)
    ) {
        // 순위
        Text(
            text = rankText,
            style = DitoCustomTextStyles.titleDLarge,
            color = if (rank == 1) OnPrimary else OnSurface,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(l))

        // 프로필 이미지
        Image(
            painter = painterResource(R.drawable.dito),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(m))

        // 닉네임과 시간
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nickname,
                style = DitoTypography.bodyLarge,
                color = if (rank == 1) OnPrimary else OnSurface
            )
            Spacer(modifier = Modifier.height(xs))
            Text(
                text = time,
                style = DitoTypography.labelMedium,
                color = if (rank == 1) OnPrimary else OnSurfaceVariant
            )
        }
    }
}

@Composable
fun SaveButton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .hardShadow(
                    offsetX = 4.dp,
                    offsetY = 4.dp,
                    cornerRadius = 8.dp,
                    color = Color.Black
                )
                .clip(DitoShapes.small)
                .border(1.dp, Color.Black, DitoShapes.small)
                .background(Color.White)
                .clickable { /* TODO: 저장 로직 */ }
                .padding(vertical = m, horizontal = xl),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "기록 저장하기",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnSurface
            )
        }
    }
}

@Composable
fun PenaltyCardSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 12.dp,
                color = Color.Black
            )
            .clip(DitoShapes.medium)
            .border(2.dp, Color.Black, DitoShapes.medium)
            .background(Color.White)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary)
                    .padding(vertical = m)
            ) {
                Text(
                    text = "벌칙대상자 : 뛰콩 동생",
                    style = DitoCustomTextStyles.titleKMedium,
                    color = OnPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(xl))

            // 디토 + 망치 이미지
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Image(
                    painter = painterResource(R.drawable.hammer),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 30.dp, y = (-20).dp)
                )
            }

            Spacer(modifier = Modifier.height(l))

            // 벌칙 내용
            Text(
                text = "벌칙 : 바나프레소 쏘기",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(xl))
        }
    }
}

@Composable
fun MostUsedAppSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            color = Outline,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(l))

        // 앱 아이콘과 정보
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.phone),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(m))

            Column {
                Text(
                    text = ": YOUTUBE",
                    style = DitoCustomTextStyles.titleKMedium,
                    color = OnSurface
                )
                Text(
                    text = "(128H 30M)",
                    style = DitoCustomTextStyles.titleKSmall,
                    color = OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(s))

        Text(
            text = "가장 많이 사용한 앱",
            style = DitoTypography.labelMedium,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(l))

        HorizontalDivider(
            color = Outline,
            thickness = 1.dp
        )
    }
}

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography

@Preview(showBackground = true)
@Composable
fun OngoingChallengeScreen(
    onNavigateToTab: (BottomTab) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {

        // RACE IN PROGRESS 배너
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFDD835))
                .border(2.dp, Color.Black)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "RACE IN PROGRESS!",
                style = DitoTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        // 챌린지 제목
        Text(
            text = "취업하기",
            style = DitoTypography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.43f) // 3/7 = 약 43%
                        .fillMaxHeight()
                        .background(Color.Black)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "3일 / 7일",
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
            Text(
                text = "Betting : 300",
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

        // 랭킹 카드들 (높낮이 다르게)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2위 카드
            RankCard(
                rank = "2",
                name = "뛰콩",
                time = "59M",
                backgroundColor = Color.White,
                height = 180.dp
            )

            // 1위 카드 (가장 높음)
            RankCard(
                rank = "1",
                name = "삭병장",
                time = "51M",
                backgroundColor = Color(0xFFFDD835),
                height = 200.dp
            )

            // 3위 카드
            RankCard(
                rank = "3",
                name = "나연",
                time = "1H 10M",
                backgroundColor = Color.White,
                height = 160.dp
            )

            // 4위 카드
            RankCard(
                rank = "4",
                name = "뛰콩동생",
                time = "1H 29M",
                backgroundColor = Color(0xFFFF5722),
                height = 140.dp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // RACE INFO 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.raceinfo),
                contentDescription = "Race Info Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(20.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " RACE INFO",
                        style = DitoTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "PERIOD : 25.10.29 - 25.11.05",
                            style = DitoTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "PENALTY : 바나프레소 쓰기",
                            style = DitoTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RankCard(
    rank: String,
    name: String,
    time: String,
    backgroundColor: Color,
    height: Dp
) {
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
        // 순위
        Text(
            text = rank,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // 캐릭터 이미지
        Image(
            painter = painterResource(R.drawable.dito),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(4.dp)
        )

        // 이름과 시간
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = time,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

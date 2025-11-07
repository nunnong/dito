package com.dito.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.background.ScreenTimeSyncWorker
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onCartClick: () -> Unit,
    onClosetClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    val context = LocalContext.current
    var weeklyGoal by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
            // Frame 156: 메인 노란색 카드 (359x589)
            Column(
                modifier = Modifier
                    .width(359.dp)
                    .height(589.dp)
                    .hardShadow(
                        DitoHardShadow.Modal.copy(
                            cornerRadius = 0.dp,
                            offsetX = 6.dp,
                            offsetY = 6.dp
                        )
                    )
                    .background(Primary, RectangleShape)
                    .border(2.dp, Color.Black, RectangleShape)
                    .padding(top = 25.dp, bottom = 25.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Frame 159: 흰색 내부 카드 (327x477)
                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .height(477.dp)
                        .background(Color.White)
                        .border(2.dp, Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단 블랙 바 (Frame 165)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.Black)
                            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽 아이콘 2개 (Frame 164)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .width(80.dp)
                                .height(24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cart),
                                contentDescription = "Cart",
                                modifier = Modifier.size(24.dp).clickable { onCartClick() },
                                contentScale = ContentScale.Fit
                            )
                            Image(
                                painter = painterResource(id = R.drawable.closet),
                                contentDescription = "Closet",
                                modifier = Modifier.size(20.dp).clickable { onClosetClick() },
                                contentScale = ContentScale.Fit
                            )
                        }

                        // 오른쪽 아이콘 1개
                        Image(
                            painter = painterResource(id = R.drawable.mail_home),
                            contentDescription = "Mail",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Frame 162 - 내부 컨텐츠
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 주간 목표 입력 필드 (Frame 177)
                        Row(
                            modifier = Modifier
                                .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 4.dp))
                                .width(261.dp)
                                .height(52.dp)
                                .background(Color.White)
                                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (weeklyGoal.isEmpty()) "주간 목표를 입력해주세요!" else weeklyGoal,
                                style = DitoCustomTextStyles.titleDSmall, // 14sp
                                color = if (weeklyGoal.isEmpty()) Color(0xFFBDBDBD) else Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier.size(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // 연필 아이콘 (Vector들로 구성)
                                Image(
                                    painter = painterResource(id = R.drawable.pencil),
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(20.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Increased space

                        // 캐릭터 이미지
                        Image(
                            painter = painterResource(id = R.drawable.dito),
                            contentDescription = "Character",
                            modifier = Modifier.size(110.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(8.dp)) // Original space

                        // 코인 표시
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 코인 박스
                            Row(
                                modifier = Modifier
                                    .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 48.dp))
                                    .width(97.dp)
                                    .height(36.dp)
                                    .background(Color.White, RoundedCornerShape(48.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(48.dp))
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "100",
                                    style = DitoCustomTextStyles.titleDLarge, // 22sp
                                    color = Color.Black
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.lemon),
                                    contentDescription = "Coin",
                                    modifier = Modifier.size(28.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // 프로그레스 바 섹션
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(191.dp)
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // border-top
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center // Center the content vertically
                        ) {
                            // Add a nested column to group the items with their own spacing
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProgressBarItem(label = "자기관리", progress = 0.7f)
                                ProgressBarItem(label = "집중", progress = 0.7f)
                                ProgressBarItem(label = "수면", progress = 0.7f)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 하단 닉네임
                Row(
                    modifier = Modifier
                        .width(310.dp)
                        .height(52.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 닉네임 영역
                    Column(
                        modifier = Modifier
                            .width(252.dp)
                            .height(52.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "낙동강오리알",
                            style = DitoTypography.headlineMedium, // 28sp
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // border-bottom
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Black)
                        )
                    }

                    // 원형 꾸밈
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(Surface, CircleShape)
                            .border(3.dp, Color.Black, CircleShape)
                    )
                }
            }
        }
    }

@Composable
private fun ProgressBarItem(label: String, progress: Float) {
    // Frame 172/177/178
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.67.dp)
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Frame 173
        Row(
            modifier = Modifier
                .width(80.dp)
                .height(52.dp)
                .padding(top = 14.dp, bottom = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = DitoCustomTextStyles.titleKMedium, // 16sp Bold
                color = Color.White
            )
        }

        // Frame 174 - 프로그레스 바
        Box(
            modifier = Modifier
                .width(171.dp)
                .height(24.dp)
                .border(1.dp, Color.White, RectangleShape),
            contentAlignment = Alignment.CenterStart // Align content to center start
        ) {
            // 눈금 선들 (0, 10, 20, ..., 100)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(11) { index ->
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.5f))
                    )
                }
            }

            // Line 1 - 프로그레스 (노란색)
            Box(
                modifier = Modifier
                    .height(12.dp) // Explicitly set height
                    .fillMaxWidth(), // Fill width of parent
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Fill the 12.dp height and full width of its parent
                        .fillMaxWidth(progress)
                        .background(Primary, RectangleShape)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Home Screen", style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 테스트용: 스크린타임 즉시 동기화
                    Button(onClick = {
                        ScreenTimeSyncWorker.triggerImmediateSync(context)
                    }) {
                        Text("📊 스크린타임 동기화 (테스트)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = onLogout) {
                        Text("Logout (Temporary)")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onLogout = {}, onCartClick = {}, onClosetClick = {})
}
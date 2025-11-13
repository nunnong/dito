package com.dito.app.feature.group

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.ui.designsystem.Spacing.l
import com.dito.app.core.ui.designsystem.Spacing.m
import com.dito.app.core.ui.designsystem.Spacing.s
import com.dito.app.core.ui.designsystem.Spacing.xl
import com.dito.app.core.ui.designsystem.Spacing.xs

private fun formatDateRange(startDate: String, endDate: String): String {
    fun formatDate(date: String): String {
        return try {
            if (date.isEmpty()) return ""
            val parts = date.split("-")
            if (parts.size == 3) {
                "${parts[0]}.${parts[1]}.${parts[2]}"
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    return "${formatDate(startDate)} - ${formatDate(endDate)}"
}

@Preview(showBackground = true)
@Composable
fun ChallengeResultScreen(
    onNavigateToTab: (BottomTab) -> Unit = {},
    onClose: () -> Unit = {},
    viewModel: ChallengeResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var captureView by remember { mutableStateOf<ComposeView?>(null) }

    // 저장 성공/실패 메시지
    LaunchedEffect(saveSuccess) {
        saveSuccess?.let { success ->
            if (success) {
                Toast.makeText(context, "갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "저장에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
            viewModel.resetSaveSuccess()
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val rankings = uiState.rankings

    if (rankings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.errorMessage ?: "데이터를 불러올 수 없습니다",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnSurface
            )
        }
        return
    }

    val onSaveClick: () -> Unit = {
        coroutineScope.launch(Dispatchers.Default) {
            captureView?.let { view ->
                try {
                    // View가 측정되고 그려질 때까지 대기
                    withContext(Dispatchers.Main) {
                        view.post {
                            // View 크기 강제 측정
                            if (view.width == 0 || view.height == 0) {
                                val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                                    1080,
                                    android.view.View.MeasureSpec.EXACTLY
                                )
                                val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                                    0,
                                    android.view.View.MeasureSpec.UNSPECIFIED
                                )
                                view.measure(widthSpec, heightSpec)
                                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                            }

                            if (view.width > 0 && view.height > 0) {
                                val bitmap = view.drawToBitmap()
                                viewModel.saveScreenshot(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "캡처에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Box {
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
                ChallengeResultContent(
                    groupInfo = null,  // TODO: 그룹 정보를 별도 API에서 가져오거나 로컬에 저장 필요
                    rankings = rankings,
                    onClose = onClose,
                    onSave = onSaveClick
                )
            }
        }

        // 캡처용 뷰 (투명하게 숨김)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset(y = (-10000).dp) // 화면 위로 이동
        ) {
            AndroidView(
                factory = { ctx ->
                    ComposeView(ctx).apply {
                        captureView = this
                        setContent {
                            Box(
                                modifier = Modifier
                                    .width(1080.dp)
                                    .background(Background)
                                    .padding(horizontal = 24.dp)
                            ) {
                                ChallengeResultContent(
                                    groupInfo = null,
                                    rankings = rankings,
                                    onClose = {},
                                    onSave = {},
                                    isCapture = true
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(1080.dp)
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
fun ChallengeResultContent(
    groupInfo: com.dito.app.core.data.group.GroupInfo?,
    rankings: List<com.dito.app.core.data.group.RankingItem>,
    onClose: () -> Unit,
    onSave: () -> Unit,
    isCapture: Boolean = false
) {
    Column {
        // 닫기 버튼 (캡처 모드에서는 숨김)
        if (!isCapture) {
            Image(
                painter = painterResource(R.drawable.close),
                contentDescription = "닫기",
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.End)
                    .clickable { onClose() }
                    .padding(top = m)
            )
        }

            Spacer(modifier = Modifier.height(l))

            // 제목
            Text(
                text = "${groupInfo?.groupName ?: "그룹"}의\n챌린지가 종료되었습니다.",
                style = DitoCustomTextStyles.titleKLarge,
                color = OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(m))

            // 기간
            Text(
                text = if (groupInfo != null) {
                    formatDateRange(groupInfo.startDate, groupInfo.endDate)
                } else {
                    ""
                },
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
                    text = "Betting : ${groupInfo?.totalBetCoins ?: 0}",
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

            // 상단 레몬 아이콘
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = "레몬",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(xl))

            // WIN/LOSE 카드
            if (rankings.isNotEmpty()) {
                val winner = rankings.first()
                val loser = rankings.last()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(m)
                ) {
                    // WIN 카드
                    WinLoseCard(
                        modifier = Modifier.weight(1f),
                        isWin = true,
                        nickname = winner.nickname,
                        time = winner.totalScreenTimeFormatted,
                        profileImage = winner.profileImage
                    )

                    // LOSE 카드
                    WinLoseCard(
                        modifier = Modifier.weight(1f),
                        isWin = false,
                        nickname = loser.nickname,
                        time = loser.totalScreenTimeFormatted,
                        profileImage = loser.profileImage
                    )
                }
            }

            Spacer(modifier = Modifier.height(xl))

            // 가장 많이 사용한 앱
            MostUsedAppSection()

            Spacer(modifier = Modifier.height(xl))

            // 벌칙 카드
            val lastPlace = rankings.lastOrNull()
            if (lastPlace != null) {
                PenaltyCardSection(
                    penaltyRecipient = lastPlace.nickname,
                    penaltyDescription = groupInfo?.penaltyDescription ?: "벌칙 없음"
                )
            }

        Spacer(modifier = Modifier.height(xl))

        // 저장 버튼
        SaveButton(
            onSave = onSave
        )

        Spacer(modifier = Modifier.height(l))
    }
}

@Composable
fun WinLoseCard(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    nickname: String,
    time: String,
    profileImage: String? = null
) {
    val backgroundColor = if (isWin) Primary else Color(0xFFE0E0E0)
    val textColor = if (isWin) OnPrimary else OnSurface
    val label = if (isWin) "WIN" else "LOSE"

    Column(
        modifier = modifier
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 12.dp,
                color = Color.Black
            )
            .clip(DitoShapes.medium)
            .border(2.dp, Color.Black, DitoShapes.medium)
            .background(backgroundColor)
            .padding(vertical = l, horizontal = m),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // WIN/LOSE 텍스트
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.height(m))

        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profileImage != null) {
                AsyncImage(
                    model = profileImage,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(m))

        // 닉네임
        Text(
            text = nickname,
            style = DitoCustomTextStyles.titleKMedium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(xs))

        // 시간
        Text(
            text = time,
            style = DitoCustomTextStyles.titleKMedium,
            color = textColor
        )
    }
}

@Composable
fun SaveButton(
    onSave: () -> Unit = {}
) {
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
                .clickable { onSave() }
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
fun PenaltyCardSection(
    penaltyRecipient: String,
    penaltyDescription: String
) {
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
                    text = "벌칙대상자 : $penaltyRecipient",
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
                text = "벌칙 : $penaltyDescription",
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

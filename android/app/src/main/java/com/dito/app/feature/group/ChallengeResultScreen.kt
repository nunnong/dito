package com.dito.app.feature.group

import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.OnPrimary
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.OnSurfaceVariant
import com.dito.app.core.ui.designsystem.Outline
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String) {
    MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, title, null)
    Toast.makeText(context, "챌린지 결과가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
}

fun formatDateRange(startDate: String, endDate: String): String {
    return "$startDate ~ $endDate"
}


@Composable
fun ChallengeResultRoute(
    viewModel: ChallengeResultViewModel = hiltViewModel(),
    onClose: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var captureView by remember { mutableStateOf<ComposeView?>(null) }

    val context = LocalContext.current
    val onSaveClick: () -> Unit = {
        captureView?.let {
            val bitmap = it.drawToBitmap()
            saveBitmapToGallery(context, bitmap, "dito_challenge_result")
        }
        Unit
    }

    LaunchedEffect(Unit) {
        viewModel.fetchChallengeResult()
    }

    val rankings = uiState.result?.rankings ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
//            topBar = {
//                DitoTopAppBar(
//                    title = "챌린지 결과",
//                    onNavigationClick = onClose
//                )
//            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l)
            ) {
                ChallengeResultContent(
                    groupName = uiState.groupName,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    totalBetCoins = uiState.totalBetCoins,
                    penaltyDescription = uiState.penaltyDescription,
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
                                    groupName = uiState.groupName,
                                    startDate = uiState.startDate,
                                    endDate = uiState.endDate,
                                    totalBetCoins = uiState.totalBetCoins,
                                    penaltyDescription = uiState.penaltyDescription,
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
    groupName: String,
    startDate: String,
    endDate: String,
    totalBetCoins: Int,
    penaltyDescription: String,
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
                    .padding(top = Spacing.m)
            )
        }

            Spacer(modifier = Modifier.height(Spacing.l))

            // 제목
            Text(
                text = "${groupName}의\n챌린지가 종료되었습니다.",
                style = DitoCustomTextStyles.titleKLarge,
                color = OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.m))

            // 기간
            Text(
                text = formatDateRange(startDate, endDate),
                style = DitoCustomTextStyles.titleKSmall,
                color = OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.m))

            // 배팅 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Betting : $totalBetCoins",
                    style = DitoCustomTextStyles.titleDLarge,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.width(Spacing.xs))

                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = "레몬",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

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

            Spacer(modifier = Modifier.height(Spacing.xl))

            // WIN/LOSE 카드
//            if (rankings.isNotEmpty()) {
//                val winner = rankings.first()
//                val loser = rankings.last()
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
//                ) {
//                    // WIN 카드
//                    WinLoseCard(
//                        modifier = Modifier.weight(1f),
//                        isWin = true,
//                        nickname = winner.nickname,
//                        time = winner.totalScreenTimeFormatted,
//                        costumeUrl = winner.profileImage
//                    )
//
//                    // LOSE 카드
//                    WinLoseCard(
//                        modifier = Modifier.weight(1f),
//                        isWin = false,
//                        nickname = loser.nickname,
//                        time = loser.totalScreenTimeFormatted,
//                        costumeUrl = loser.profileImage
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // 가장 많이 사용한 앱
            MostUsedAppSection()

            Spacer(modifier = Modifier.height(Spacing.xl))

            // 벌칙 카드
            val lastPlace = rankings.lastOrNull()
            if (lastPlace != null) {
                PenaltyCardSection(
                    penaltyRecipient = lastPlace.nickname,
                    penaltyDescription = penaltyDescription
                )
            }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // 저장 버튼 (캡처 모드에서는 숨김)
        if (!isCapture) {
             SaveButton(
                onSave = onSave
            )
        }

        Spacer(modifier = Modifier.height(Spacing.l))
    }
}

@Composable
fun WinLoseCard(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    nickname: String,
    time: String,
    costumeUrl: String? = null
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
            .padding(vertical = Spacing.l, horizontal = Spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // WIN/LOSE 텍스트
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (costumeUrl != null) {
                AsyncImage(
                    model = costumeUrl,
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

        Spacer(modifier = Modifier.height(Spacing.m))

        // 닉네임
        Text(
            text = nickname,
            style = DitoCustomTextStyles.titleKMedium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

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
                .padding(vertical = Spacing.m, horizontal = Spacing.xl),
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
                    .padding(vertical = Spacing.m)
            ) {
                Text(
                    text = "벌칙대상자 : $penaltyRecipient",
                    style = DitoCustomTextStyles.titleKMedium,
                    color = OnPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            // 벌칙 내용
            Text(
                text = "벌칙 : $penaltyDescription",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
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

        Spacer(modifier = Modifier.height(Spacing.l))

        Text(
            text = "가장 많이 사용한 앱",
            style = DitoCustomTextStyles.titleKMedium,
            color = OnSurface
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        // TODO: 실제 데이터 연동 필요
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = R.drawable.ic_youtube), contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(Spacing.m))
            Text(
                text = "Youtube",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnSurface
            )
        }

        Spacer(modifier = Modifier.height(Spacing.l))

        HorizontalDivider(
            color = Outline,
            thickness = 1.dp
        )
    }
}

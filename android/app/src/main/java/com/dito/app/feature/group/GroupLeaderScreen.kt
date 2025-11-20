package com.dito.app.feature.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.dito.app.core.data.group.Participant
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoHardShadow
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.hardShadow


@Composable
fun GroupLeaderScreen(
    groupName: String,
    entryCode: String,
    period: Int,
    goal: String,
    penalty: String,
    startDate: String,
    endDate: String,
    participants: List<Participant> = emptyList(),
    isStarted: Boolean = false,
    onStartChallenge: () -> Unit = {},
    onLoadParticipants: () -> Unit = {},
    onLoadGroupDetails: () -> Unit = {},
    onStartPolling: () -> Unit = {},
    onStopPolling: () -> Unit = {},
) {
    val context = LocalContext.current
    val participantCount = participants.size

    fun formatDate(date: String): String {
        return try {
            if (date.isEmpty()) return ""
            val parts = date.split("-")
            if (parts.size == 3) {
                val year = parts[0].takeLast(2)
                val month = parts[1]
                val day = parts[2]
                "$year.$month.$day"
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    val formattedStartDate = formatDate(startDate)
    val formattedEndDate = formatDate(endDate)

    // 화면 진입 시 참여자 목록 조회 + 5초마다 polling
    LaunchedEffect(Unit) {
        onStartPolling()
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopPolling()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9D9D9))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 타이틀
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 입장코드 및 슬롯 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow(
                        offsetX = 4.dp,
                        offsetY = 4.dp,
                        cornerRadius = 0.dp,
                        color = Color.Black
                    )
                    .border(3.dp, Color.Black)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Ready 탭
                    Box(
                        modifier = Modifier
                            .border(2.dp, Color.Black)
                            .background(Primary)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Ready",
                            style = DitoTypography.titleMedium,
                            color = Color.Black
                        )
                    }

                    // 입장코드
                    Text(
                        text = entryCode,
                        style = DitoTypography.titleMedium,
                        color = Color.Black
                    )

                    // 복사 아이콘
                    Image(
                        painter = painterResource(id = R.drawable.copy),
                        contentDescription = "복사",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("입장코드", entryCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "입장코드가 복사되었습니다", Toast.LENGTH_SHORT).show()
                            }
                    )

                    // 4개 슬롯
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(2.dp, Color.Black)
                                    .background(if (index < participantCount) Primary else Color.Gray)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // START 버튼
            if (!isStarted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 16.dp,
                            color = Color.Black
                        )
                        .clip(DitoShapes.medium)
                        .border(3.dp, Color.Black, DitoShapes.medium)
                        .background(Primary)
                        .clickable { onStartChallenge() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START",
                        style = DitoCustomTextStyles.titleDLarge,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 참가자 목록 (2x2 그리드)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) { colIndex ->
                            val index = rowIndex * 2 + colIndex

                            val participant = participants.getOrNull(index)
                            val backgroundImgUrl =
                                participant?.equipedItems?.find { it.type == "background" }?.imgUrl
                            val costumeImgUrl =
                                participant?.equipedItems?.find { it.type == "costume" }?.imgUrl

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(DitoShapes.extraSmall)
                                        .border(1.dp, Color.Black, DitoShapes.extraSmall)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (participant != null) {
                                        // 배경 이미지 (먼저 그려짐)
                                        if (backgroundImgUrl != null) {
                                            AsyncImage(
                                                model = backgroundImgUrl,
                                                contentDescription = "${'$'}{participant.nickname} background",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // 의상 이미지 (배경 위에 그려짐)
                                        if (costumeImgUrl != null) {
                                            AsyncImage(
                                                model = costumeImgUrl,
                                                contentDescription = "${'$'}{participant.nickname} costume",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = participant?.nickname ?: "",
                                    style = DitoTypography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 챌린지 정보 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 4.dp))
                    .border(2.dp, Color.Black, DitoShapes.extraSmall)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Primary)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.info),
                            contentDescription = "정보 아이콘",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "그룹 정보 : ${'$'}{groupName}",
                            style = DitoCustomTextStyles.titleKMedium,
                            color = Color.Black,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.Black)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PERIOD : ${'$'}formattedStartDate - ${'$'}formattedEndDate",
                            style = DitoCustomTextStyles.titleKSmall,
                            color = Color.Black
                        )

                        Text(
                            text = "GOAL : {}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKSmall
                        )

                        Text(
                            text = "PENALTY : ${'$'}penalty",
                            style = DitoCustomTextStyles.titleKSmall,
                            color = Color.Black
                        )
                        Text(
                            text = "현재 참여 인원 : ${'$'}{participantCount}명",
                            style = DitoCustomTextStyles.titleKSmall,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

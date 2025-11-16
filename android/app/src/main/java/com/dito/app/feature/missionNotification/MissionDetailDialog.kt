package com.dito.app.feature.missionNotification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dito.app.R
import com.dito.app.core.data.missionNotification.MissionNotificationData
import com.dito.app.core.data.missionNotification.MissionResult
import com.dito.app.core.data.missionNotification.MissionStatus
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

// 미션 상세 모달
@Composable
fun MissionDetailDialog(
    mission: MissionNotificationData,
    isShowingAnimation: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = Color.Black,
            shadowColor = Color.Black,
            contentPadding = PaddingValues(vertical = Spacing.l)
        ) {

            //뒤로가기 버튼을 왼쪽에 정렬하기 위해
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m, vertical = Spacing.s)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .clickable{onDismiss()}
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = mission.missionType,
                    color = Color.Black,
                    style = DitoTypography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.m)
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                // AI 피드백 섹션
                mission.feedback?.let { feedback ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.m)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(Background, DitoShapes.small)
                            .padding(Spacing.m)
                    ) {
                        Text(
                            text = "AI 피드백",
                            color = Primary,
                            style = DitoCustomTextStyles.titleDMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = feedback,
                            color = Color.Black,
                            style = DitoTypography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))
                }

                // 미션 결과
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "결과",
                        color = Color.Black,
                        style = DitoTypography.labelLarge
                    )

                    val resultText = when (mission.result) {
                        MissionResult.SUCCESS -> "성공"
                        MissionResult.FAILURE -> "실패"
                        MissionResult.IGNORE -> "무시됨"
                        else -> "진행 중"
                    }

                    Text(
                        text = resultText,
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // 레몬 보상
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "보상",
                        color = Color.Black,
                        style = DitoTypography.labelLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.lemon),
                            contentDescription = "Lemon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${mission.coinReward}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleDMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // 확인 버튼 (성공/실패 모두 표시)
                if (mission.status == MissionStatus.COMPLETED) {
                    val isSuccess = mission.result == MissionResult.SUCCESS

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 8.dp,
                                color = Color.Black
                            )
                            .clip(DitoShapes.small)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(if (isSuccess) Primary else Color.White)
                            .clickable(enabled = !isShowingAnimation) { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSuccess) {
                                Image(
                                    painter = painterResource(id = R.drawable.lemon),
                                    contentDescription = "Lemon",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isSuccess) "레몬 받기" else "확인",
                                color = Color.Black,
                                style = DitoCustomTextStyles.titleDMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))
                }



//                Spacer(modifier = Modifier.height(Spacing.m))
            }
        }
    }
}

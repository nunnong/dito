package com.dito.app.feature.missionNotification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography

@Composable
fun MissionInfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .width(300.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* 클릭 이벤트 전파 방지 */ }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // 제목
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "미션 알림 가이드",
                        style = DitoCustomTextStyles.titleDLarge,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Black
                )

                // 가이드 내용
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GuideItem(
                        question = "알림은 언제 오나요?",
                        answer = "AI가 사용 패턴을 분석해 개입이 필요할 때 알림을 보내요. 같은 앱을 오래 쓰거나, 앱을 자주 전환하거나, 취침 시간대에 사용할 때 나타나요."
                    )

                    GuideItem(
                        question = "알림 후에는 어떻게 되나요?",
                        answer = "타이머와 함께 자동으로 미션이 시작돼요. 버튼을 누를 필요 없이 AI가 당신의 행동을 지켜봅니다."
                    )

                    GuideItem(
                        question = "성공/실패는 어떻게 판정되나요?",
                        answer = "미션 중 앱 사용을 자동 감지해요. 알림받은 앱을 내려놓으면 성공, 계속 사용하면 실패예요."
                    )

                    GuideItem(
                        question = "하루에 알림이 몇 번 오나요?",
                        answer = "하루 2~3회, 최소 2시간 간격으로 와요. 설정에서 빈도를 조절할 수 있어요."
                    )

                    GuideItem(
                        question = "YouTube는 다르게 분석돼요",
                        answer = "AI가 콘텐츠 유형을 구분해요. 강의는 오래 봐도 괜찮고, 숏폼은 20분 시청 시 알림이 와요."
                    )

                    GuideItem(
                        question = "미션에 실패해도 괜찮아요",
                        answer = "실패가 반복되면 AI가 더 쉬운 미션을 제안하거나 다른 시간대에 알림을 보내요."
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideItem(
    question: String,
    answer: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = question,
            style = DitoTypography.labelLarge,
            color = Color.Black
        )
        Text(
            text = answer,
            style = DitoTypography.bodySmall,
            color = Color.Black
        )
    }
}
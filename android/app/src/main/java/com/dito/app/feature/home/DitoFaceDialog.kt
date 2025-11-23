package com.dito.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Secondary
import com.dito.app.core.ui.designsystem.Tertiary

/**
 * 캐릭터 얼굴 표정 안내 다이얼로그
 *
 * 사용자의 스탯에 따라 디토의 표정이 변한다는 것을 알려주는 정보 제공용 다이얼로그
 *
 * @param onDismiss 다이얼로그를 닫을 때 호출되는 콜백
 */
@Composable
fun DitoFaceDialog(
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
        // 배경 클릭 영역 (모달 밖 클릭 시 닫힘)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.TopCenter
        ) {
            // 모달 컨텐츠 (내부 클릭 이벤트 전파 방지)
            Column(
                modifier = Modifier
                    .padding(top = 100.dp) // Adjust this value to move the modal higher or lower
                    .width(294.dp)
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

                // 세 번째 섹션: 표정 변화 안내
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "내 전체 스탯에 따라\n디토의 표정이 바뀌어요!",
                        style = DitoCustomTextStyles.titleDMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // 구분선
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Black
                )

                // 네 번째 섹션: 얼굴 표정과 설명
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 얼굴 표정 5개 (가로로 나열)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 얼굴 1 (가장 슬픔)
                        Image(
                            painter = painterResource(id = R.drawable.face_1),
                            contentDescription = "가장 슬픈 표정",
                            modifier = Modifier.size(30.dp, 22.dp)
                        )

                        // 얼굴 2 (슬픔)
                        Image(
                            painter = painterResource(id = R.drawable.face_2),
                            contentDescription = "슬픈 표정",
                            modifier = Modifier.size(30.dp, 22.dp)
                        )

                        // 얼굴 3 (보통)
                        Image(
                            painter = painterResource(id = R.drawable.face_3),
                            contentDescription = "보통 표정",
                            modifier = Modifier.size(30.dp, 22.dp)
                        )

                        // 얼굴 4 (기쁨)
                        Image(
                            painter = painterResource(id = R.drawable.face_4),
                            contentDescription = "기쁜 표정",
                            modifier = Modifier.size(30.dp, 22.dp)
                        )

                        // 얼굴 5 (가장 행복)
                        Image(
                            painter = painterResource(id = R.drawable.face_5),
                            contentDescription = "가장 행복한 표정",
                            modifier = Modifier.size(30.dp, 22.dp)
                        )
                    }

                    // 안내 문구
                    Text(
                        text = "모든 스탯을 균형 있게 관리해야\n디토의 표정이 좋아집니다",
                        style = DitoTypography.bodySmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                // 첫 번째 섹션: AI 판단 안내
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "디토 AI의 판단으로\n세 가지 스탯이 변화합니다!",
                        style = DitoCustomTextStyles.titleDMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // 구분선
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Black
                )

                // 두 번째 섹션: 스탯 설명
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Spacer(modifier = Modifier.height(0.dp))

                    // 자기관리
                    StatItem(
                        label = "자기관리",
                        description = "AI 알림에 반응해 미션을 수행했을 때",
                        backgroundColor = Primary
                    )

                    // 집중력
                    StatItem(
                        label = "집중력",
                        description = "앱 간 전환이 잦지 않을 때",
                        backgroundColor = Secondary
                    )

                    // 수면
                    StatItem(
                        label = "수면",
                        description = "취침 전 휴대폰을 사용하지 않을 때",
                        backgroundColor = Tertiary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }



            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    description: String,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 스탯 라벨 (pill 형태)
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(48.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = DitoTypography.labelMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 설명 텍스트
        Text(
            text = description,
            style = DitoTypography.bodySmall,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DitoFaceDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        DitoFaceDialog(
            onDismiss = {}
        )
    }
}
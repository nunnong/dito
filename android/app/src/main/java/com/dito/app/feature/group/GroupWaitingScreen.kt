package com.dito.app.feature.group

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import androidx.compose.ui.graphics.ColorFilter

data class GroupMember(
    val name: String,
    val characterUrl: String? = null,
    val isWaiting: Boolean = false
)
@Composable
fun GroupWaitingScreen(
    groupName: String = "눈농포케콕콕콕프렌즈",
    inviteCode: String = "ABCD",
    members: List<GroupMember> = emptyList(),
    onCopyCodeClick: () -> Unit = {},
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1) 전체 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.group_waiting_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2) 배경 위에 깔리는 큰 나무 이미지 (group_tree)
        Image(
            painter = painterResource(id = R.drawable.group_tree),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)   // 화면 가로 중앙 정렬
                .width(396.dp)                // Figma 기준 크기
                .height(396.dp)
                .offset(y = (90).dp),
            contentScale = ContentScale.Fit
        )

        // 메인 컨테이너 (디자인 기준 410 x 635)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .offset(y = (-50).dp),          // ← 둘을 같이 위로 올리기
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 나무 간판 + 그룹 이름
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_panel),
                        contentDescription = "Group Panel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // 간판 안 그룹명
                    StrokeText(
                        text = groupName,
                        style = DitoTypography.headlineMedium,
                        fillColor = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 1 .dp,
                        modifier = Modifier
                            .width(180.dp)             // ← 박스의 가로 폭을 고정!
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp)
                            .offset(y = 42.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 2                   // ← 자동 줄바꿈 허용
                    )

                }
                // 참여코드 박스
                Row(
                    modifier = Modifier
                        .width(262.dp)
                        .height(48.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "참여코드 : $inviteCode",
                        style = DitoCustomTextStyles.titleDLarge, // 22sp DungGeunMo
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BounceClickable(
                        onClick = {
                            playPopSound(context)
                            onCopyCodeClick()
                        }
                    ) { isPressed ->
                        Image(
                            painter = painterResource(id = R.drawable.copy), // 복사 아이콘 리소스
                            contentDescription = "Copy Invite Code",
                            modifier = Modifier.size(24.dp),
                            alpha = if (isPressed) 0.7f else 1f,
                            colorFilter = ColorFilter.tint(OnSurfaceVariant)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 멤버 슬롯 4개
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val baseMembers = members.take(4)
                val placeholderCount = (4 - baseMembers.size).coerceAtLeast(0)
                val displayMembers = baseMembers + List(placeholderCount) {
                    GroupMember(
                        name = "waiting...",
                        characterUrl = null,
                        isWaiting = true
                    )
                }

                displayMembers.forEach { member ->
                    MemberSlot(member = member,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                        )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 하단 노란 안내 배너
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(52.dp)
                    .hardShadow(
                        DitoHardShadow.ButtonLarge.copy(
                            cornerRadius = 8.dp
                        )
                    )
                    .background(Primary, RoundedCornerShape(8.dp))
                    .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "방장의 시작을 기다리고 있어요!",
                    style = DitoCustomTextStyles.titleDMedium, // 16sp DungGeunMo
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MemberSlot(
    member: GroupMember,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 캐릭터 + 의자 영역 (캐릭터 기준으로 크게)
        Box(
            modifier = Modifier
                .fillMaxWidth()      // 캐릭터 폭에 맞춤
                .height(160.dp),    // 캐릭터 높이
            contentAlignment = Alignment.BottomCenter
        ) {
            // 의자(그루터기)는 아래쪽에만 작게 배치
            Image(
                painter = painterResource(id = R.drawable.group_chair),
                contentDescription = "Chair",
                modifier = Modifier
                    .width(96.dp)
                    .height(80.dp)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Fit
            )

            // 캐릭터
            if (!member.isWaiting) {
                val characterModifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-50).dp)

                if (!member.characterUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = member.characterUrl,
                        contentDescription = "Character",
                        modifier = characterModifier,
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.dito_tmp),
                        contentDescription = "Character",
                        modifier = characterModifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // 닉네임 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .hardShadow(
                    DitoHardShadow.ButtonSmall.copy(
                        cornerRadius = 4.dp
                    )
                )
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (member.isWaiting) {
                // waiting 상태 -> loading 이미지 표시
                Image(
                    painter = painterResource(id = R.drawable.loading),
                    contentDescription = "Waiting",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            } else {
                // 기존 닉네임 표시
                Text(
                    text = member.name,
                    style = DitoTypography.labelSmall,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}


// 효과음 재생
fun playPopSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.pop)
    mediaPlayer?.start()
    mediaPlayer?.setOnCompletionListener { mp ->
        mp.release()
    }
}


@Preview(showBackground = true)
@Composable
fun GroupWaitingScreenPreview() {
    val mockMembers = listOf(
        GroupMember("위아리얼디토예"),
        GroupMember("정윤영"),
        GroupMember("유지은")
    )

    DitoTheme {
        GroupWaitingScreen(
            groupName = "눈농포케콕콕콕프렌즈",
            inviteCode = "ABCD",
            members = mockMembers
        )
    }
}

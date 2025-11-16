package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.ErrorContainer
import com.dito.app.core.ui.designsystem.OnErrorContainer
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun CreateGroupNameDialog(
    initialGroupName: String = "",
    costumeUrl: String?,
    onDismiss: () -> Unit,
    onNavigateNext: (String) -> Unit
) {
    var groupName by remember { mutableStateOf(initialGroupName) }
    val isValid = groupName.length in 1..10 && groupName.matches("^[a-zA-Z가-힣]+$".toRegex())

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
            // Column: 전체 컨텐츠를 세로로 배치
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Box: 뒤로가기 버튼을 왼쪽에 정렬하기 위해
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s, vertical = Spacing.m)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "뒤로가기",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                // 제목 텍스트
                Text(
                    text = "방의 이름을\n정해주세요",
                    color = OnSurface,
                    style = DitoTypography.headlineMedium
                )

                Spacer(Modifier.height(Spacing.xl))

                // 캐릭터 이미지
                AsyncImage(
                    model = costumeUrl,
                    contentDescription = "Character Image",
                    error = painterResource(id = R.drawable.dito), // fallback image
                    modifier = Modifier.size(115.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(Spacing.xl))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(horizontal = Spacing.m)
                ) {
                    BasicTextField(
                        value = groupName,
                        onValueChange = { input ->
                            // 길이만 체크하여 한글 조합형 입력 허용
                            if (input.length <= 10) {
                                groupName = input
                            }
                        },
                        textStyle = DitoTypography.bodyLarge.copy(color = OnSurface, textAlign = TextAlign.Center),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val strokeWidth = 2.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(vertical = Spacing.s),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (groupName.isEmpty()) {
                                    Text(
                                        text = "영문/한글 1~10자",
                                        color = Color.Gray,
                                        style = DitoTypography.bodyMedium.copy(textAlign = TextAlign.Center)
                                    )
                                }
                                innerTextField()
                            }

                        }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.x),
                        contentDescription = "방 이름 삭제",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clickable { groupName = "" }
                    )
                }

                Spacer(Modifier.height(Spacing.xxl))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 8.dp,
                            color = Color.Black

                        )
                        .clip(DitoShapes.small)
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(if (isValid) Primary else Color.White)
                        .clickable(enabled = isValid) {
                            onNavigateNext(groupName)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "계속하기",
                        color = if (isValid) Color.Black else Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                }

                Spacer(Modifier.height(Spacing.m))
            }
        }
    }
}

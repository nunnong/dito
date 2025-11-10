package com.dito.app.feature.group


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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Outline
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.Spacing.m
import com.dito.app.core.ui.designsystem.hardShadow
import com.dito.app.core.ui.component.DitoModalContainer

@Composable
fun JoinWithCodeDialog(
    onDismiss: () -> Unit,
    onJoinWithCode: (String) -> Unit
) {
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    val isCodeComplete = code1.isNotEmpty() && code2.isNotEmpty() &&
                         code3.isNotEmpty() && code4.isNotEmpty()

    LaunchedEffect(Unit) {
        focusRequester1.requestFocus()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        DitoModalContainer(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 400.dp),
            contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.l)
        ) {
            Box {
                // 닫기 버튼을 가장 상단에 배치
                Image(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "창 닫기",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onDismiss() }
                )

                // 컨텐츠
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.s)
                ) {
                    Text(
                        text = "코드로 참여하기",
                        color = OnSurface,
                        style = DitoCustomTextStyles.titleKLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CodeInputBox(
                            value = code1,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1) {
                                    code1 = newValue.uppercase()
                                    if (newValue.isNotEmpty()) {
                                        focusRequester2.requestFocus()
                                    }
                                }
                            },
                            focusRequester = focusRequester1,
                            modifier = Modifier.weight(1f)
                        )

                        CodeInputBox(
                            value = code2,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1) {
                                    code2 = newValue.uppercase()
                                    if (newValue.isNotEmpty()) {
                                        focusRequester3.requestFocus()
                                    }
                                }
                            },
                            focusRequester = focusRequester2,
                            modifier = Modifier.weight(1f)
                        )

                        CodeInputBox(
                            value = code3,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1) {
                                    code3 = newValue.uppercase()
                                    if (newValue.isNotEmpty()) {
                                        focusRequester4.requestFocus()
                                    }
                                }
                            },
                            focusRequester = focusRequester3,
                            modifier = Modifier.weight(1f)
                        )

                        CodeInputBox(
                            value = code4,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1) {
                                    code4 = newValue.uppercase()
                                }
                            },
                            focusRequester = focusRequester4,
                            modifier = Modifier.weight(1f)
                        )
                    }


                    Spacer(Modifier.height(Spacing.xl))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 16.dp,
                                color = Color.Black
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                            .background(if (isCodeComplete) Primary else Color.White)
                            .clickable(enabled = isCodeComplete) {
                                val entryCode = code1 + code2 + code3 + code4
                                onJoinWithCode(entryCode)
                            }
                            .padding(vertical = Spacing.m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "입력",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, color = Color.Black, shape = DitoShapes.extraSmall)
            .background(color = Color.White, shape = DitoShapes.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            textStyle = DitoCustomTextStyles.titleKLarge.copy(
                color = OnSurface,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}


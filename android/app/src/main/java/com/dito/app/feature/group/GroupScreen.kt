package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnPrimary
import com.dito.app.core.ui.designsystem.PrimaryContainer

@Preview(showBackground = true)
@Composable
fun GroupChallengeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryContainer)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.dito),
            contentDescription = null,
            modifier = Modifier
                .width(270.dp)
                .height(120.dp)
                .padding(bottom = 11.dp)
        )

        Row(
            modifier = Modifier
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.together),
                contentDescription = null,
                modifier = Modifier
                    .width(82.dp)
                    .height(135.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = 28.dp, horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아직 참여 중인 챌린지가 없어요",
                color = OnPrimary,
                style = DitoTypography.headlineSmall
            )
            Text(
                text = "함께 디지털 휴식에 도전해볼까요?",
                color = OnPrimary,
                style = DitoTypography.headlineSmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 29.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "방 만들기",
                    color = Color.Black,
                    style = DitoTypography.headlineMedium
                )
            }

            // 입장하기 버튼
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mail),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "입장하기",
                    color = Color.Black,
                    style = DitoTypography.headlineMedium
                )
            }
        }
    }
}

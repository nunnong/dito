package com.dito.app.feature.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnPrimary
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing.s

@Preview(showBackground = true)
@Composable
fun OngoingChallengeScreen(
    onNavigateToTab: (BottomTab) -> Unit = {}
) {

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
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            Text(
                text = "RACE IN PROGRESS!",
                style = DitoTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "취업하기",
                style = DitoTypography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "3일 / 7일",
                style = DitoCustomTextStyles.titleKSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BETTING : ",
                    style = DitoCustomTextStyles.titleDLarge
                )

                Spacer(modifier = Modifier.width(6.dp))

                Image(
                    painter = painterResource(R.drawable.lemon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }


            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 16.dp)
                    .border(1.dp, Color.Black, shape = DitoShapes.medium)
                    .clip(shape = DitoShapes.small)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    "1st",
                    color = OnPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(start = 9.dp, end = 32.dp)
                )
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(end = 26.dp)
                        .padding(end = 23.dp)
                ) {
                    Text(
                        "뛰콩",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                    Text(
                        "50h 01m",
                        color = Color(0xFF000000),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 16.dp)
                    .border(1.dp, Color.Black, shape = DitoShapes.medium)
                    .clip(shape = DitoShapes.small)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    "1st",
                    color = OnPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(start = 9.dp, end = 32.dp)
                )
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(end = 26.dp)
                        .padding(end = 23.dp)
                ) {
                    Text(
                        "뛰콩",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                    Text(
                        "50h 01m",
                        color = Color(0xFF000000),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 16.dp)
                    .border(1.dp, Color.Black, shape = DitoShapes.medium)
                    .clip(shape = DitoShapes.small)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    "1st",
                    color = OnPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(start = 9.dp, end = 32.dp)
                )
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(end = 26.dp)
                        .padding(end = 23.dp)
                ) {
                    Text(
                        "뛰콩",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                    Text(
                        "50h 01m",
                        color = Color(0xFF000000),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 16.dp)
                    .border(1.dp, Color.Black, shape = DitoShapes.medium)
                    .clip(shape = DitoShapes.small)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    "1st",
                    color = OnPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(start = 9.dp, end = 32.dp)
                )
                Image(
                    painter = painterResource(R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(end = 26.dp)
                        .padding(end = 23.dp)
                ) {
                    Text(
                        "뛰콩",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                    Text(
                        "50h 01m",
                        color = Color(0xFF000000),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box() {
                Image(
                    painter = painterResource(R.drawable.raceinfo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(400.dp)
                        .padding(top = 16.dp)
                )
                Text(
                    "PERIOD :",
                    color = OnPrimary,
                    style = DitoCustomTextStyles.titleKSmall
                )
                Text(
                    "PENALTY :",
                    color = OnPrimary,
                    style = DitoCustomTextStyles.titleKSmall
                )
            }


        }
    }
}

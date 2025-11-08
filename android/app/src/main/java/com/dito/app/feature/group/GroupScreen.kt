package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnPrimary
import com.dito.app.core.ui.designsystem.PrimaryContainer
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun GroupScreen(
    navController: NavController? = null,
    viewModel: GroupChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryContainer)
            .verticalScroll(rememberScrollState())
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

            Image(
                painter = painterResource(id = R.drawable.groupchallenge),
                contentDescription = null,
                modifier = Modifier
                    .width(270.dp)
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.l))

            Row(
                modifier = Modifier.padding(bottom = Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dito),
                    contentDescription = null,
                    modifier = Modifier
                        .size(135.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.melon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(135.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(vertical = Spacing.xl, horizontal = Spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "아직 참여 중인 챌린지가 없어요",
                    color = OnPrimary,
                    style = DitoCustomTextStyles.titleKLarge
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "함께 디지털 휴식에 도전해볼까요?",
                    color = OnPrimary,
                    style = DitoCustomTextStyles.titleKLarge
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l, vertical = Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 8.dp,
                            color = Color.Black
                        )
                        .clip(DitoShapes.small)
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(Color.White)
                        .clickable { viewModel.onCreateDialogOpen() }
                        .padding(vertical = Spacing.l)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Text(
                        text = "방 만들기",
                        color = Color.Black,
                        style = DitoTypography.headlineSmall
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 8.dp,
                            color = Color.Black
                        )
                        .clip(DitoShapes.small)
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(Color.White)
                        .clickable { viewModel.onJoinDialogOpen() }
                        .padding(vertical = Spacing.l)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mail),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Text(
                        text = "입장하기",
                        color = Color.Black,
                        style = DitoTypography.headlineSmall
                    )
                }
            }
        }

    var showChallengeDialog by remember { mutableStateOf(false) }
    var currentGroupName by remember { mutableStateOf("") }

    if (uiState.showCreateDialog) {
        CreateGroupNameDialog(
            onDismiss = {
                viewModel.onDialogClose()
            },
            onNavigateNext = { groupName ->
                currentGroupName = groupName
                viewModel.onDialogClose()
                showChallengeDialog = true
            }
        )
    }

    if (showChallengeDialog) {
        CreateChallengeDialog(
            groupName = currentGroupName,
            onDismiss = {
                showChallengeDialog = false
            },
            onCreateChallenge = { name, goal, penalty, period, bet ->
                showChallengeDialog = false
            }
        )
    }

    if (uiState.showJoinDialog) {
        JoinWithCodeDialog(onDismiss = {
            viewModel.onDialogClose()
        })
    }
}

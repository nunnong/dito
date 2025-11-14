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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dito.app.R
import androidx.compose.ui.platform.LocalContext
import com.dito.app.core.background.ScreenTimeSyncWorker
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnPrimary
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.PrimaryContainer
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun GroupScreen(
    navController: NavController? = null,
    viewModel: GroupChallengeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.refreshGroupInfo()
        // GroupScreen 진입 시 스크린타임 동기화
        ScreenTimeSyncWorker.triggerImmediateSync(context)
    }

    // 스플래시 화면 표시
    if (uiState.showSplash) {
        ChallengeSplashScreen()
        return
    }

    // 챌린지 상태에 따라 다른 화면 표시
    when (uiState.challengeStatus) {
        ChallengeStatus.IN_PROGRESS -> {
            OngoingChallengeScreen(viewModel = viewModel)
            return
        }
        ChallengeStatus.PENDING -> {
            // 대기 중 (방장/참가자 모두 GroupWaitingScreen으로 통합)
            GroupWaitingScreen(viewModel = viewModel)
            return
        }
        ChallengeStatus.COMPLETED, ChallengeStatus.CANCELLED -> {
            // 종료되거나 취소된 챌린지는 그룹 정보 삭제하고 NO_CHALLENGE로 표시
            viewModel.onChallengeEnded()
            // fall through to NO_CHALLENGE
        }
        ChallengeStatus.NO_CHALLENGE -> {
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()

    ) {
        // 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.padding(bottom = Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.group_main_img),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
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
                    style = DitoCustomTextStyles.titleKMedium
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "함께 디지털 휴식에 도전해볼까요?",
                    color = OnPrimary,
                    style = DitoCustomTextStyles.titleKMedium
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
                        .background(Primary)
                        .clickable { viewModel.onCreateDialogOpen() }
                        .padding(vertical = Spacing.l)
                ) {
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
                        .background(Primary)
                        .clickable { viewModel.onJoinDialogOpen() }
                        .padding(vertical = Spacing.l)
                ) {
                    Text(
                        text = "입장하기",
                        color = Color.Black,
                        style = DitoTypography.headlineSmall
                    )
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateGroupNameDialog(
            initialGroupName = uiState.groupName,
            onDismiss = {
                viewModel.onDialogClose()
            },
            onNavigateNext = { groupName ->
                viewModel.onNavigateToChallenge(groupName)
            }
        )
    }

    if (uiState.showChallengeDialog) {
        CreateChallengeDialog(
            groupName = uiState.groupName,
            onDismiss = {
                viewModel.onBackToNameDialog()
            },
            onCreateChallenge = { name, goal, penalty, period, bet ->
                viewModel.onChallengeCreated(name, goal, penalty, period, bet)
            }
        )
    }

    if (uiState.showJoinDialog) {
        JoinWithCodeDialog(
            onDismiss = {
                viewModel.onDialogClose()
            },
            onJoinWithCode = { inviteCode ->
                viewModel.joinGroupWithCode(inviteCode)
            }
        )
    }

    if (uiState.showBetInputDialog) {
        JoinGroupInfoDialog(
            groupName = uiState.joinedGroupName,
            goal = uiState.joinedGroupGoal,
            penalty = uiState.joinedGroupPenalty,
            period = uiState.joinedGroupPeriod,
            onDismiss = {
                viewModel.onDialogClose()
            },
            onConfirm = { betAmount ->
                viewModel.enterGroupWithBet(betAmount)
            }
        )
    }
}

@Composable
fun ChallengeSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "챌린지 시작!",
                style = DitoCustomTextStyles.titleDLarge,
                color = OnPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = "함께 디지털 휴식에 도전해요",
                style = DitoCustomTextStyles.titleKMedium,
                color = OnPrimary
            )
        }
    }
}

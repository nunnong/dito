package com.dito.app.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dito.app.MainActivity
import com.dito.app.PermissionTestScreen
import com.dito.app.MainScreen
import com.dito.app.feature.auth.AuthViewModel
import com.dito.app.feature.auth.LoginScreen
import com.dito.app.feature.auth.SignUpCredentialsScreen
import com.dito.app.feature.auth.SignUpJobScreen
import com.dito.app.feature.auth.SignUpPermissionScreen
import com.dito.app.feature.auth.SignUpProfileScreen
import com.dito.app.feature.group.ChallengeResultRoute
import com.dito.app.feature.splash.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun DitoNavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.path,
    deepLinkUri: Uri? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1) Splash → 1.2초 후 Login으로 이동
        composable(Route.Splash.path) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(1200)
                navController.navigate(Route.Login.path) {
                    popUpTo(Route.Splash.path) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        // 2) 로그인 화면
        composable(Route.Login.path) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.path){
                        popUpTo(Route.Login.path){ inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Route.SignupCredential.path)
                }
            )
        }

        // 3) 회원가입 1단계: 아이디/비밀번호 입력
        composable(Route.SignupCredential.path) {
            SignUpCredentialsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { username, password ->
                    navController.navigate(Route.SignUpProfile.createRoute(username, password))
                }
            )
        }

        // 4) 회원가입 2단계: 프로필 정보 입력
        composable(
            route = Route.SignUpProfile.path,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            SignUpProfileScreen(
                username = username,
                password = password,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { user, pass, nickname, birthYear, birthMonth, birthDay, gender ->
                    navController.navigate(
                        Route.SignUpJob.createRoute(
                            user,
                            pass,
                            nickname,
                            birthYear,
                            birthMonth,
                            birthDay,
                            gender
                        )
                    )
                }
            )
        }

        // 5) 회원가입 3단계: 직업 선택
        composable(
            route = Route.SignUpJob.path,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType },
                navArgument("nickname") { type = NavType.StringType },
                navArgument("birthYear") { type = NavType.IntType },
                navArgument("birthMonth") { type = NavType.IntType },
                navArgument("birthDay") { type = NavType.IntType },
                navArgument("gender") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            val birthYear = backStackEntry.arguments?.getInt("birthYear") ?: 1990
            val birthMonth = backStackEntry.arguments?.getInt("birthMonth") ?: 1
            val birthDay = backStackEntry.arguments?.getInt("birthDay") ?: 1
            val gender = backStackEntry.arguments?.getString("gender") ?: ""

            SignUpJobScreen(
                username = username,
                password = password,
                nickname = nickname,
                birthYear = birthYear,
                birthMonth = birthMonth,
                birthDay = birthDay,
                gender = gender,
                onNavigateBack = { navController.popBackStack() },
                onSignUpComplete = { user, pass, nick, year, month, day, gend, job ->
                    navController.navigate(
                        Route.SignUpPermission.createRoute(
                            user,
                            pass,
                            nick,
                            year,
                            month,
                            day,
                            gend,
                            job
                        )
                    )
                }
            )
        }

        // 6) 회원가입 4단계: 권한 허용
        composable(
            route = Route.SignUpPermission.path,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType },
                navArgument("nickname") { type = NavType.StringType },
                navArgument("birthYear") { type = NavType.IntType },
                navArgument("birthMonth") { type = NavType.IntType },
                navArgument("birthDay") { type = NavType.IntType },
                navArgument("gender") { type = NavType.StringType },
                navArgument("job") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            val birthYear = backStackEntry.arguments?.getInt("birthYear") ?: 1990
            val birthMonth = backStackEntry.arguments?.getInt("birthMonth") ?: 1
            val birthDay = backStackEntry.arguments?.getInt("birthDay") ?: 1
            val gender = backStackEntry.arguments?.getString("gender") ?: ""
            val job = backStackEntry.arguments?.getString("job") ?: ""

            SignUpPermissionScreen(
                username = username,
                password = password,
                nickname = nickname,
                birthYear = birthYear,
                birthMonth = birthMonth,
                birthDay = birthDay,
                gender = gender,
                job = job,
                onNavigateBack = { navController.popBackStack() },
                onPermissionsGranted = { user, pass, nick, year, month, day, gend, jobType ->
                    // 회원가입 API 호출은 ViewModel에서 이미 완료됨
                    // 성공 시 Home 화면으로 이동
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 7) 메인 화면 (Home) - 딥링크 파싱해서 MainScreen으로 전달
        composable(Route.Home.path) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val context = LocalContext.current   // 🆕 추가

            // deepLinkUri가 변할 때만 파싱 (remember 사용)
            val deepLinkInfo = remember(deepLinkUri) {
                parseDeepLink(deepLinkUri)
            }

            android.util.Log.d("NavGraph", "🔄 MainScreen composable (deepLinkUri=${deepLinkUri})")
            android.util.Log.d("NavGraph", "   initialMissionId: ${deepLinkInfo.missionId}")
            android.util.Log.d("NavGraph", "   initialNavigateTo: ${deepLinkInfo.navigateTo}")
            android.util.Log.d("NavGraph", "   initialOpenDetail: ${deepLinkInfo.openDetail}")

            // 🆕 여기 추가: 딥링크 한 번 소비하고 나면 MainActivity 쪽 state를 비워버리기
            LaunchedEffect(deepLinkUri) {
                if (deepLinkUri != null) {
                    (context as? MainActivity)?.clearDeepLink()
                }
            }

            MainScreen(
                onLogout = {
                    authViewModel.logout(
                        onSuccess = {
                            navController.navigate(Route.Login.path) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                },
                outerNavController = navController,
                // 딥링크에서 파싱한 navigation 정보
                initialNavigateTo = deepLinkInfo.navigateTo,
                initialMissionId = deepLinkInfo.missionId,
                initialOpenMissionDetail = deepLinkInfo.openDetail
            )
        }

        // 8) 권한 재확인 화면
        composable(Route.PermissionRecheck.path) {
            SignUpPermissionScreen(
                mode = com.dito.app.feature.auth.PermissionScreenMode.RECHECK,
                onPermissionsRecheckComplete = {
                    navController.popBackStack()
                }
            )
        }

        // 9) 테스트 화면 (권한 테스트)
        composable(Route.Test.path) {
            PermissionTestScreen()
        }

        // 10) 챌린지 결과 화면
        composable(Route.ChallengeResult.path) {
            ChallengeResultRoute(
                onClose = { navController.popBackStack() }
            )
        }
    }
}

/**
 * 딥링크 파싱 결과를 담는 데이터 클래스
 */
data class DeepLinkInfo(
    val navigateTo: String? = null,
    val missionId: String? = null,
    val openDetail: Boolean = false
)

/**
 * 딥링크 URI를 파싱하여 navigation 정보 추출
 *
 * @param deepLinkUri 딥링크 URI (예: dito://mission/7 또는 dito://mission/7?openDetail=true)
 * @return DeepLinkInfo
 *
 * 지원하는 딥링크:
 * - dito://mission/{missionId} → 미션 알림 화면으로 이동
 * - dito://mission/{missionId}?openDetail=true → 미션 알림 화면 + 모달 자동 열기
 */
private fun parseDeepLink(deepLinkUri: Uri?): DeepLinkInfo {
    if (deepLinkUri == null) {
        return DeepLinkInfo()
    }

    android.util.Log.d("NavGraph", "🔍 딥링크 파싱 시작")
    android.util.Log.d("NavGraph", "   URI: $deepLinkUri")
    android.util.Log.d("NavGraph", "   host: ${deepLinkUri.host}")

    return when (deepLinkUri.host) {
        "mission" -> {
            val missionId = deepLinkUri.lastPathSegment  // "7"
            val openDetail = deepLinkUri.getQueryParameter("openDetail") == "true"

            android.util.Log.d("NavGraph", "   missionId: $missionId")
            android.util.Log.d("NavGraph", "   openDetail query param: ${deepLinkUri.getQueryParameter("openDetail")}")
            android.util.Log.d("NavGraph", "   openDetail (parsed): $openDetail")

            val result = DeepLinkInfo(
                navigateTo = "mission_notifications",
                missionId = missionId,
                openDetail = openDetail
            )

            android.util.Log.d("NavGraph", "✅ 딥링크 파싱 완료")
            android.util.Log.d("NavGraph", "   navigateTo: ${result.navigateTo}")
            android.util.Log.d("NavGraph", "   missionId: ${result.missionId}")
            android.util.Log.d("NavGraph", "   openDetail: ${result.openDetail}")

            result
        }
        else -> {
            android.util.Log.d("NavGraph", "⚠️ 알 수 없는 딥링크 호스트")
            DeepLinkInfo()
        }
    }
}
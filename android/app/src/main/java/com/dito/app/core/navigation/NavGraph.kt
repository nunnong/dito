package com.dito.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dito.app.feature.auth.AuthViewModel
import com.dito.app.feature.splash.SplashScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dito.app.feature.auth.SignUpCredentialsScreen
import com.dito.app.feature.auth.LoginScreen
import com.dito.app.feature.auth.SignUpProfileScreen
import com.dito.app.feature.auth.SignUpJobScreen
import com.dito.app.feature.home.HomeScreen // Added import for HomeScreen
import kotlinx.coroutines.delay

@Composable
fun DitoNavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.path,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1) Splash → 1.2초 후 Login으로 이동
        composable(Route.Splash.path) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2020) // Increased delay for smoother transition
                navController.navigate(Route.Login.path) {
                    popUpTo(Route.Splash.path) { inclusive = true } // 뒤로가기 시 스플래시로 못 돌아가게
                    launchSingleTop = true
                }
            }
        }

        // 2) 로그인 화면
        composable(Route.Login.path) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.path) { // Uncommented and enabled navigation to Home
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Route.Signup.path)
                }
            )
        }

        // 3) 회원가입 1단계: 아이디/비밀번호 입력
        composable(Route.Signup.path) {
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
                    // TODO: 나중에 권한 화면으로 이동하거나 회원가입 API 호출
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 6) 메인 화면 (Home) - Added new composable block for HomeScreen
        composable(Route.Home.path) {
            val authViewModel: AuthViewModel = hiltViewModel()
            HomeScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Route.Login.path) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

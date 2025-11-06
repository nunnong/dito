package com.dito.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dito.app.MainActivity
import com.dito.app.MainScreen
import com.dito.app.feature.splash.SplashScreen
import com.dito.app.feature.auth.LoginScreen
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
                delay(1200)
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
                    // 테스트용: 로그인 성공 시 Test 화면으로 이동
                    navController.navigate(Route.Test.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    // TODO: 회원가입 라우트 추가시 열기
                    // navController.navigate(Route.SignUp.path)
                }
            )
        }

        // 3) 테스트 화면 (권한 설정, Realm 확인 등)
        composable(Route.Test.path) {
            val activity = LocalContext.current as MainActivity
            MainScreen(
                activity = activity,
                onNavigateToHealth = {}
            )
        }
    }
}

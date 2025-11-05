package com.dito.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dito.app.feature.auth.AuthViewModel
import com.dito.app.feature.splash.SplashScreen
import com.dito.app.feature.auth.LoginScreen
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
                    // TODO: 회원가입 라우트 추가시 열기
                    // navController.navigate(Route.SignUp.path)
                }
            )
        }

        // 3) 메인 화면 (Home) - Added new composable block for HomeScreen
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

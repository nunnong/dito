package com.dito.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dito.app.MainActivity
import com.dito.app.MainScreen
import com.dito.app.feature.auth.AuthViewModel
import com.dito.app.feature.auth.LoginScreen
import com.dito.app.feature.auth.SignUpCredentialsScreen
import com.dito.app.feature.auth.SignUpJobScreen
import com.dito.app.feature.auth.SignUpPermissionScreen
import com.dito.app.feature.auth.SignUpProfileScreen
import com.dito.app.feature.shop.ShopScreen
import com.dito.app.feature.splash.SplashScreen
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
//                    navController.navigate(Route.Test.path) {
//                        popUpTo(Route.Login.path) { inclusive = true }
//                        launchSingleTop = true
//                    }
                    // 로그인 성공 시 Home 화면으로 이동
                    navController.navigate(Route.Home.path){
                        popUpTo(Route.Login.path){ inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    // TODO: 회원가입 라우트 추가시 열기
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

        // 6) 메인 화면 (Home)
        composable(Route.Home.path) {
            val authViewModel: AuthViewModel = hiltViewModel()
            MainScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Route.Login.path) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToShop = {
                    navController.navigate(Route.Shop.path)
                }
            )
        }

        // ShopScreen Composable
        composable(Route.Shop.path) {
            ShopScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 7) 테스트 화면 (권한 설정, Realm 확인 등)
        composable(Route.Test.path) {
            val activity = LocalContext.current as MainActivity
            MainScreen(
                activity = activity,
                onNavigateToHealth = {}
            )
        }
    }
}

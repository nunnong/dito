package com.dito.app.core.navigation

sealed class Route(val path: String) {
    // Auth / Onboarding
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Signup : Route("signup")
    data object SignUpProfile : Route("signup_profile/{username}/{password}") {
        fun createRoute(username: String, password: String) = "signup_profile/$username/$password"
    }
    data object SignUpJob : Route("signup_job/{username}/{password}/{nickname}/{birthYear}/{birthMonth}/{birthDay}/{gender}") {
        fun createRoute(
            username: String,
            password: String,
            nickname: String,
            birthYear: Int,
            birthMonth: Int,
            birthDay: Int,
            gender: String
        ) = "signup_job/$username/$password/$nickname/$birthYear/$birthMonth/$birthDay/$gender"
    }
    data object SignUpPermission : Route("signup_permission/{username}/{password}/{nickname}/{birthYear}/{birthMonth}/{birthDay}/{gender}/{job}") {
        fun createRoute(
            username: String,
            password: String,
            nickname: String,
            birthYear: Int,
            birthMonth: Int,
            birthDay: Int,
            gender: String,
            job: String
        ) = "signup_permission/$username/$password/$nickname/$birthYear/$birthMonth/$birthDay/$gender/$job"
    }
//    data object Tutorial : Route("tutorial")

    // Main
    data object Home : Route("home")
    data object Shop : Route("shop")
    data object Closet : Route("closet")
    data object  Missions : Route("missions")
}

package com.dito.app.core.navigation

sealed class Route(val path: String) {
    // Auth / Onboarding
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Signup : Route("signup")
    data object SignUpProfile : Route("signup_profile/{username}/{password}") {
        fun createRoute(username: String, password: String) = "signup_profile/$username/$password"
    }
//    data object Tutorial : Route("tutorial")

    // Main
    data object Home : Route("home")
    data object Shop : Route("shop")
    data object Closet : Route("closet")
    data object  Missions : Route("missions")
}

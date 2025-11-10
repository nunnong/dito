package com.dito.app.core.navigation

sealed class Route(val path: String) {
    // Auth / Onboarding
    data object Splash : Route("splash")
    data object Login : Route("login")

    data object SignupCredential : Route("signup_credential")
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

    // Main
    data object Home : Route("home")
    data object Closet : Route("closet")
    data object Shop : Route("shop")
    data object MissionNotification : Route("mission_notification")
    data object PermissionRecheck : Route("permission_recheck")

    // Test
    data object Test : Route("test")

    // Group
    data object GroupRoot : Route("group")
    data object GroupName : Route("group_name")
    data object GroupCreateInfo : Route("group_info")
    data object GroupWaitingRoomOwner : Route("waiting_room_owner")
    data object GroupEnterInviteCode : Route("enter_invite_code")
    data object GroupIdentifyRoomInfo : Route("identify_room_info")
    data object GroupWaitingRoomGuest : Route("waiting_room_guest")
    data object GroupOngoingChallenge : Route("ongoing_challenge")
    data object GroupChallengeReport : Route("challenge_report")
    data object ChallengeResult : Route("challenge_result")
   // Setting
    data object SettingRoot : Route("setting")
    data object SettingEditNickname : Route("edit_nickname")
    data object SettingEditNotiCount : Route("edit_noti_count")
    data object SettingPrivacyPolicy : Route("privacy_policy")
    data object SettingTermsOfService : Route("terms_of_service")
    






}

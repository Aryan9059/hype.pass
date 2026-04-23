package com.pass.hype.navigation

sealed class Screen(val route: String) {
    object OnBoarding : Screen("onBoarding")
    object PinScreen : Screen("pinScreen")
    object MainScreen : Screen("mainScreen")
    data object AddPassword : Screen("add_Password")
    object BioScreen :  Screen("bioScreen")
    object Settings : Screen("settings") // New Settings route
    data object EditPassword : Screen("edit_password/{passwordId}") {
        fun createRoute(passwordId: Int) = "edit_password/$passwordId"
    }
}
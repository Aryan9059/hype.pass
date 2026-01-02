package com.pass.hype.navigation

sealed class Screen(val route: String) {
    object OnBoarding : Screen("onBoarding")
    object PinScreen : Screen("pinScreen")
    object MainScreen : Screen("mainScreen")
    object AddPassword : Screen("addPassword")
    object BioScreen :  Screen("bioScreen")
}
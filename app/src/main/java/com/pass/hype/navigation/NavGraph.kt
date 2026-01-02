package com.pass.hype.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation. core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx. compose.runtime.Composable
import androidx.compose. ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation. compose.NavHost
import androidx.navigation. compose.composable
import com.pass.hype.presentation.home.HomeScreen
import com. pass.hype. presentation.biometric.BiometricScreen
import com. pass.hype. presentation.boarding.OnBoardPinScreen
import com.pass.hype.presentation.boarding.OnBoarding
import com.pass. hype.presentation. cards.CardViewModel
import com.pass.hype.presentation.passwords.PasswordViewModel

private const val ANIMATION_DURATION = 400

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    passwordViewModel:  PasswordViewModel,
    cardViewModel: CardViewModel,
    isBoarding: Boolean,
    pinLength: Int,
    storedPin: String,
    isFingerprintEnabled: Boolean,
    onBioScreenEntered: () -> Unit,
    onOnBoardingClicked: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.PinScreen.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            OnBoardPinScreen(navController)
        }

        composable(
            route = Screen.OnBoarding.route,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection. Start,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            Scaffold { paddingValues ->
                OnBoarding(
                    modifier = Modifier.padding(paddingValues),
                    onClicked = onOnBoardingClicked,
                    isBoarding = isBoarding
                )
            }
        }

        composable(
            route = Screen.MainScreen.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            HomeScreen(
                passwordViewModel = passwordViewModel,
                cardViewModel = cardViewModel
            )
        }

        composable(
            route = Screen.AddPassword.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection. Start,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            // TODO:  Implement AddPasswordScreen
        }

        composable(
            route = Screen.BioScreen. route,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection. Start,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            onBioScreenEntered()
            BiometricScreen(
                length = pinLength,
                correctPin = storedPin,
                isFingerprintEnabled = isFingerprintEnabled,
                navController = navController
            )
        }
    }
}
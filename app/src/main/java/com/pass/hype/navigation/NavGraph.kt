package com.pass.hype.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pass.hype.data.room.model.Passwords
import com.pass.hype.presentation.biometric.BiometricScreen
import com.pass.hype.presentation.boarding.OnBoardPinScreen
import com.pass.hype.presentation.boarding.OnBoarding
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.home.HomeScreen
import com.pass.hype.presentation.passwords.AddPasswordScreen
import com.pass.hype.presentation.passwords.PasswordViewModel

private const val ANIMATION_DURATION = 400
private const val FADE_DURATION = 200

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    passwordViewModel: PasswordViewModel,
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
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. PIN Screen ---
        composable(
            route = Screen.PinScreen.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope. SlideDirection.Right,
                    tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope. SlideDirection.Right,
                    tween(ANIMATION_DURATION)
                )
            }
        ) {
            OnBoardPinScreen(navController)
        }

        // --- 2. Onboarding ---
        composable(
            route = Screen.OnBoarding. route,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection. Left,
                    tween(ANIMATION_DURATION)
                ) + fadeOut(tween(ANIMATION_DURATION))
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

        // --- 3. Main Screen (Home) ---
        composable(
            route = Screen.MainScreen.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                fadeIn(tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(tween(ANIMATION_DURATION))
            }
        ) {
            HomeScreen(
                navController = navController,
                passwordViewModel = passwordViewModel,
                cardViewModel = cardViewModel
            )
        }

        // --- 4. Add Password ---
        composable(
            route = Screen.AddPassword.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            AddPasswordScreen(
                existingPassword = null,
                onSave = { password ->
                    passwordViewModel.addPassword(password)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- 5. Edit Password ---
        composable(
            route = Screen.EditPassword. route,
            arguments = listOf(
                navArgument("passwordId") {
                    type = NavType.IntType
                }
            ),
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getInt("passwordId") ?: return@composable

            // Find password from the list
            var password by remember { mutableStateOf<Passwords?>(null) }

            LaunchedEffect(passwordId) {
                password = passwordViewModel.getPasswordById(passwordId)
            }

            // Also try to get from the current list as fallback
            val passwordFromList = passwordViewModel.passwordsList.value?. find {
                it.passwordId == passwordId
            }

            val finalPassword = password ?:  passwordFromList

            if (finalPassword != null) {
                AddPasswordScreen(
                    existingPassword = finalPassword,
                    onSave = { updatedPassword ->
                        passwordViewModel.updatePassword(updatedPassword)
                        navController.popBackStack()
                    },
                    onBack = {
                        navController. popBackStack()
                    }
                )
            }
        }

        // --- 6. Biometric Screen ---
        composable(
            route = Screen. BioScreen.route,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection. Left,
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
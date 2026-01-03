package com.pass.hype.presentation.home

import androidx.compose.animation.core. Animatable
import androidx.compose.animation. core.tween
import androidx. compose.foundation.layout. Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout. size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx. compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx. compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose. runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime. mutableIntStateOf
import androidx.compose.runtime. mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics. graphicsLayer
import androidx.compose.ui. input.nestedscroll.nestedScroll
import androidx. compose.ui.res.painterResource
import androidx.compose. ui.text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose. ui.unit.dp
import androidx.navigation.NavController
import com.pass.hype.R
import com.pass.hype.data.room.model.Passwords
import com.pass.hype.navigation.Screen
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.cards.CardsScreen
import com.pass. hype.components.dialog.AddCardDialog
import com. pass.hype. presentation.passwords.PasswordViewModel
import com.pass.hype.presentation.passwords.PasswordsScreen
import com.pass.hype.presentation.settings. SettingsScreen

private data class NavItem(
    val label: String,
    val iconRes: Int
)

private val navItems = listOf(
    NavItem("Passwords", R. drawable.password),
    NavItem("Cards", R.drawable. card),
    NavItem("Settings", R.drawable.settings)
)

private object NavIndex {
    const val PASSWORDS = 0
    const val CARDS = 1
    const val SETTINGS = 2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel
) {
    // Navigation state for bottom bar
    var selectedNavIndex by rememberSaveable { mutableIntStateOf(NavIndex. PASSWORDS) }

    // Card dialog state
    var isCardDialogOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            HomeFab(
                selectedIndex = selectedNavIndex,
                onPasswordClick = {
                    // Navigate to AddPassword screen
                    navController.navigate(Screen.AddPassword.route)
                },
                onCardClick = {
                    isCardDialogOpen = true
                }
            )
        },
        bottomBar = {
            HomeBottomBar(
                items = navItems,
                selectedIndex = selectedNavIndex,
                onItemSelected = { selectedNavIndex = it },
            )
        }
    ) { paddingValues ->
        HomeContent(
            modifier = Modifier.padding(paddingValues),
            selectedIndex = selectedNavIndex,
            passwordViewModel = passwordViewModel,
            cardViewModel = cardViewModel,
            onEditPassword = { password ->
                // Navigate to EditPassword screen with password ID
                navController. navigate(Screen.EditPassword.createRoute(password.passwordId))
            }
        )
    }

    // Card Dialog
    AddCardDialog(
        isOpen = isCardDialogOpen,
        existingCard = null,
        onDismiss = { isCardDialogOpen = false },
        onSave = { card ->
            cardViewModel.addCard(card)
            isCardDialogOpen = false
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeBottomBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected:  (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}

@Composable
private fun HomeFab(
    selectedIndex: Int,
    onPasswordClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier:  Modifier = Modifier
) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(selectedIndex) {
        alphaAnimation. snapTo(0f)
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300))
    }

    if (selectedIndex == NavIndex. PASSWORDS || selectedIndex == NavIndex. CARDS) {
        val isPassword = selectedIndex == NavIndex. PASSWORDS
        val text = if (isPassword) "Add Password" else "Add Card"
        val iconRes = if (isPassword) R.drawable.create else R. drawable.add
        val onClick = if (isPassword) onPasswordClick else onCardClick

        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = modifier.graphicsLayer { alpha = alphaAnimation.value }
        ) {
            Row(Modifier.padding(horizontal = 12.dp)) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = text,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    fontFamily = FontFamily(Font(R.font.password))
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier:  Modifier = Modifier,
    selectedIndex:  Int,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel,
    onEditPassword:  (Passwords) -> Unit
) {
    when (selectedIndex) {
        NavIndex.PASSWORDS -> PasswordsScreen(
            modifier = modifier,
            viewModel = passwordViewModel,
            onEditPassword = onEditPassword
        )
        NavIndex.CARDS -> CardsScreen(
            modifier = modifier,
            viewModel = cardViewModel
        )
        NavIndex. SETTINGS -> SettingsScreen(
            modifier = modifier
        )
    }
}
package com.pass.hype.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pass.hype.R
import com.pass.hype.data.room.model.Passwords
import com.pass.hype.navigation.Screen
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.cards.CardsScreen
import com.pass.hype.components.dialog.AddCardDialog
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.presentation.passwords.PasswordsScreen
import kotlin.collections.emptyList

private data class NavItem(
    val label: String,
    val iconRes: Int
)

private val navItems = listOf(
    NavItem("Dashboard", R.drawable.app), // Reused 'app.png' as dashboard icon
    NavItem("Passwords", R.drawable.password),
    NavItem("Cards", R.drawable.card)
)

private object NavIndex {
    const val DASHBOARD = 0
    const val PASSWORDS = 1
    const val CARDS = 2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel
) {
    // Navigation state for bottom bar (Defaults to Dashboard now)
    var selectedNavIndex by rememberSaveable { mutableIntStateOf(NavIndex.DASHBOARD) }

    // Card dialog state
    var isCardDialogOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hype Pass",
                        fontFamily = FontFamily(Font(R.font.heading)),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            painter = painterResource(R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            HomeFab(
                selectedIndex = selectedNavIndex,
                onPasswordClick = {
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
                navController.navigate(Screen.EditPassword.createRoute(password.passwordId))
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
    onItemSelected: (Int) -> Unit,
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
    modifier: Modifier = Modifier
) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(selectedIndex) {
        alphaAnimation.snapTo(0f)
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300))
    }

    // Show FAB on all screens. Dashboard defaults to adding a password.
    val isCardScreen = selectedIndex == NavIndex.CARDS
    val text = if (isCardScreen) "Add Card" else "Add Password"
    val iconRes = if (isCardScreen) R.drawable.add else R.drawable.create
    val onClick = if (isCardScreen) onCardClick else onPasswordClick

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

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel,
    onEditPassword: (Passwords) -> Unit
) {
    when (selectedIndex) {
        NavIndex.DASHBOARD -> DashboardScreen(
            modifier = modifier,
            passwordViewModel = passwordViewModel,
            cardViewModel = cardViewModel
        )
        NavIndex.PASSWORDS -> PasswordsScreen(
            modifier = modifier,
            viewModel = passwordViewModel,
            onEditPassword = onEditPassword
        )
        NavIndex.CARDS -> CardsScreen(
            modifier = modifier,
            viewModel = cardViewModel
        )
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel
) {
    // Basic implementation for the new Dashboard layout
    val scrollState = rememberScrollState()

    // In a real scenario, you'd observe counts from ViewModels
    val totalPasswords = passwordViewModel.passwordsList.observeAsState(initial = emptyList()).value.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Security Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Overall Security Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "85", // Replace with logic to calculate dynamic score
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = " / 100",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
            }
        }

        // Vault Summary Overview
        Text(
            text = "Vault Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Passwords summary
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(painterResource(R.drawable.password), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalPasswords", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Passwords", style = MaterialTheme.typography.bodySmall)
                }
            }
            // Cards summary
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(painterResource(R.drawable.card), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "0", style = MaterialTheme.typography.headlineMedium) // Replace with dynamic card size
                    Text(text = "Bank Cards", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Suggested Actions Section
        Text(
            text = "Suggested Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.backup),
                        contentDescription = "Backup",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Backup your vault", fontWeight = FontWeight.SemiBold)
                        Text(text = "Ensure you don't lose your data", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Leave space for FAB
    }
}
package com.pass.hype.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pass.hype.R
import com.pass.hype.data.local.cards.Cards
import com.pass.hype.data.local.passwords.Passwords
import com.pass.hype.navigation.NavItem
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.cards.CardsScreen
import com.pass.hype.presentation.components.dialog.AddCardDialog
import com.pass.hype.presentation.components.dialog.AddPasswordDialog
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.presentation.passwords.PasswordsScreen
import com.pass.hype.presentation.settings.SettingsScreen
import com.pass.hype.utils.generateStrongPassword
import com.pass.hype.utils.isStrongPassword

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(passwordViewModel: PasswordViewModel, cardViewModel: CardViewModel) {
    var isCardDialogOpen by rememberSaveable { mutableStateOf(false) }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }

    var isAddPasswordOpen by rememberSaveable { mutableStateOf(false) }
    var app by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AddCardDialog(
        isOpen = isCardDialogOpen,
        cardName = cardName,
        cardNumber = cardNumber,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        cvv = cvv,
        company = company,
        onDismissRequest = {
            isCardDialogOpen = false
            cardName = ""
            cardNumber = ""
            expiryMonth = ""
            expiryYear = ""
            cvv = ""
            company = ""
        },
        onConfirmButtonClick = {
            val cardColorList = listOf("#FF46263a", "#ff633664", "#ff3d4758")
            cardViewModel.addCard(
                Cards(
                    baseColor = cardColorList.random(),
                    cardNumber = cardNumber,
                    cardHolder = cardName,
                    expires = "$expiryMonth/$expiryYear",
                    cvv = cvv
                )
            )
            isCardDialogOpen = false
            cardName = ""
            cardNumber = ""
            expiryMonth = ""
            expiryYear = ""
            cvv = ""
            company = ""
        },
        onNameChanged = {cardName = it},
        onNumberChanged = {
            cardNumber = it.take(16)
            if (cardNumber.isNotBlank()){
                company = if (cardNumber.first() == '5' || cardNumber.first() == '2') {
                    "MASTERCARD"
                } else if (cardNumber.first() == '4') {
                    "VISA"
                } else if (cardNumber.first() == '3') {
                    "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS"
                } else {
                    "OTHER"
                }
            }
        },
        onExpiryMonthChanged = {expiryMonth = it.take(2)},
        onExpiryYearChanged = {expiryYear = it.take(2)},
        onCvvChanged = {cvv = it.take(3)},
        onCompanyChanged = {company = it},
        edit = false
    )

    var selectedNavItem by rememberSaveable {
        mutableIntStateOf(0)
    }

    val navItemList = listOf(
        NavItem("Passwords", Icons.Default.Password),
        NavItem("Cards", Icons.Default.CreditCard),
        NavItem("Settings", Icons.Default.Settings)
    )

    val bottomAppScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .nestedScroll(bottomAppScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            val alphaAnimation = remember { Animatable(initialValue = 0f) }

            LaunchedEffect(100) {
                alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
            }

            if (selectedNavItem == 0 || selectedNavItem == 1) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.graphicsLayer {
                        alpha = alphaAnimation.value
                    },
                    onClick = {
                        if (selectedNavItem == 0){
                            isAddPasswordOpen = true
                        } else {
                            isCardDialogOpen = true
                        }

                    }) {
                    Row(Modifier.padding(horizontal = 12.dp)) {
                        if (selectedNavItem == 0){
                            Icon(
                                painter = painterResource(R.drawable.create_icon),
                                modifier = Modifier.size(20.dp),
                                contentDescription = "Add Password"
                            )
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                modifier = Modifier.size(20.dp),
                                contentDescription = "Add Card"
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(modifier = Modifier.padding(end = 4.dp), text = if (selectedNavItem == 0) "Create Password" else "Add a Card")
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(scrollBehavior = bottomAppScrollBehavior) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedNavItem == index,
                        onClick = {
                            selectedNavItem = index
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label
                            )
                        },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        },
    ){ paddingValues ->
        SelectedScreen(Modifier.padding(paddingValues), selectedIndex = selectedNavItem, passwordViewModel, cardViewModel)
    }

    AddPasswordDialog(
        passStrength =
            if (password.isStrongPassword() == 0 || password.length <= 4) "Weakest"
            else if (password.isStrongPassword() == 1 || password.length <= 8) "Weak"
            else if (password.isStrongPassword() == 2 || password.length <= 12) "Moderate"
            else if (password.isStrongPassword() == 3 || password.length <= 16) "Strong"
            else "Strongest"
        ,
        app = app,
        email = email,
        isOpen = isAddPasswordOpen,
        password = password,
        onDismiss = {
            email = ""
            password = ""
            app = ""
            isAddPasswordOpen = false
        },
        onConfirm = {
            passwordViewModel.addPassword(
                Passwords(
                    appName = app,
                    appIcon = app.lowercase(),
                    email = email,
                    password = password,
                    editTime = System.currentTimeMillis().toString(),
                    edited = false
                )
            )
            email = ""
            password = ""
            app = ""
            isAddPasswordOpen = false
        },
        onEmailChanged = { email = it },
        onPasswordChanged = { password = it },
        onAppChanged = { app = it },
        onCreateClick = { password = generateStrongPassword() }
    )
}

@Composable
fun SelectedScreen(modifier: Modifier = Modifier, selectedIndex: Int, passwordViewModel: PasswordViewModel, cardViewModel: CardViewModel){
    when(selectedIndex){
        0 -> PasswordsScreen(modifier = modifier, passwordViewModel)
        1 -> CardsScreen(modifier = modifier, cardViewModel)
        2 -> SettingsScreen(modifier = modifier)
    }
}
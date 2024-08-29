package com.pass.hype.presentation.passwords

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import com.pass.hype.presentation.EmptyScreen
import com.pass.hype.presentation.components.PasswordItem
import com.pass.hype.presentation.components.SearchBar

@Composable
fun PasswordsScreen(modifier: Modifier, passwordViewModel: PasswordViewModel) {
    val passwordList by passwordViewModel.passwordsList.observeAsState()

    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(100) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    val textState = remember { mutableStateOf(TextFieldValue("")) }
    val searchedText = textState.value.text

    passwordList?.reversed()?.let{ it ->

        Column {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = alphaAnimation.value },
                hint = "Search Passwords", searchQuery = textState, isListEmpty = passwordList!!.isEmpty())

            if (it.isEmpty()) {
                EmptyScreen(modifier = modifier, screenText = "Tap the + button to add passwords")
            } else if (passwordList!!.none {
                    it.email.contains(searchedText, ignoreCase = true) || it.appName.contains(
                        searchedText,
                        ignoreCase = true
                    )
                }){
                EmptyScreen(modifier = modifier, screenText = "There are no matching passwords")
            }
            else {
                LazyColumn {
                    items(items = passwordList!!.reversed().filter {
                        it.email.contains(searchedText, ignoreCase = true) ||
                                it.appName.contains(searchedText, ignoreCase = true)
                    }, key = { it.passwordId }) { item ->
                        PasswordItem(item = item, Modifier.animateItem())
                    }
                }
            }
        }
    }
}
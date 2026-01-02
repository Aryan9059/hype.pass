package com.pass.hype.presentation.passwords

import androidx.compose. animation.core. Animatable
import androidx.compose.animation. core.tween
import androidx. compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout. Column
import androidx. compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation. layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose. foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose. runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime. livedata.observeAsState
import androidx.compose.runtime. mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui. text.input.TextFieldValue
import androidx. compose.ui.unit.dp
import com.pass.hype.components.EmptyScreen
import com.pass.hype.components.SearchBar
import com.pass.hype.components.dialog.DeleteDialog
import com.pass.hype.components.password.PasswordItem
import com.pass.hype.components.password.SwipeToDeleteContainer

@Composable
fun PasswordsScreen(
    modifier: Modifier = Modifier,
    viewModel: PasswordViewModel
) {
    val passwordList by viewModel.passwordsList.observeAsState(emptyList())
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    // Delete confirmation dialog state
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(500))
    }

    // Filtered and sorted list
    val filteredPasswords by remember(passwordList, searchQuery. text) {
        derivedStateOf {
            val searchText = searchQuery.text.lowercase().trim()
            passwordList
                .filter { password ->
                    searchText. isEmpty() ||
                            password. appName.lowercase().contains(searchText) ||
                            password.email.lowercase().contains(searchText)
                }
                .sortedByDescending { it. editTime. toLongOrNull() ?: 0L }
        }
    }

    val isListEmpty = passwordList.isEmpty()
    val isSearchEmpty = filteredPasswords.isEmpty() && ! isListEmpty

    // Delete confirmation dialog
    DeleteDialog(
        isOpen = showDeleteDialog,
        item = "Password",
        onDelete = {
            pendingDeleteId?.let { id ->
                viewModel.deletePassword(id)
            }
            showDeleteDialog = false
            pendingDeleteId = null
        },
        onDismissRequest = {
            showDeleteDialog = false
            pendingDeleteId = null
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alphaAnimation.value }
    ) {
        // Search Bar
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp),
            hint = "Search passwords.. .",
            searchQuery = searchQuery,
            onValueChange = { searchQuery = it },
            isEnabled = ! isListEmpty,
            isListEmpty = isListEmpty
        )

        Spacer(Modifier.size(12.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isListEmpty -> {
                    EmptyScreen(
                        modifier = Modifier.fillMaxSize(),
                        screenText = "Tap the + button to add passwords"
                    )
                }
                isSearchEmpty -> {
                    EmptyScreen(
                        modifier = Modifier. fillMaxSize(),
                        screenText = "No passwords match \"${searchQuery.text}\""
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredPasswords,
                            key = { it.passwordId }
                        ) { password ->
                            SwipeToDeleteContainer(
                                item = password,
                                onDelete = {
                                    pendingDeleteId = password.passwordId
                                    showDeleteDialog = true
                                }
                            ) { _ ->
                                PasswordItem(
                                    item = password,
                                    onEdit = { updatedPassword ->
                                        viewModel. updatePassword(updatedPassword)
                                    },
                                    onDelete = { id ->
                                        pendingDeleteId = id
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
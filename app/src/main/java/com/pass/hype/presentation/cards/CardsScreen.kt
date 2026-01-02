package com.pass.hype.presentation.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core. Animatable
import androidx.compose.animation. core.tween
import androidx. compose.animation.fadeIn
import androidx. compose.animation.fadeOut
import androidx. compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose. foundation.layout.Column
import androidx.compose.foundation.layout. PaddingValues
import androidx.compose. foundation.layout.Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx. compose.foundation.layout.fillMaxWidth
import androidx.compose. foundation.layout.padding
import androidx.compose.foundation.layout. size
import androidx. compose.foundation.lazy.LazyColumn
import androidx.compose. foundation.lazy.LazyRow
import androidx.compose.foundation. lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose. material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx. compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime. Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime. derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime. livedata.observeAsState
import androidx.compose.runtime. mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime. saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui. text.input.TextFieldValue
import androidx.compose. ui.unit.dp
import com.pass.hype.components.CardItem
import com.pass.hype.components.EmptyScreen
import com.pass.hype.components.SearchBar
import com.pass.hype.components.dialog.AddCardDialog
import com.pass.hype.components.password.SwipeToDeleteContainer
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room. model.CardType
import com.pass. hype.utils. CardUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    modifier: Modifier = Modifier,
    viewModel: CardViewModel
) {
    val cardList by viewModel.cardList.observeAsState(emptyList())
    val uiState by viewModel.uiState. collectAsState()

    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var selectedTypeFilter by rememberSaveable { mutableStateOf<CardType?>(null) }
    var showOnlyFavorites by rememberSaveable { mutableStateOf(false) }
    var showOnlyExpiring by rememberSaveable { mutableStateOf(false) }

    // Edit dialog state
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<Card?>(null) }

    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(500))
    }

    // Filtered and sorted cards
    val filteredCards by remember(cardList, searchQuery. text, selectedTypeFilter, showOnlyFavorites, showOnlyExpiring) {
        derivedStateOf {
            val query = searchQuery.text.lowercase().trim()

            cardList
                .filter { card ->
                    // Search filter
                    val matchesSearch = query.isEmpty() ||
                            card.cardName. lowercase().contains(query) ||
                            card.holderName.lowercase().contains(query) ||
                            card. cardNumber.contains(query) ||
                            card.issuer?.lowercase()?.contains(query) == true

                    // Type filter
                    val matchesType = selectedTypeFilter == null || card.cardType == selectedTypeFilter

                    // Favorites filter
                    val matchesFavorites = ! showOnlyFavorites || card.isFavorite

                    // Expiring filter
                    val matchesExpiring = !showOnlyExpiring ||
                            CardUtils.isExpiringSoon(card. expiryDate) ||
                            CardUtils.isExpired(card.expiryDate)

                    matchesSearch && matchesType && matchesFavorites && matchesExpiring
                }
                .sortedWith(
                    compareByDescending<Card> { it.isPinned }
                        .thenByDescending { it.isFavorite }
                        .thenByDescending { it.updatedAt }
                )
        }
    }

    val isListEmpty = cardList.isEmpty()
    val isFilteredEmpty = filteredCards.isEmpty() && !isListEmpty

    // Edit Dialog
    AddCardDialog(
        isOpen = showEditDialog,
        existingCard = cardToEdit,
        onDismiss = {
            showEditDialog = false
            cardToEdit = null
        },
        onSave = { card ->
            viewModel.updateCard(card)
            showEditDialog = false
            cardToEdit = null
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alphaAnimation. value }
    ) {
        // Search Bar with Filter Toggle
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                modifier = Modifier.weight(1f),
                hint = "Search cards.. .",
                searchQuery = searchQuery,
                onValueChange = { searchQuery = it },
                isEnabled = ! isListEmpty,
                isListEmpty = isListEmpty
            )

            IconButton(
                onClick = { showFilters = !showFilters }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = if (selectedTypeFilter != null || showOnlyFavorites || showOnlyExpiring) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme. colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Filter Chips
        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                // Card Type Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("All") }
                        )
                    }
                    items(CardType.entries. toList()) { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = {
                                selectedTypeFilter = if (selectedTypeFilter == type) null else type
                            },
                            label = { Text(CardUtils. getCardTypeDisplayName(type)) }
                        )
                    }
                }

                // Quick Filters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("Favorites") }
                    )
                    FilterChip(
                        selected = showOnlyExpiring,
                        onClick = { showOnlyExpiring = !showOnlyExpiring },
                        label = { Text("Expiring Soon") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isListEmpty -> {
                    EmptyScreen(
                        modifier = Modifier.fillMaxSize(),
                        screenText = "Tap the + button to add cards"
                    )
                }
                isFilteredEmpty -> {
                    EmptyScreen(
                        modifier = Modifier.fillMaxSize(),
                        screenText = if (searchQuery.text.isNotEmpty()) {
                            "No cards match \"${searchQuery.text}\""
                        } else {
                            "No cards match the selected filters"
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier. fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredCards,
                            key = { it.cardId }
                        ) { card ->
                            SwipeToDeleteContainer(
                                item = card,
                                onDelete = { viewModel.deleteCard(card. cardId) }
                            ) {
                                CardItem(
                                    card = card,
                                    onEdit = {
                                        cardToEdit = card
                                        showEditDialog = true
                                    },
                                    onDelete = { viewModel.deleteCard(card.cardId) },
                                    onToggleFavorite = { viewModel.toggleFavorite(card.cardId) },
                                    onTogglePinned = { viewModel. togglePinned(card.cardId) },
                                    onToggleLock = { viewModel.toggleLock(card.cardId) },
                                    onCardAccess = { viewModel.recordCardAccess(card.cardId) },
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
package com.pass.hype.components.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout. Arrangement
import androidx.compose.foundation. layout.Column
import androidx.compose.foundation.layout. ExperimentalLayoutApi
import androidx.compose.foundation.layout. FlowRow
import androidx.compose.foundation.layout. Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout. height
import androidx.compose.foundation.layout. padding
import androidx. compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose. foundation.text.KeyboardOptions
import androidx. compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3. DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx. compose.material3.ExposedDropdownMenuBox
import androidx. compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose. runtime.LaunchedEffect
import androidx. compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime. remember
import androidx. compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime. setValue
import androidx. compose.ui. Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui. text.input.KeyboardCapitalization
import androidx.compose.ui. text.input.KeyboardType
import androidx.compose.ui. unit.dp
import androidx.compose.ui. window.DialogProperties
import com.pass.hype.R
import com.pass.hype.data.room.model. Card
import com. pass.hype. data.room.model.CardNetwork
import com.pass.hype.data.room.model. CardSubType
import com.pass.hype.data.room.model.CardType
import com.pass.hype.utils.CardUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCardDialog(
    isOpen: Boolean,
    existingCard: Card?  = null,
    onDismiss:  () -> Unit,
    onSave: (Card) -> Unit
) {
    if (!isOpen) return

    val isEditing = existingCard != null

    // State
    var cardName by rememberSaveable { mutableStateOf(existingCard?.cardName ?: "") }
    var selectedCardType by rememberSaveable { mutableStateOf(existingCard?.cardType ?: CardType. FINANCIAL) }
    var selectedSubType by rememberSaveable { mutableStateOf(existingCard?.cardSubType ?: CardSubType. DEBIT_CARD) }
    var cardNumber by rememberSaveable { mutableStateOf(existingCard?.cardNumber ?: "") }
    var holderName by rememberSaveable { mutableStateOf(existingCard?.holderName ?: "") }
    var expiryMonth by rememberSaveable { mutableStateOf(existingCard?.expiryDate?. split("/")?.getOrNull(0) ?: "") }
    var expiryYear by rememberSaveable { mutableStateOf(existingCard?.expiryDate?.split("/")?.getOrNull(1) ?: "") }
    var cvv by rememberSaveable { mutableStateOf(existingCard?.cvv ?: "") }
    var issuer by rememberSaveable { mutableStateOf(existingCard?.issuer ?:  "") }
    var notes by rememberSaveable { mutableStateOf(existingCard?.notes ?: "") }

    // ID Card specific fields
    var dateOfBirth by rememberSaveable { mutableStateOf(existingCard?.dateOfBirth ?:  "") }
    var gender by rememberSaveable { mutableStateOf(existingCard?.gender ?:  "") }
    var address by rememberSaveable { mutableStateOf(existingCard?.address ?: "") }
    var fatherName by rememberSaveable { mutableStateOf(existingCard?.fatherName ?: "") }
    var issueDate by rememberSaveable { mutableStateOf(existingCard?.issueDate ?: "") }
    var validUntil by rememberSaveable { mutableStateOf(existingCard?.validUntil ?:  "") }
    var authorityName by rememberSaveable { mutableStateOf(existingCard?.authorityName ?: "") }

    // Detected network
    var detectedNetwork by remember { mutableStateOf<CardNetwork?>(existingCard?.cardNetwork) }

    // Dropdown states
    var typeExpanded by remember { mutableStateOf(false) }
    var subTypeExpanded by remember { mutableStateOf(false) }

    // Auto-detect card network
    LaunchedEffect(cardNumber) {
        if (selectedCardType == CardType.FINANCIAL && cardNumber.length >= 4) {
            detectedNetwork = CardUtils.detectCardNetwork(cardNumber)
        }
    }

    // Get available subtypes
    val availableSubTypes = CardUtils. getSubTypesForType(selectedCardType)

    // Update subtype when type changes
    LaunchedEffect(selectedCardType) {
        if (! availableSubTypes.contains(selectedSubType)) {
            selectedSubType = availableSubTypes. firstOrNull() ?: CardSubType.CUSTOM
        }
    }

    // Validation
    val isValid = cardName.isNotBlank() && cardNumber.isNotBlank() && holderName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Card" else "Add Card",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Type Selection
                Text(
                    text = "Card Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme. colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedCardType == type,
                            onClick = { selectedCardType = type },
                            label = { Text(CardUtils.getCardTypeDisplayName(type)) }
                        )
                    }
                }

                // Card Subtype
                ExposedDropdownMenuBox(
                    expanded = subTypeExpanded,
                    onExpandedChange = { subTypeExpanded = !subTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = CardUtils.getCardSubTypeDisplayName(selectedSubType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Card Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = subTypeExpanded,
                        onDismissRequest = { subTypeExpanded = false }
                    ) {
                        availableSubTypes.forEach { subType ->
                            DropdownMenuItem(
                                text = { Text(CardUtils. getCardSubTypeDisplayName(subType)) },
                                onClick = {
                                    selectedSubType = subType
                                    subTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Card Name
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name / Nickname") },
                    placeholder = { Text("e.g., My Savings Card") },
                    modifier = Modifier. fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        cardNumber = it. filter { c -> c.isDigit() || c. isLetter() }.take(
                            when (selectedSubType) {
                                CardSubType. AADHAAR -> 12
                                CardSubType.PAN -> 10
                                CardSubType.PASSPORT -> 8
                                else -> 19
                            }
                        )
                    },
                    label = {
                        Text(
                            when (selectedSubType) {
                                CardSubType. AADHAAR -> "Aadhaar Number"
                                CardSubType.PAN -> "PAN Number"
                                CardSubType.DRIVING_LICENSE -> "License Number"
                                CardSubType.VOTER_ID -> "Voter ID"
                                CardSubType. PASSPORT -> "Passport Number"
                                else -> "Card Number"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedCardType == CardType. FINANCIAL)
                            KeyboardType.Number else KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    supportingText = {
                        if (selectedCardType == CardType. FINANCIAL && detectedNetwork != null && detectedNetwork != CardNetwork.UNKNOWN) {
                            Text("Detected:  ${detectedNetwork?. name}")
                        }
                    }
                )

                // Holder Name
                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("Holder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                // Financial Card Fields
                AnimatedVisibility(visible = selectedCardType == CardType.FINANCIAL) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = expiryMonth,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }.take(2)
                                    val month = filtered.toIntOrNull() ?: 0
                                    expiryMonth = if (month > 12) "12" else filtered
                                },
                                label = { Text("MM") },
                                modifier = Modifier. weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = expiryYear,
                                onValueChange = { expiryYear = it. filter { c -> c.isDigit() }.take(2) },
                                label = { Text("YY") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(4) },
                                label = { Text("CVV") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        OutlinedTextField(
                            value = issuer,
                            onValueChange = { issuer = it },
                            label = { Text("Issuing Bank") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // ID Card Fields
                AnimatedVisibility(visible = selectedCardType == CardType.GOVERNMENT_ID) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = { Text("Date of Birth") },
                                placeholder = { Text("DD/MM/YYYY") },
                                modifier = Modifier. weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("Gender") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        if (selectedSubType == CardSubType.AADHAAR) {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )
                        }

                        if (selectedSubType == CardSubType. DRIVING_LICENSE || selectedSubType == CardSubType.PASSPORT) {
                            Row(horizontalArrangement = Arrangement. spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = issueDate,
                                    onValueChange = { issueDate = it },
                                    label = { Text("Issue Date") },
                                    modifier = Modifier. weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = validUntil,
                                    onValueChange = { validUntil = it },
                                    label = { Text("Valid Until") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = authorityName,
                                onValueChange = { authorityName = it },
                                label = { Text("Issuing Authority") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val expiryDate = if (expiryMonth.isNotBlank() && expiryYear. isNotBlank()) {
                        "$expiryMonth/$expiryYear"
                    } else null

                    val card = Card(
                        cardId = existingCard?.cardId ?: 0,
                        cardName = cardName,
                        cardType = selectedCardType,
                        cardSubType = selectedSubType,
                        cardNumber = cardNumber,
                        cardNumberMasked = CardUtils.maskCardNumber(cardNumber),
                        holderName = holderName,
                        expiryDate = expiryDate,
                        cvv = cvv. ifBlank { null },
                        issuer = issuer. ifBlank { null },
                        cardNetwork = detectedNetwork,
                        dateOfBirth = dateOfBirth. ifBlank { null },
                        gender = gender.ifBlank { null },
                        address = address.ifBlank { null },
                        fatherName = fatherName.ifBlank { null },
                        issueDate = issueDate.ifBlank { null },
                        validUntil = validUntil.ifBlank { null },
                        authorityName = authorityName. ifBlank { null },
                        notes = notes,
                        baseColor = existingCard?.baseColor ?: CardUtils.getRandomCardColor(),
                        createdAt = existingCard?.createdAt ?: System. currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(card)
                },
                enabled = isValid
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        }
    )
}
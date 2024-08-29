package com.pass.hype.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.pass.hype.utils.cardList

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDialog(
    edit: Boolean,
    isOpen: Boolean,
    cardName: String,
    cardNumber: String,
    expiryMonth: String,
    expiryYear: String,
    cvv: String,
    company: String,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onExpiryMonthChanged: (String) -> Unit,
    onExpiryYearChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCompanyChanged: (String) -> Unit
) {
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var numberError by rememberSaveable { mutableStateOf<String?>(null) }
    var expiryError by rememberSaveable { mutableStateOf<String?>(null) }
    var cvvError by rememberSaveable { mutableStateOf<String?>(null) }

    nameError = when {
        cardName.isBlank() -> "Please enter the name on your card."
        else -> null
    }
    numberError = when {
        cardNumber.isBlank() -> "Please enter your card number."
        cardNumber.length != 16 -> ""
        else -> null
    }
    expiryError = when {
        expiryMonth.isBlank() || expiryMonth.isBlank() -> "Please enter the expiry of your card."
        else -> null
    }
    cvvError = when {
        cvv.isBlank() -> "Please enter the CVV on your card."
        else -> null
    }

    if (isOpen){
        AlertDialog(
            title = { Text(text = if (edit) "Edit Card" else "Add a Card") },
            onDismissRequest = onDismissRequest,
            text = {
                Column {
                    var expanded by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.size(6.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = cardName,
                        onValueChange = onNameChanged,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        label = { Text(text = "Name on Card") },
                        singleLine = true,
                        maxLines = 1,
                        isError = nameError != null && cardName.isNotBlank(),

                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = cardNumber,
                        visualTransformation = { number ->
                            formatCardNumbers(number)
                        },
                        onValueChange = onNumberChanged,
                        label = { Text(text = "Card Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    Row {

                        OutlinedTextField(
                            modifier = Modifier.weight(1F),
                            value = cvv,
                            onValueChange = onCvvChanged,
                            label = { Text(text = "CVV") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1.8F)
                        ) {
                            OutlinedTextField(
                                value = company,
                                onValueChange = onCompanyChanged,
                                readOnly = true,
                                singleLine = true,
                                label = { Text(text = "Issuer") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                colors = OutlinedTextFieldDefaults.colors(),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.height(216.dp)) {
                                cardList.forEach { option: String ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = option) },
                                        onClick = {
                                            onCompanyChanged(option)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(6.dp))

                    Row {
                        OutlinedTextField(
                            modifier = Modifier.weight(1F),
                            value = expiryMonth,
                            onValueChange = onExpiryMonthChanged,
                            label = { Text(text = "Month") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        OutlinedTextField(
                            modifier = Modifier.weight(1F),
                            value = expiryYear,
                            onValueChange = onExpiryYearChanged,
                            label = { Text(text = "Year") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClick,
                    enabled = nameError == null && numberError == null && expiryError == null && cvvError == null
                ) {
                    Text(text = "Save")
                }
            }
        )
    }
}

fun formatCardNumbers(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
    var out = ""

    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i % 4 == 3 && i != 15) out += " "
    }
    val creditCardOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 7) return offset + 1
            if (offset <= 11) return offset + 2
            if (offset <= 16) return offset + 3
            return 19
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 9) return offset - 1
            if (offset <= 14) return offset - 2
            if (offset <= 19) return offset - 3
            return 16
        }
    }
    return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
}
@file:Suppress("UNUSED_EXPRESSION")

package com.pass.hype.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import com.pass.hype.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pass.hype.data.local.passwords.Passwords
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.utils.appList

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordDialog(
    edit: Boolean,
    isOpen: Boolean,
    app: String,
    email: String,
    password: String,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onAppChanged: (String) -> Unit
) {
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    emailError = when {
        email.isBlank() -> "Please enter your Email/UserID."
        else -> null
    }

    //Required while enabling conditions on passwords.

//    val letter = Pattern.compile("[^A-Za-z]")
//    val digit = Pattern.compile("[0-9]")
//    val special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
//
//    val hasLetter = letter.matcher(password)
//    val hasDigit = digit.matcher(password)
//    val hasSpecial = special.matcher(password)

    passwordError = when {
        password.isBlank() -> "Please enter your Password."
        else -> null
    }

    if (isOpen){
        AlertDialog(
            title = { Text(text = if (edit) "Edit Password" else "Add a Password")},
            onDismissRequest = {
                onDismissRequest },
            text = {
                Column {
                    var expanded by remember { mutableStateOf(false) }

                    val context = LocalContext.current
                    val drawableId = remember(app.lowercase()) {
                        context.resources.getIdentifier(
                            app.lowercase(),
                            "drawable",
                            context.packageName
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                    ) {
                        OutlinedTextField(
                            value = app,
                            onValueChange = onAppChanged,
                            label = { Text(text = "Select App") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            leadingIcon = {
                                if (!appList.contains(app)){
                                    Icon(imageVector = Icons.Default.Android, contentDescription = "App Icon")
                                } else {
                                    Icon(modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onBackground, painter = painterResource(id = drawableId), contentDescription = "App Icon")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.height(216.dp)) {
                            appList.forEach { option: String ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        onAppChanged(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(6.dp))
                    
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = onEmailChanged,
                        label = { Text(text = "Email/UserID")},
                        singleLine = true,
                        maxLines = 1,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AlternateEmail, contentDescription ="Email")
                        },
                        isError = emailError != null && email.isNotBlank(),
                        //supportingText = { Text(text = emailError.orEmpty())}

                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    var passwordVisibility: Boolean by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = onPasswordChanged,
                        label = { Text(text = "Password")},
                        singleLine = true,
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.password))),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisibility)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility

                            val description = if (passwordVisibility) "Hide Password" else "Show Password"

                            IconButton(onClick = {passwordVisibility = !passwordVisibility}){
                                Icon(imageVector = image, description)
                            }
                        },
                        isError = passwordError != null && password.isNotBlank(),
                        //supportingText = { Text(text = passwordError.orEmpty())},
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Password, contentDescription = "Password Text Box Icon")
                        }
                    )
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
                    enabled = emailError == null && passwordError == null
                ) {
                    Text(text = "Save")
                }
            }
        )
    }
}
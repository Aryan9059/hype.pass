package com.pass.hype.presentation.components.dialog

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.pass.hype.R
import com.pass.hype.utils.appList

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordDialog(
    passStrength: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCreateClick: () -> Unit,
    isOpen: Boolean,
    app: String,
    email: String,
    password: String,
    notes: String,
    onEmailChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onAppChanged: (String) -> Unit
) {
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var expand by rememberSaveable { mutableStateOf(false) }

    emailError = when {
        email.isBlank() -> "Please enter your Email/UserID."
        else -> null
    }

    passwordError = when {
        password.isBlank() -> "Please enter your Password."
        else -> null
    }

    expand = isOpen

    if (isOpen){
        AlertDialog (
            title = { Text(text = "Create Password") },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = onDismiss,
            text = {
            Column {
                Spacer(Modifier.size(16.dp))

                val length = password.length
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = "Password Strength",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = passStrength,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally),
                                text = "$length characters",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                        CircularProgressIndicator(
                            progress = {
                                when (passStrength) {
                                    "Weakest" -> 0F
                                    "Weak" -> 0.25F
                                    "Moderate" -> 0.5F
                                    "Strong" -> 0.75F
                                    else -> 1F
                                }
                            },
                            strokeWidth = 12.dp,
                            strokeCap = StrokeCap.Round,
                            trackColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .size(200.dp),
                        )
                    }
                }

                Spacer(Modifier.size(24.dp))

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
                        @Suppress("DEPRECATION")
                        OutlinedTextField(
                            value = app,
                            onValueChange = onAppChanged,
                            label = { Text(text = "Select App") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            leadingIcon = {
                                if (!appList.contains(app)) {
                                    Icon(
                                        imageVector = Icons.Default.Android,
                                        contentDescription = "App Icon"
                                    )
                                } else {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        painter = painterResource(id = drawableId),
                                        contentDescription = "App Icon"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.height(208.dp)
                        ) {
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
                        label = { Text(text = "Email/UserID") },
                        singleLine = true,
                        maxLines = 1,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Email"
                            )
                        },
                        isError = emailError != null && email.isNotBlank(),
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = onPasswordChanged,
                        label = { Text(text = "Password") },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.password))),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = onCreateClick) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.create_icon),
                                    contentDescription = "Create Password"
                                )
                            }
                        },
                        isError = passwordError != null && password.isNotBlank(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Password,
                                contentDescription = "Password Text Box Icon"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = notes,
                        onValueChange = onNotesChanged,
                        label = { Text(text = "Additional Notes") },
                        singleLine = false,
                        maxLines = 3,
                    )
                }
            }
        },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = passwordError == null && emailError == null
                ) {
                    Text(text = "Save")
                }
            }
        )
    }
}
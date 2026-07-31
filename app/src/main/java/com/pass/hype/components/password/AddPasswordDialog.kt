package com.pass.hype.components.password

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.Dialog
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

    val targetValue = when (passStrength) {
        "Very Weak" -> 0F
        "Weak" -> 0.25F
        "Fair" -> 0.5F
        "Good" -> 0.75F
        else -> 1F
    }
    val animatedProgress = animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 500),
        label = "Progress Animation"
    )

    emailError = when {
        email.isBlank() -> "Please enter your Email/UserID."
        else -> null
    }

    passwordError = when {
        password.isBlank() -> "Please enter your Password."
        else -> null
    }

    expand = isOpen

    if (isOpen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create Password",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val length = password.length
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column {
                                    Icon(
                                        painter = painterResource(R.drawable.password),
                                        contentDescription = "Password Strength Icon",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.CenterHorizontally),
                                    )
                                    Text(
                                        modifier = Modifier
                                            .padding(top = 6.dp, bottom = 4.dp)
                                            .align(Alignment.CenterHorizontally),
                                        text = passStrength,
                                        fontFamily = FontFamily(Font(R.font.password)),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(bottom = 10.dp),
                                        text = "$length characters",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                                CircularProgressIndicator(
                                    progress = { animatedProgress.value },
                                    strokeWidth = 12.dp,
                                    strokeCap = StrokeCap.Round,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(200.dp),
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
                                    shape = RoundedCornerShape(12.dp),
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
                                                painter = painterResource(R.drawable.app),
                                                contentDescription = "App Icon",
                                                Modifier.size(24.dp)
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                value = email,
                                onValueChange = onEmailChanged,
                                label = { Text(text = "Email/UserID") },
                                singleLine = true,
                                maxLines = 1,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.email),
                                        contentDescription = "Email",
                                        Modifier.size(24.dp)
                                    )
                                },
                                isError = emailError != null && email.isNotBlank(),
                            )

                            Spacer(modifier = Modifier.size(6.dp))

                            OutlinedTextField(
                                shape = RoundedCornerShape(12.dp),
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
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(R.drawable.create),
                                            contentDescription = "Create Password"
                                        )
                                    }
                                },
                                isError = passwordError != null && password.isNotBlank(),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.password),
                                        contentDescription = "Password Text Box Icon",
                                        Modifier.size(24.dp)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.size(6.dp))

                            OutlinedTextField(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                value = notes,
                                onValueChange = onNotesChanged,
                                label = { Text(text = "Additional Notes") },
                                singleLine = false,
                                maxLines = 3,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = passwordError == null && emailError == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Save", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
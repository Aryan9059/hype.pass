package com.pass.hype.components.password

import android.annotation.SuppressLint
import android. content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx. compose.animation.core. Animatable
import androidx.compose.animation. core.LinearOutSlowInEasing
import androidx. compose.animation.core.animateFloatAsState
import androidx.compose.animation.core. tween
import androidx.compose.animation. expandVertically
import androidx. compose.animation.fadeIn
import androidx. compose.animation.fadeOut
import androidx. compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout. Box
import androidx. compose.foundation.layout.Column
import androidx.compose.foundation. layout.Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose. foundation.layout.size
import androidx.compose.foundation.layout. width
import androidx. compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled. KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose. material3.FilledTonalIconButton
import androidx. compose.material3.Icon
import androidx. compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx. compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose. runtime.LaunchedEffect
import androidx. compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime. remember
import androidx. compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime. setValue
import androidx. compose.ui. Alignment
import androidx. compose.ui. Modifier
import androidx. compose.ui.draw.rotate
import androidx.compose.ui.graphics. graphicsLayer
import androidx.compose.ui. platform.LocalClipboard
import androidx.compose.ui.platform. LocalContext
import androidx.compose.ui. res.painterResource
import androidx.compose.ui. text.TextStyle
import androidx. compose.ui.text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.text.input. KeyboardType
import androidx.compose.ui.text.input. PasswordVisualTransformation
import androidx.compose.ui. text.input. VisualTransformation
import androidx.compose. ui.text.style.TextOverflow
import androidx. compose.ui.unit.dp
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.pass. hype.R
import com.pass. hype.data. room.model. Passwords
import com. pass.hype. utils.appList
import com.pass. hype.utils. capitalizeWords
import com.pass.hype.utils.getPasswordStrength

@SuppressLint("DiscouragedApi")
@Composable
fun PasswordItem(
    modifier: Modifier = Modifier,
    item:  Passwords,
    onEdit: (Passwords) -> Unit,
    onDelete: (Int) -> Unit,
    isEnd: Boolean = false
) {
    val context = LocalContext. current
    val clipboardManager = LocalClipboard.current. nativeClipboard

    val drawableId = remember(item. appIcon) {
        context.resources.getIdentifier(
            item.appIcon,
            "drawable",
            context.packageName
        )
    }

    // UI states
    var expandedState by rememberSaveable { mutableStateOf(false) }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    // Animations
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f,
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300))
    }

    Card (
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            . fillMaxWidth()
            .graphicsLayer { alpha = alphaAnimation.value }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = LinearOutSlowInEasing
                )
            ),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = { expandedState = ! expandedState }
    ) {
        Column(
            modifier = Modifier
                . fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            PasswordItemHeader(
                item = item,
                drawableId = drawableId,
                expandedState = expandedState,
                rotationState = rotationState,
                onShareClick = {
                    val shareText = buildString {
                        appendLine("App: ${item.appName}")
                        appendLine("Email: ${item.email}")
                        appendLine("Password: ${item.password}")
                        if (item.notes.isNotEmpty()) {
                            appendLine("Notes: ${item.notes}")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent. EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Password"))
                },
                onEditClick = {
                    // Call onEdit with the current item to navigate to edit screen
                    onEdit(item)
                }
            )

            // Expanded Content
            AnimatedVisibility(
                visible = expandedState,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier. padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Email Field
                    PasswordDetailField(
                        value = item.email,
                        label = "Email/UserID",
                        leadingIcon = R.drawable.email,
                        onCopy = {
                            clipboardManager.setPrimaryClip(
                                ClipData. newPlainText("Email", item.email)
                            )
                        }
                    )

                    // Password Field
                    PasswordDetailField(
                        value = item. password,
                        label = "Password",
                        leadingIcon = R.drawable.password,
                        isPassword = true,
                        passwordVisible = passwordVisibility,
                        onToggleVisibility = { passwordVisibility = ! passwordVisibility },
                        onCopy = {
                            clipboardManager.setPrimaryClip(
                                ClipData.newPlainText("Password", item. password)
                            )
                        }
                    )

                    // Notes Field (if not empty)
                    if (item.notes.isNotEmpty()) {
                        PasswordDetailField(
                            value = item.notes,
                            label = "Additional Notes",
                            leadingIcon = R.drawable.edit,
                            singleLine = false,
                            maxLines = 3
                        )
                    }

                    // Action Row
                    PasswordItemActions(
                        passwordStrength = item.password.getPasswordStrength(),
                        onCopyPassword = {
                            clipboardManager. setPrimaryClip(
                                ClipData.newPlainText("Password", item.password)
                            )
                        },
                        onDelete = { onDelete(item. passwordId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordItemHeader(
    item:  Passwords,
    drawableId: Int,
    expandedState: Boolean,
    rotationState:  Float,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier. fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        AppIconBadge(
            appName = item.appName,
            drawableId = drawableId,
            isKnownApp = appList.contains(item.appName)
        )

        Spacer(Modifier.width(12.dp))

        // Title and Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.appName,
                style = MaterialTheme. typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (expandedState) {
                    val timeText = TimeAgo. using(item.editTime. toLong()).capitalizeWords()
                    if (item.edited) "Edited $timeText" else "Created $timeText"
                } else {
                    item.email
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme. colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(
                onClick = onShareClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = "Share",
                    modifier = Modifier.size(18.dp)
                )
            }

            FilledTonalIconButton(
                onClick = onEditClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable. edit),
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expand/Collapse indicator
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expandedState) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationState)
                    .padding(start = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppIconBadge(
    appName:  String,
    drawableId: Int,
    isKnownApp: Boolean
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme. secondaryContainer
    ) {
        Box(
            modifier = Modifier. fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isKnownApp && drawableId != 0) {
                Icon(
                    painter = painterResource(id = drawableId),
                    contentDescription = "$appName Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp),
                    tint = MaterialTheme. colorScheme.onSecondaryContainer
                )
            } else {
                Text(
                    text = appName. firstOrNull()?.uppercase() ?: "? ",
                    style = MaterialTheme. typography.titleLarge,
                    fontWeight = FontWeight. Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun PasswordDetailField(
    value: String,
    label:  String,
    leadingIcon: Int,
    isPassword:  Boolean = false,
    passwordVisible: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    onToggleVisibility: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        textStyle = if (isPassword) {
            TextStyle(fontFamily = FontFamily(Font(R.font.password)))
        } else {
            TextStyle. Default
        },
        visualTransformation = if (isPassword && ! passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions. Default
        },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Row {
                if (isPassword && onToggleVisibility != null) {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisible) R.drawable.eye_close else R.drawable.eye_open
                            ),
                            contentDescription = if (passwordVisible) "Hide" else "Show",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onCopy != null) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            painter = painterResource(R.drawable.copy),
                            contentDescription = "Copy",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme. colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme. outlineVariant
        )
    )
}

@Composable
private fun PasswordItemActions(
    passwordStrength: String,
    onCopyPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Password Strength Badge
        PasswordStrengthBadge(strength = passwordStrength)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Delete Button
            Surface(
                onClick = onDelete,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme. colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement. spacedBy(6.dp),
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme. colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography. labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Copy Button
            Surface(
                onClick = onCopyPassword,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme. colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement. spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Copy",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthBadge(strength: String) {
    val (backgroundColor, textColor) = when (strength. lowercase()) {
        "very strong" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        "strong" -> MaterialTheme.colorScheme. primaryContainer to MaterialTheme.colorScheme. onPrimaryContainer
        "good" -> MaterialTheme.colorScheme. tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "fair" -> MaterialTheme.colorScheme. secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "weak" -> MaterialTheme.colorScheme. errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme. colorScheme.errorContainer to MaterialTheme.colorScheme. onErrorContainer
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = strength. uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme. typography.labelMedium,
            fontFamily = FontFamily(Font(R.font. password)),
            color = textColor
        )
    }
}
package com.pass.hype.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx. compose.animation.fadeOut
import androidx. compose.animation.scaleIn
import androidx.compose.animation. scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose. foundation.layout.Box
import androidx.compose.foundation.layout. Row
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation. layout.padding
import androidx.compose.foundation.layout. size
import androidx. compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text. BasicTextField
import androidx. compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text. KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.IconButtonDefaults
import androidx. compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose. runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx. compose.ui.focus.focusRequester
import androidx.compose.ui.graphics. SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx. compose.ui.res.painterResource
import androidx.compose. ui.text.TextStyle
import androidx. compose.ui.text.font.Font
import androidx.compose. ui.text.font.FontFamily
import androidx.compose.ui.text.input. ImeAction
import androidx.compose.ui. text.input.KeyboardCapitalization
import androidx.compose. ui.text.input.KeyboardType
import androidx.compose. ui.text.input.TextFieldValue
import androidx.compose. ui.unit.dp
import androidx.compose.ui.unit. sp
import com.pass.hype.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search",
    searchQuery: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isEnabled:  Boolean = true,
    isListEmpty: Boolean = false,
    onSearchClicked: () -> Unit = {}
) {
    val focusManager = LocalFocusManager. current
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val isDisabled = ! isEnabled || isListEmpty

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDisabled) 0.6f else 1f),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme. surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme. colorScheme.surfaceContainerHighest,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme. colorScheme.onSurfaceVariant
                )
            }

            // Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Placeholder
                if (searchQuery.text.isEmpty()) {
                    Text(
                        text = hint,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font. password))
                        )
                    )
                }

                // Input Field
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = ! isDisabled,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.password))
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchClicked()
                        }
                    ),
                    singleLine = true,
                    interactionSource = interactionSource
                )
            }

            // Clear Button
            AnimatedVisibility(
                visible = searchQuery.text.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = {
                        onValueChange(TextFieldValue(""))
                        focusManager. clearFocus()
                    },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme. colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme. colorScheme.onSurfaceVariant
                    )
                }
            }

            // Placeholder for alignment when clear button is hidden
            if (searchQuery. text.isEmpty()) {
                Box(modifier = Modifier.size(48.dp))
            }
        }
    }
}
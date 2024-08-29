package com.pass.hype.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    hint: String,
    modifier: Modifier = Modifier,
    isEnabled: (Boolean) = true,
    height: Dp = 108.dp,
    cornerShape: RoundedCornerShape = RoundedCornerShape(50),
    onSearchClicked: () -> Unit = {},
    searchQuery: MutableState<TextFieldValue>,
    onTextChange: (String) -> Unit = {},
    isListEmpty: Boolean
) {
    Row(
        modifier = modifier
            .height(height)
            .padding(horizontal = 10.dp)
            .padding(top = 40.dp, bottom = 10.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = cornerShape
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(modifier = Modifier.size(40.dp).padding(start = 16.dp), onClick = {}) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Close Button")
        }
        BasicTextField(
            modifier = modifier
                .weight(5f)
                .fillMaxWidth()
                .padding(start = 12.dp),
            value = searchQuery.value,
            onValueChange = { value ->
                searchQuery.value = value },
            cursorBrush = Brush.sweepGradient(listOf(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.onSurface)),
            enabled = isEnabled,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
            ),
            decorationBox = { innerTextField ->
                if (searchQuery.value.text.isEmpty()) {
                    Text(
                        text = hint,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                    )
                }
                innerTextField()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
            singleLine = true,
            readOnly = isListEmpty,
        )
        Box(
            modifier = modifier
                .weight(1f)
                .size(56.dp)
                .padding(end = 12.dp)
                .background(color = Color.Transparent, shape = CircleShape)
                .clickable {
                    if (searchQuery.value.text.isNotEmpty()) {
                        searchQuery.value = TextFieldValue(text = "")
                        onTextChange("")
                    }
                },
        ) {
            if (searchQuery.value.text.isNotEmpty()) {
                IconButton(modifier = Modifier.size(36.dp).align(Alignment.CenterEnd), onClick = { searchQuery.value = TextFieldValue(text = "") }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Button")
                }
            }
        }
    }
}
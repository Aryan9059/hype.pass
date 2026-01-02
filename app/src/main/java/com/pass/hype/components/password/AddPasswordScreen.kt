package com.pass.hype.components.password

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pass.hype.R
import com.pass.hype.utils.appList // Ensure this utility is accessible

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    passStrength: String,
    onNavigateBack: () -> Unit,
    onSaveConfirmed: () -> Unit,
    onCreateClick: () -> Unit,
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

    val scrollState = rememberScrollState()

    // --- Validation Logic ---
    emailError = when {
        email.isBlank() -> "Please enter your Email/UserID."
        else -> null
    }
    passwordError = when {
        password.isBlank() -> "Please enter your Password."
        else -> null
    }

    // --- Password Strength Animation ---
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

    // --- SCREEN STRUCTURE (Scaffold replaces AlertDialog) ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Create New Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.x), // Assuming you have an arrow_back icon
                            contentDescription = "Go Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveConfirmed,
                        enabled = passwordError == null && emailError == null
                    ) {
                        Text(text = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // --- 1. PASSWORD STRENGTH VISUAL ---
            Spacer(Modifier.size(16.dp))
            val length = password.length

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.password),
                            contentDescription = "Password Strength Icon",
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                            text = passStrength,
                            fontFamily = FontFamily(Font(R.font.password)),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$length characters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
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

            // --- 2. INPUT FIELDS ---

            // App Selection Dropdown
            AppSelectionDropdown(app, onAppChanged)

            Spacer(modifier = Modifier.size(12.dp))

            // Email/UserID Field
            OutlinedTextField(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                label = { Text(text = "Email/UserID") },
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.email), contentDescription = "Email", Modifier.size(24.dp))
                },
                isError = emailError != null,
                supportingText = if (emailError != null) { { Text(emailError!!) } } else null
            )

            Spacer(modifier = Modifier.size(12.dp))

            // Password Field
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
                isError = passwordError != null,
                supportingText = if (passwordError != null) { { Text(passwordError!!) } } else null,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.password), contentDescription = "Password Text Box Icon", Modifier.size(24.dp))
                }
            )

            Spacer(modifier = Modifier.size(12.dp))

            // Notes Field
            OutlinedTextField(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text(text = "Additional Notes") },
                singleLine = false,
                maxLines = 3,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- EXTRACTED DROPDOWN FOR CLEANLINESS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSelectionDropdown(app: String, onAppChanged: (String) -> Unit) {
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
        modifier = Modifier.fillMaxWidth()
    ) {
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
                        painter = painterResource(R.drawable.app), // Default icon
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
            modifier = Modifier.heightIn(max = 208.dp) // Use heightIn for better handling
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
}
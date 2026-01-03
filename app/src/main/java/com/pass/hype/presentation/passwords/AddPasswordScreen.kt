package com.pass.hype.presentation.passwords

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose. foundation.border
import androidx.compose.foundation.layout.*
import androidx. compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx. compose.foundation.verticalScroll
import androidx.compose.material. icons.Icons
import androidx.compose.material. icons.automirrored.filled.ArrowBack
import androidx.compose. material. icons.filled.*
import androidx.compose.material. icons.outlined.*
import androidx.compose. material3.*
import androidx.compose.runtime.*
import androidx. compose.runtime.saveable.rememberSaveable
import androidx.compose. ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui. geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.graphics.graphicsLayer
import androidx.compose.ui. graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx. compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui. text. AnnotatedString
import androidx.compose.ui. text.TextStyle
import androidx. compose.ui.text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose.ui.text. font.FontWeight
import androidx.compose. ui.text.input.KeyboardCapitalization
import androidx.compose.ui. text.input.KeyboardType
import androidx.compose. ui.text.input. PasswordVisualTransformation
import androidx.compose. ui.text.input. VisualTransformation
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import com.pass.hype.R
import com.pass.hype.data.room.model. Passwords
import com.pass.hype.utils. PasswordAnalysis
import com.pass.hype.utils.PasswordConfig
import com.pass.hype.utils.PasswordStrength
import com.pass.hype.utils. PasswordUtils
import com.pass.hype.utils.appList
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun AddPasswordScreen(
    existingPassword:  Passwords? = null,
    onSave: (Passwords) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext. current
    val clipboardManager = LocalClipboardManager.current
    val isEditing = existingPassword != null

    // Form state
    var app by rememberSaveable { mutableStateOf(existingPassword?. appName ?: "") }
    var email by rememberSaveable { mutableStateOf(existingPassword?.email ?: "") }
    var password by rememberSaveable { mutableStateOf(existingPassword?.password ?: "") }
    var notes by rememberSaveable { mutableStateOf(existingPassword?.notes ?: "") }

    // UI state
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showGeneratorSettings by rememberSaveable { mutableStateOf(false) }
    var appDropdownExpanded by remember { mutableStateOf(false) }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Password generator settings
    var passwordLength by rememberSaveable { mutableFloatStateOf(16f) }
    var includeUppercase by rememberSaveable { mutableStateOf(true) }
    var includeLowercase by rememberSaveable { mutableStateOf(true) }
    var includeNumbers by rememberSaveable { mutableStateOf(true) }
    var includeSymbols by rememberSaveable { mutableStateOf(true) }
    var excludeAmbiguous by rememberSaveable { mutableStateOf(false) }
    var usePassphrase by rememberSaveable { mutableStateOf(false) }

    // Password analysis
    var passwordAnalysis by remember { mutableStateOf<PasswordAnalysis? >(null) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode. Reverse
        ),
        label = "gradient"
    )

    LaunchedEffect(password) {
        passwordAnalysis = if (password.isNotEmpty()) {
            PasswordUtils.analyzePassword(password)
        } else null
    }

    LaunchedEffect(showCopiedFeedback) {
        if (showCopiedFeedback) {
            delay(2000)
            showCopiedFeedback = false
        }
    }

    fun generateNewPassword() {
        password = if (usePassphrase) {
            PasswordUtils.generatePassphrase(
                wordCount = (passwordLength / 4).toInt().coerceIn(3, 6),
                separator = "-",
                capitalize = true,
                includeNumber = true
            )
        } else {
            PasswordUtils.generatePassword(
                PasswordConfig(
                    length = passwordLength. toInt(),
                    includeUppercase = includeUppercase,
                    includeLowercase = includeLowercase,
                    includeNumbers = includeNumbers,
                    includeSymbols = includeSymbols,
                    excludeAmbiguous = excludeAmbiguous
                )
            )
        }
    }

    // Validation
    val isValid = app.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    // Strength colors
    val strengthColors = remember(passwordAnalysis?. strength) {
        when (passwordAnalysis?. strength) {
            PasswordStrength. VERY_STRONG -> listOf(Color(0xFF00C853), Color(0xFF69F0AE))
            PasswordStrength.STRONG -> listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
            PasswordStrength. GOOD -> listOf(Color(0xFFFFA726), Color(0xFFFFCC02))
            PasswordStrength.FAIR -> listOf(Color(0xFFFF7043), Color(0xFFFF9800))
            PasswordStrength. WEAK -> listOf(Color(0xFFE53935), Color(0xFFFF5252))
            PasswordStrength. VERY_WEAK, null -> listOf(Color. Gray, Color.LightGray)
        }
    }

    val strengthColor by animateColorAsState(
        targetValue = strengthColors[0],
        animationSpec = tween(500),
        label = "strengthColor"
    )

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Password" else "New Password",
                        style = if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            MaterialTheme.typography.headlineLarge
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        fontFamily = FontFamily(Font(R.font.password))
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton (
                        onClick = { showGeneratorSettings = true },
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Settings",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults. topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // App Header Card
                AppSelectionCard(
                    app = app,
                    onAppChange = { app = it },
                    expanded = appDropdownExpanded,
                    onExpandedChange = { appDropdownExpanded = it },
                    context = context
                )

                Spacer(Modifier.height(20.dp))

                // Credentials Section
                CredentialsSection(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible },
                    strengthColor = strengthColor,
                    passwordAnalysis = passwordAnalysis
                )

                Spacer(Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernActionButton(
                        text = "Generate",
                        icon = R.drawable.create,
                        onClick = { generateNewPassword() },
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    )
                    ModernActionButton(
                        text = if (showCopiedFeedback) "Copied!" else "Copy",
                        icon = if (showCopiedFeedback) R.drawable.done else R.drawable.copy,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(password))
                            showCopiedFeedback = true
                        },
                        modifier = Modifier. weight(1f),
                        isPrimary = false
                    )
                }

                // Password Strength Card
                AnimatedVisibility(
                    visible = password.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PasswordStrengthCard(
                        analysis = passwordAnalysis,
                        strengthColors = strengthColors,
                        gradientOffset = gradientOffset
                    )
                }

                Spacer(Modifier. height(12.dp))

                // Notes Section
                NotesSection(
                    notes = notes,
                    onNotesChange = { notes = it }
                )

                // Suggestions
                AnimatedVisibility(
                    visible = passwordAnalysis?.suggestions?. isNotEmpty() == true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    passwordAnalysis?.let { analysis ->
                        SuggestionsCard(suggestions = analysis.suggestions)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Save Button
                SaveButton(
                    isValid = isValid,
                    isEditing = isEditing,
                    onClick = {
                        val passwordEntry = Passwords(
                            passwordId = existingPassword?.passwordId ?: 0,
                            appName = app,
                            appIcon = app.lowercase(),
                            email = email,
                            password = password,
                            editTime = System.currentTimeMillis().toString(),
                            edited = isEditing,
                            notes = notes
                        )
                        onSave(passwordEntry)
                    }
                )

                Spacer(Modifier.height(32.dp))
            }

            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

    // Generator Settings Sheet
    if (showGeneratorSettings) {
        ModernGeneratorSheet(
            passwordLength = passwordLength,
            onLengthChange = { passwordLength = it },
            includeUppercase = includeUppercase,
            onUppercaseChange = { includeUppercase = it },
            includeLowercase = includeLowercase,
            onLowercaseChange = { includeLowercase = it },
            includeNumbers = includeNumbers,
            onNumbersChange = { includeNumbers = it },
            includeSymbols = includeSymbols,
            onSymbolsChange = { includeSymbols = it },
            excludeAmbiguous = excludeAmbiguous,
            onAmbiguousChange = { excludeAmbiguous = it },
            usePassphrase = usePassphrase,
            onPassphraseChange = { usePassphrase = it },
            onDismiss = { showGeneratorSettings = false },
            onGenerate = {
                generateNewPassword()
                showGeneratorSettings = false
            }
        )
    }
}

// ==================== APP SELECTION CARD ====================

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DiscouragedApi")
@Composable
private fun AppSelectionCard(
    app:  String,
    onAppChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    context: android.content.Context
) {
    val drawableId = remember(app. lowercase()) {
        if (app.isEmpty()) 0 else
            context.resources.getIdentifier(app.lowercase(), "drawable", context.packageName)
    }

    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated App Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = 1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(0.1f),
                        shape = CircleShape
                        ),
                contentAlignment = Alignment.Center
            ) {
                if (drawableId != 0) {
                    Icon(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = app. take(1).uppercase().ifEmpty { "?" },
                        style = MaterialTheme. typography.headlineLarge,
                        color = MaterialTheme. colorScheme.primary,
                        fontFamily = FontFamily(Font(R.font.password)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // App Name Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = app,
                    onValueChange = onAppChange,
                    placeholder = {
                        Text(
                            "Enter app name",
                            color = MaterialTheme. colorScheme.onSurfaceVariant. copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        textAlign = TextAlign. Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme. outlineVariant. copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme. primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    trailingIcon = {
                        Icon(
                            Icons. Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f)
                        )
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.background(MaterialTheme. colorScheme.surfaceContainerLow)
                ) {
                    appList.filter {
                        it. lowercase().contains(app.lowercase()) || app.isEmpty()
                    }.take(6).forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                onAppChange(option)
                                onExpandedChange(false)
                            },
                            leadingIcon = {
                                val optionDrawableId = context.resources.getIdentifier(
                                    option. lowercase(), "drawable", context. packageName
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer. copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment. Center
                                ) {
                                    if (optionDrawableId != 0) {
                                        Icon(
                                            painterResource(id = optionDrawableId),
                                            null,
                                            Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme. primary
                                        )
                                    } else {
                                        Text(
                                            option.take(1),
                                            style = MaterialTheme. typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== CREDENTIALS SECTION ====================

@Composable
private fun CredentialsSection(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    strengthColor: Color,
    passwordAnalysis:  PasswordAnalysis?
) {
    Column(
        modifier = Modifier. fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email Field
        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email or Username",
            leadingIcon = R.drawable.email,
            keyboardType = KeyboardType. Email
        )

        // Password Field with strength indicator
        Box {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                textStyle = TextStyle(
                    fontFamily = FontFamily(Font(R.font. password)),
                    fontSize = 16.sp,
                    letterSpacing = if (! passwordVisible) 2.sp else 0.sp
                ),
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.password),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (password.isNotEmpty()) strengthColor else MaterialTheme.colorScheme. onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            if (passwordVisible) Icons.Default. VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            modifier = Modifier. size(20.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (password.isNotEmpty()) strengthColor else MaterialTheme.colorScheme. primary,
                    unfocusedBorderColor = if (password.isNotEmpty())
                        strengthColor.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme. colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme. surface
                )
            )
        }
    }
}

@Composable
private fun ModernTextField(
    value:  String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier:  Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier. fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme. outlineVariant. copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme. colorScheme.surface,
            unfocusedContainerColor = MaterialTheme. colorScheme.surface
        )
    )
}

// ==================== ACTION BUTTONS ====================

@Composable
private fun ModernActionButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val buttonColor = if (isPrimary) {
        MaterialTheme. colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isPrimary) {
        MaterialTheme. colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = modifier. height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = buttonColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment. CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight. SemiBold,
                color = contentColor
            )
        }
    }
}

// ==================== PASSWORD STRENGTH CARD ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasswordStrengthCard(
    analysis: PasswordAnalysis?,
    strengthColors:  List<Color>,
    gradientOffset:  Float
) {
    val progress by animateFloatAsState(
        targetValue = (analysis?.score ?:  0) / 100f,
        animationSpec = tween(600),
        label = "progress"
    )

    Card(
        modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(strengthColors),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (analysis?.strength) {
                                PasswordStrength. VERY_STRONG, PasswordStrength. STRONG -> Icons.Default.Shield
                                PasswordStrength.GOOD -> Icons.Default. Security
                                else -> Icons.Default. Warning
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = analysis?.strength?. label ?: "No Password",
                            style = MaterialTheme. typography.titleMedium,
                            fontWeight = FontWeight. Bold,
                            color = strengthColors[0]
                        )
                        Text(
                            text = "Crack time: ${analysis?.crackTime ?: "-"}",
                            style = MaterialTheme. typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Score Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = strengthColors[0]. copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${analysis?.score ?: 0}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight. Bold,
                        color = strengthColors[0]
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        . fillMaxWidth(progress)
                        . background(
                            brush = Brush. horizontalGradient(
                                colors = strengthColors,
                                startX = 0f,
                                endX = 500f * (1 + gradientOffset * 0.3f)
                            )
                        )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Requirements Grid
            FlowRow(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementPill("8+", analysis?.length ?: 0 >= 8)
                RequirementPill("ABC", analysis?.hasUppercase == true)
                RequirementPill("abc", analysis?.hasLowercase == true)
                RequirementPill("123", analysis?.hasDigits == true)
                RequirementPill("@#$", analysis?.hasSymbols == true)
            }
        }
    }
}

@Composable
private fun RequirementPill(text: String, met: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = if (met) Color(0xFF2E7D32).copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "pillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (met) Color(0xFF2E7D32)
        else MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.5f),
        label = "pillContent"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (met) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier. size(14.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography. labelMedium,
                fontWeight = FontWeight. Medium,
                color = contentColor
            )
        }
    }
}

// ==================== NOTES SECTION ====================

@Composable
private fun NotesSection(
    notes:  String,
    onNotesChange: (String) -> Unit
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Notes (Optional)") },
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 1,
        maxLines = 5,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.edit),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme. colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme. colorScheme.surface,
            unfocusedContainerColor = MaterialTheme. colorScheme.surface
        )
    )
}

// ==================== SUGGESTIONS CARD ====================

@Composable
private fun SuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier. padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme. colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tips",
                    style = MaterialTheme. typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.height(8.dp))
            suggestions.take(2).forEach { suggestion ->
                Text(
                    "• $suggestion",
                    style = MaterialTheme.typography. bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// ==================== SAVE BUTTON ====================

@Composable
private fun SaveButton(
    isValid:  Boolean,
    isEditing: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isValid) 1f else 0.98f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        enabled = isValid,
        colors = ButtonDefaults. buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme. colorScheme.surfaceContainerHighest
        ),
        elevation = ButtonDefaults. buttonElevation(
            defaultElevation = if (isValid) 4.dp else 0.dp
        )
    ) {
        Text(
            text = if (isEditing) "Update Password" else "Save Password",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ==================== GENERATOR SHEET ====================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ModernGeneratorSheet(
    passwordLength: Float,
    onLengthChange: (Float) -> Unit,
    includeUppercase: Boolean,
    onUppercaseChange:  (Boolean) -> Unit,
    includeLowercase: Boolean,
    onLowercaseChange: (Boolean) -> Unit,
    includeNumbers: Boolean,
    onNumbersChange:  (Boolean) -> Unit,
    includeSymbols: Boolean,
    onSymbolsChange: (Boolean) -> Unit,
    excludeAmbiguous: Boolean,
    onAmbiguousChange: (Boolean) -> Unit,
    usePassphrase: Boolean,
    onPassphraseChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                . fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment. Center
                ) {
                    Icon(
                        Icons. Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme. onPrimaryContainer
                    )
                }
                Spacer(Modifier. width(16.dp))
                Column {
                    Text(
                        "Password Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Customize your password settings",
                        style = MaterialTheme. typography.bodySmall,
                        color = MaterialTheme.colorScheme. onSurfaceVariant
                    )
                }
            }

            // Type Selection
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. spacedBy(12.dp)
            ) {
                GeneratorTypeCard(
                    title = "Random",
                    subtitle = "Mixed characters",
                    icon = Icons.Default. Shuffle,
                    selected = ! usePassphrase,
                    onClick = { onPassphraseChange(false) },
                    modifier = Modifier.weight(1f)
                )
                GeneratorTypeCard(
                    title = "Passphrase",
                    subtitle = "Memorable words",
                    icon = Icons.Default. TextFields,
                    selected = usePassphrase,
                    onClick = { onPassphraseChange(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Length Slider
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement. SpaceBetween
                    ) {
                        Text(
                            if (usePassphrase) "Word Count" else "Length",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (usePassphrase) "${(passwordLength / 4).toInt().coerceIn(3, 6)} words"
                            else "${passwordLength.toInt()} characters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight. Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = passwordLength,
                        onValueChange = onLengthChange,
                        valueRange = if (usePassphrase) 12f.. 24f else 8f..32f,
                        steps = if (usePassphrase) 2 else 23,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme. primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Character Options
            AnimatedVisibility(visible = ! usePassphrase) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Include Characters",
                            style = MaterialTheme. typography.titleSmall
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement. spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CharacterToggle("A-Z", includeUppercase) { onUppercaseChange(! includeUppercase) }
                            CharacterToggle("a-z", includeLowercase) { onLowercaseChange(! includeLowercase) }
                            CharacterToggle("0-9", includeNumbers) { onNumbersChange(!includeNumbers) }
                            CharacterToggle("! @#", includeSymbols) { onSymbolsChange(!includeSymbols) }
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Generate Password",
                    fontWeight = FontWeight. SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun GeneratorTypeCard(
    title:  String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme. colorScheme.surfaceContainerLow,
        label = "cardBg"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography. labelSmall,
                color = if (selected) MaterialTheme.colorScheme. onPrimaryContainer. copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CharacterToggle(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = enabled,
        onClick = onClick,
        label = {
            Text(
                text,
                fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = if (enabled) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
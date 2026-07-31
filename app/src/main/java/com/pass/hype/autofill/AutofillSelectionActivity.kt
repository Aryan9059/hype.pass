package com.pass.hype.autofill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pass.hype.R
import com.pass.hype.autofill.builder.ResponseBuilder
import com.pass.hype.autofill.repository.AutofillRepository
import com.pass.hype.data.room.model.Passwords
import com.pass.hype.ui.theme.HypepassTheme
import com.pass.hype.ui.theme.berlinFontFamily

class AutofillSelectionActivity : ComponentActivity() {

    private lateinit var repository: AutofillRepository

    private var packageNameExtra: String? = null
    private var webDomainExtra: String? = null
    private var usernameId: AutofillId? = null
    private var emailId: AutofillId? = null
    private var passwordId: AutofillId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AutofillRepository(applicationContext)

        packageNameExtra = intent.getStringExtra(ResponseBuilder.EXTRA_PACKAGE_NAME)
        webDomainExtra = intent.getStringExtra(ResponseBuilder.EXTRA_WEB_DOMAIN)
        usernameId = intent.getParcelableExtra(ResponseBuilder.EXTRA_USERNAME_ID)
        emailId = intent.getParcelableExtra(ResponseBuilder.EXTRA_EMAIL_ID)
        passwordId = intent.getParcelableExtra(ResponseBuilder.EXTRA_PASSWORD_ID)

        setContent {
            HypepassTheme {
                AutofillBottomSheetScreen(
                    repository = repository,
                    identifier = webDomainExtra ?: packageNameExtra ?: "",
                    onPasswordSelected = { returnSelectedPassword(it) },
                    onDismiss = { cancelSelection() }
                )
            }
        }
    }

    private fun returnSelectedPassword(password: Passwords) {
        val replyIntent = Intent()

        val presentation = RemoteViews(packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_title, password.appName)
            setTextViewText(R.id.autofill_subtitle, password.email)
            setImageViewResource(R.id.autofill_icon, R.drawable.password)
        }

        val datasetBuilder = Dataset.Builder(presentation)

        usernameId?.let { id ->
            datasetBuilder.setValue(id, AutofillValue.forText(password.email))
        }
        emailId?.let { id ->
            datasetBuilder.setValue(id, AutofillValue.forText(password.email))
        }
        passwordId?.let { id ->
            datasetBuilder.setValue(id, AutofillValue.forText(password.password))
        }

        replyIntent.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, datasetBuilder.build())
        setResult(Activity.RESULT_OK, replyIntent)
        finish()
    }

    private fun cancelSelection() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutofillBottomSheetScreen(
    repository: AutofillRepository,
    identifier: String,
    onPasswordSelected: (Passwords) -> Unit,
    onDismiss: () -> Unit
) {
    var allPasswords by remember { mutableStateOf<List<Passwords>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        allPasswords = repository.getAllPasswords()
        isLoading = false
    }

    val identifierLower = identifier.lowercase()

    fun matchScore(password: Passwords): Int {
        val appNameLower = password.appName.lowercase()
        return when {
            appNameLower == identifierLower -> 3
            appNameLower.contains(identifierLower) || identifierLower.contains(appNameLower) -> 2
            else -> 0
        }
    }

    val suggestedPasswords by remember(allPasswords, searchQuery.text) {
        derivedStateOf {
            val query = searchQuery.text.lowercase().trim()
            if (query.isEmpty()) {
                allPasswords.filter { matchScore(it) > 0 }.sortedByDescending { matchScore(it) }
            } else emptyList()
        }
    }

    val otherPasswords by remember(allPasswords, searchQuery.text) {
        derivedStateOf {
            val query = searchQuery.text.lowercase().trim()
            if (query.isEmpty()) {
                allPasswords.filter { matchScore(it) == 0 }
            } else {
                allPasswords.filter { password ->
                    password.appName.lowercase().contains(query) ||
                            password.email.lowercase().contains(query)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 0.dp
    ) {
        BottomSheetContent(
            identifier = identifier,
            suggestedPasswords = suggestedPasswords,
            otherPasswords = otherPasswords,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isLoading = isLoading,
            onPasswordSelected = onPasswordSelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun BottomSheetContent(
    identifier: String,
    suggestedPasswords: List<Passwords>,
    otherPasswords: List<Passwords>,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    isLoading: Boolean,
    onPasswordSelected: (Passwords) -> Unit,
    onDismiss: () -> Unit
) {
    val totalCount = suggestedPasswords.size + otherPasswords.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Choose Password",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = berlinFontFamily,
                    fontWeight = FontWeight.Bold
                )
                if (identifier.isNotEmpty()) {
                    Text(
                        text = "for $identifier",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Close",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Search passwords...",
                    fontFamily = FontFamily(Font(R.font.password))
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.text.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { onSearchQueryChange(TextFieldValue("")) }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            totalCount == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.password),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.text.isEmpty()) "No passwords saved yet"
                                   else "No matching passwords",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.text.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Suggested section
                    if (suggestedPasswords.isNotEmpty()) {
                        item {
                            SectionLabel(text = "Suggested")
                        }
                        items(
                            items = suggestedPasswords,
                            key = { "s_${it.passwordId}" }
                        ) { password ->
                            PasswordSelectionItem(
                                password = password,
                                onClick = { onPasswordSelected(password) }
                            )
                        }
                    }

                    // Divider between sections
                    if (suggestedPasswords.isNotEmpty() && otherPasswords.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            SectionLabel(text = "All Passwords")
                        }
                    }

                    // Other / search results
                    items(
                        items = otherPasswords,
                        key = { "o_${it.passwordId}" }
                    ) { password ->
                        PasswordSelectionItem(
                            password = password,
                            onClick = { onPasswordSelected(password) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun PasswordSelectionItem(
    password: Passwords,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initial letter
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = password.appName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = password.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = password.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.next),
                    contentDescription = "Select",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

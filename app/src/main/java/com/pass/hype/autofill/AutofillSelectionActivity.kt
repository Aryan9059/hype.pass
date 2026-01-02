package com.pass.hype.autofill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill. Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill. AutofillValue
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx. activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx. compose.animation.fadeOut
import androidx. compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout. Arrangement
import androidx.compose.foundation. layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose. foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose. foundation.layout. Spacer
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation. layout.height
import androidx.compose.foundation.layout. navigationBarsPadding
import androidx.compose. foundation.layout.padding
import androidx.compose.foundation.layout. size
import androidx.compose.foundation.layout. width
import androidx. compose.foundation.lazy.LazyColumn
import androidx.compose. foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose. material3.ModalBottomSheet
import androidx.compose. material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx. compose.material3.Surface
import androidx. compose.material3.Text
import androidx. compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime. Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime. remember
import androidx. compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose. ui.Alignment
import androidx.compose. ui.Modifier
import androidx.compose. ui.draw.clip
import androidx.compose.ui.graphics. Color
import androidx. compose.ui.res.painterResource
import androidx.compose.ui. text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose.ui.text. font.FontWeight
import androidx.compose. ui.text.input.TextFieldValue
import androidx.compose.ui. text.style.TextOverflow
import androidx. compose.ui.unit.dp
import com.pass.hype.R
import com.pass.hype.autofill. builder.ResponseBuilder
import com.pass.hype.autofill.repository.AutofillRepository
import com.pass.hype.data.room.model.Passwords
import com.pass.hype.ui.theme.HypepassTheme
import kotlinx.coroutines.launch

class AutofillSelectionActivity : ComponentActivity() {

    private lateinit var repository: AutofillRepository

    private var packageNameExtra: String? = null
    private var webDomainExtra:  String? = null
    private var usernameId: AutofillId? = null
    private var emailId: AutofillId? = null
    private var passwordId: AutofillId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = AutofillRepository(applicationContext)

        // Extract intent extras
        packageNameExtra = intent. getStringExtra(ResponseBuilder.EXTRA_PACKAGE_NAME)
        webDomainExtra = intent. getStringExtra(ResponseBuilder.EXTRA_WEB_DOMAIN)
        usernameId = intent. getParcelableExtra(ResponseBuilder.EXTRA_USERNAME_ID)
        emailId = intent.getParcelableExtra(ResponseBuilder. EXTRA_EMAIL_ID)
        passwordId = intent.getParcelableExtra(ResponseBuilder.EXTRA_PASSWORD_ID)

        setContent {
            HypepassTheme {
                AutofillBottomSheetScreen(
                    repository = repository,
                    identifier = webDomainExtra ?: packageNameExtra ?: "",
                    onPasswordSelected = { password -> returnSelectedPassword(password) },
                    onDismiss = { cancelSelection() }
                )
            }
        }
    }

    private fun returnSelectedPassword(password: Passwords) {
        val replyIntent = Intent()

        val presentation = RemoteViews(packageName, R.layout. autofill_item).apply {
            setTextViewText(R.id.autofill_title, password.appName)
            setTextViewText(R.id.autofill_subtitle, password.email)
            setImageViewResource(R. id.autofill_icon, R. drawable.password)
        }

        val datasetBuilder = Dataset.Builder(presentation)

        usernameId?.let { id ->
            datasetBuilder.setValue(id, AutofillValue. forText(password. email))
        }

        emailId?.let { id ->
            datasetBuilder.setValue(id, AutofillValue. forText(password. email))
        }

        passwordId?. let { id ->
            datasetBuilder. setValue(id, AutofillValue.forText(password.password))
        }

        val dataset = datasetBuilder.build()
        replyIntent. putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)

        setResult(Activity. RESULT_OK, replyIntent)
        finish()
    }

    private fun cancelSelection() {
        setResult(Activity. RESULT_CANCELED)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutofillBottomSheetScreen(
    repository: AutofillRepository,
    identifier: String,
    onPasswordSelected: (Passwords) -> Unit,
    onDismiss:  () -> Unit
) {
    var passwords by remember { mutableStateOf<List<Passwords>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        passwords = repository.getAllPasswords()
        isLoading = false
    }

    val filteredPasswords by remember(passwords, searchQuery. text) {
        derivedStateOf {
            val query = searchQuery. text.lowercase().trim()
            val identifierLower = identifier.lowercase()

            if (query.isEmpty()) {
                passwords. sortedByDescending { password ->
                    val appNameLower = password.appName.lowercase()
                    when {
                        appNameLower == identifierLower -> 3
                        appNameLower.contains(identifierLower) || identifierLower.contains(appNameLower) -> 2
                        else -> 0
                    }
                }
            } else {
                passwords.filter { password ->
                    password.appName.lowercase().contains(query) ||
                            password.email.lowercase().contains(query)
                }
            }
        }
    }

    // Transparent background that dismisses on tap
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme. colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 0.dp
    ) {
        BottomSheetContent(
            identifier = identifier,
            passwords = filteredPasswords,
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
    identifier:  String,
    passwords: List<Passwords>,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    isLoading: Boolean,
    onPasswordSelected: (Passwords) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier. fillMaxWidth(),
            horizontalArrangement = Arrangement. SpaceBetween,
            verticalAlignment = Alignment. CenterVertically
        ) {
            Column {
                Text(
                    text = "Choose Password",
                    style = MaterialTheme. typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.password))
                )
                if (identifier.isNotEmpty()) {
                    Text(
                        text = "for $identifier",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme. colorScheme.onSurfaceVariant
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
                    painter = painterResource(R.drawable. close),
                    contentDescription = "Close",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier. fillMaxWidth(),
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
                            painter = painterResource(R. drawable.close),
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme. colorScheme.primary,
                unfocusedBorderColor = MaterialTheme. colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password List
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        . height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Loading passwords...",
                        color = MaterialTheme. colorScheme.onSurfaceVariant
                    )
                }
            }
            passwords.isEmpty() -> {
                Box(
                    modifier = Modifier
                        . fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.password),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme. colorScheme.onSurfaceVariant. copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.text.isEmpty()) {
                                "No passwords saved yet"
                            } else {
                                "No matching passwords"
                            },
                            style = MaterialTheme. typography.bodyLarge,
                            color = MaterialTheme.colorScheme. onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier. height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = passwords,
                        key = { it.passwordId }
                    ) { password ->
                        PasswordSelectionItem(
                            password = password,
                            onClick = { onPasswordSelected(password) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordSelectionItem(
    password: Passwords,
    onClick:  () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme. colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Surface(
                modifier = Modifier. size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme. primaryContainer
            ) {
                Box(contentAlignment = Alignment. Center) {
                    Text(
                        text = password.appName. firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme. typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = password.appName,
                    style = MaterialTheme.typography. titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow. Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = password.email,
                    style = MaterialTheme. typography.bodyMedium,
                    color = MaterialTheme.colorScheme. onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme. primary
            ) {
                Icon(
                    painter = painterResource(R.drawable.next),
                    contentDescription = "Select",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
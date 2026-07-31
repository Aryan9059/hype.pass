package com.pass.hype.components.dialog

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pass.hype.R
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room.model.CardNetwork
import com.pass.hype.data.room.model.CardSubType
import com.pass.hype.data.room.model.CardType
import com.pass.hype.presentation.cards.CardScanActivity
import com.pass.hype.utils.CardUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCardDialog(
    isOpen: Boolean,
    existingCard: Card? = null,
    onDismiss: () -> Unit,
    onSave: (Card) -> Unit
) {
    val context = LocalContext.current

    // --- Scan result state (must be before early return) ---
    var scannedPan by remember { mutableStateOf("") }
    var scannedExpiryMonth by remember { mutableStateOf("") }
    var scannedExpiryYear by remember { mutableStateOf("") }
    var scannedHolderName by remember { mutableStateOf("") }

    // --- Photo state (before early return) ---
    var frontPhotoUri by remember { mutableStateOf<String?>(null) }
    var backPhotoUri by remember { mutableStateOf<String?>(null) }
    var pendingFrontFile by remember { mutableStateOf<File?>(null) }
    var pendingBackFile by remember { mutableStateOf<File?>(null) }
    var cameraSide by remember { mutableStateOf("front") }

    // Reset photo state when dialog opens for a different card
    LaunchedEffect(isOpen, existingCard?.cardId) {
        if (isOpen) {
            frontPhotoUri = existingCard?.frontImageUri
            backPhotoUri = existingCard?.backImageUri
            scannedPan = ""
            scannedExpiryMonth = ""
            scannedExpiryYear = ""
            scannedHolderName = ""
        }
    }

    // --- Activity result launchers (MUST be before early return) ---
    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                scannedPan         = data.getStringExtra(CardScanActivity.EXTRA_PAN) ?: ""
                scannedExpiryMonth = data.getStringExtra(CardScanActivity.EXTRA_EXPIRY_MONTH) ?: ""
                scannedExpiryYear  = data.getStringExtra(CardScanActivity.EXTRA_EXPIRY_YEAR) ?: ""
                scannedHolderName  = data.getStringExtra(CardScanActivity.EXTRA_CARDHOLDER_NAME) ?: ""
                // Auto-populate photos from the scanned document images
                data.getStringExtra(CardScanActivity.EXTRA_FRONT_IMAGE_PATH)?.let { frontPhotoUri = it }
                data.getStringExtra(CardScanActivity.EXTRA_BACK_IMAGE_PATH)?.let  { backPhotoUri  = it }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            if (cameraSide == "front") {
                frontPhotoUri = pendingFrontFile?.absolutePath
            } else {
                backPhotoUri = pendingBackFile?.absolutePath
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Persist the permission so we can read it later
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            if (cameraSide == "front") frontPhotoUri = it.toString()
            else backPhotoUri = it.toString()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(context, cameraSide, pendingFrontFile, pendingBackFile,
                setFrontFile = { pendingFrontFile = it },
                setBackFile = { pendingBackFile = it },
                launch = { uri -> cameraLauncher.launch(uri) }
            )
        }
    }

    // --- Early return when not open ---
    if (!isOpen) return

    // ---- Form state ----
    val isEditing = existingCard != null

    var cardName by rememberSaveable { mutableStateOf(existingCard?.cardName ?: "") }
    var selectedCardType by rememberSaveable { mutableStateOf(existingCard?.cardType ?: CardType.FINANCIAL) }
    var selectedSubType by rememberSaveable { mutableStateOf(existingCard?.cardSubType ?: CardSubType.DEBIT_CARD) }
    var cardNumber by rememberSaveable { mutableStateOf(existingCard?.cardNumber ?: "") }
    var holderName by rememberSaveable { mutableStateOf(existingCard?.holderName ?: "") }
    var expiryMonth by rememberSaveable { mutableStateOf(existingCard?.expiryDate?.split("/")?.getOrNull(0) ?: "") }
    var expiryYear by rememberSaveable { mutableStateOf(existingCard?.expiryDate?.split("/")?.getOrNull(1) ?: "") }
    var cvv by rememberSaveable { mutableStateOf(existingCard?.cvv ?: "") }
    var issuer by rememberSaveable { mutableStateOf(existingCard?.issuer ?: "") }
    var notes by rememberSaveable { mutableStateOf(existingCard?.notes ?: "") }
    var dateOfBirth by rememberSaveable { mutableStateOf(existingCard?.dateOfBirth ?: "") }
    var gender by rememberSaveable { mutableStateOf(existingCard?.gender ?: "") }
    var address by rememberSaveable { mutableStateOf(existingCard?.address ?: "") }
    var fatherName by rememberSaveable { mutableStateOf(existingCard?.fatherName ?: "") }
    var issueDate by rememberSaveable { mutableStateOf(existingCard?.issueDate ?: "") }
    var validUntil by rememberSaveable { mutableStateOf(existingCard?.validUntil ?: "") }
    var authorityName by rememberSaveable { mutableStateOf(existingCard?.authorityName ?: "") }
    var detectedNetwork by remember { mutableStateOf<CardNetwork?>(existingCard?.cardNetwork) }

    var typeExpanded by remember { mutableStateOf(false) }
    var subTypeExpanded by remember { mutableStateOf(false) }

    // Sync scan results into form fields (after variable declarations)
    LaunchedEffect(scannedPan) {
        if (scannedPan.isNotEmpty()) cardNumber = scannedPan
    }
    LaunchedEffect(scannedExpiryMonth) {
        if (scannedExpiryMonth.isNotEmpty()) expiryMonth = scannedExpiryMonth
    }
    LaunchedEffect(scannedExpiryYear) {
        if (scannedExpiryYear.isNotEmpty()) expiryYear = scannedExpiryYear.takeLast(2)
    }
    LaunchedEffect(scannedHolderName) {
        if (scannedHolderName.isNotEmpty()) holderName = scannedHolderName
    }
    LaunchedEffect(cardNumber) {
        if (selectedCardType == CardType.FINANCIAL && cardNumber.length >= 4) {
            detectedNetwork = CardUtils.detectCardNetwork(cardNumber)
        }
    }

    val availableSubTypes = CardUtils.getSubTypesForType(selectedCardType)

    LaunchedEffect(selectedCardType) {
        if (!availableSubTypes.contains(selectedSubType)) {
            selectedSubType = availableSubTypes.firstOrNull() ?: CardSubType.CUSTOM
        }
    }

    val isValid = cardName.isNotBlank() && cardNumber.isNotBlank() && holderName.isNotBlank()

    // ---- Dialog ----
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
                        text = if (isEditing) "Edit Card" else "Add Card",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Circular Progress Header
                    val filledFieldsCount = listOf(cardName, cardNumber, holderName).count { it.isNotBlank() } +
                            (if (selectedCardType == CardType.FINANCIAL) listOf(expiryMonth, expiryYear, cvv).count { it.isNotBlank() } else 0)
                    val totalFields = if (selectedCardType == CardType.FINANCIAL) 6 else 3
                    val targetProgress = if (totalFields == 0) 0f else (filledFieldsCount.toFloat() / totalFields).coerceIn(0f, 1f)
                    val animatedProgress = animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 500),
                        label = "Progress Animation"
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column {
                            Icon(
                                painter = painterResource(R.drawable.card),
                                contentDescription = "Card Icon",
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.CenterHorizontally),
                            )
                            Text(
                                modifier = Modifier
                                    .padding(top = 6.dp, bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = if (targetProgress == 1f) "Complete" else "Incomplete",
                                fontFamily = FontFamily(Font(R.font.password)),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 10.dp),
                                text = "$filledFieldsCount / $totalFields fields",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---- Scan with Stripe (financial cards only) ----
                    androidx.compose.animation.AnimatedVisibility(visible = selectedCardType == CardType.FINANCIAL) {
                        Button(
                            onClick = {
                                scanLauncher.launch(
                                    Intent(context, CardScanActivity::class.java)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Card — fills data & photo")
                        }
                    }

                    // ---- Card Photo Section ----
                    CardPhotoSection(
                        label = "Front Photo",
                        imageUri = frontPhotoUri,
                        onCamera = {
                            cameraSide = "front"
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                launchCamera(context, "front", pendingFrontFile, pendingBackFile,
                                    setFrontFile = { pendingFrontFile = it },
                                    setBackFile = { pendingBackFile = it },
                                    launch = { uri -> cameraLauncher.launch(uri) }
                                )
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGallery = {
                            cameraSide = "front"
                            galleryLauncher.launch("image/*")
                        },
                        onClear = { frontPhotoUri = null }
                    )

                    CardPhotoSection(
                        label = "Back Photo (optional)",
                        imageUri = backPhotoUri,
                        onCamera = {
                            cameraSide = "back"
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                launchCamera(context, "back", pendingFrontFile, pendingBackFile,
                                    setFrontFile = { pendingFrontFile = it },
                                    setBackFile = { pendingBackFile = it },
                                    launch = { uri -> cameraLauncher.launch(uri) }
                                )
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGallery = {
                            cameraSide = "back"
                            galleryLauncher.launch("image/*")
                        },
                        onClear = { backPhotoUri = null }
                    )

                    HorizontalDivider()

                    // ---- Card Type ----
                    Text(
                        text = "Card Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedCardType == type,
                                onClick = { selectedCardType = type },
                                label = { Text(CardUtils.getCardTypeDisplayName(type)) }
                            )
                        }
                    }

                    // ---- Card Subtype ----
                    ExposedDropdownMenuBox(
                        expanded = subTypeExpanded,
                        onExpandedChange = { subTypeExpanded = !subTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = CardUtils.getCardSubTypeDisplayName(selectedSubType),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Card Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = subTypeExpanded,
                            onDismissRequest = { subTypeExpanded = false }
                        ) {
                            availableSubTypes.forEach { subType ->
                                DropdownMenuItem(
                                    text = { Text(CardUtils.getCardSubTypeDisplayName(subType)) },
                                    onClick = {
                                        selectedSubType = subType
                                        subTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // ---- Card Name ----
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Card Name / Nickname") },
                        placeholder = { Text("e.g., My Savings Card") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    // ---- Card Number ----
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            cardNumber = it.filter { c -> c.isDigit() || c.isLetter() }.take(
                                when (selectedSubType) {
                                    CardSubType.AADHAAR -> 12
                                    CardSubType.PAN -> 10
                                    CardSubType.PASSPORT -> 8
                                    else -> 19
                                }
                            )
                        },
                        label = {
                            Text(
                                when (selectedSubType) {
                                    CardSubType.AADHAAR -> "Aadhaar Number"
                                    CardSubType.PAN -> "PAN Number"
                                    CardSubType.DRIVING_LICENSE -> "License Number"
                                    CardSubType.VOTER_ID -> "Voter ID"
                                    CardSubType.PASSPORT -> "Passport Number"
                                    else -> "Card Number"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedCardType == CardType.FINANCIAL) KeyboardType.Number else KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        supportingText = {
                            if (selectedCardType == CardType.FINANCIAL &&
                                detectedNetwork != null && detectedNetwork != CardNetwork.UNKNOWN
                            ) {
                                Text("Detected: ${detectedNetwork?.name}")
                            }
                        }
                    )

                    // ---- Holder Name ----
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Holder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    // ---- Financial Card Fields ----
                    androidx.compose.animation.AnimatedVisibility(visible = selectedCardType == CardType.FINANCIAL) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = expiryMonth,
                                    onValueChange = {
                                        val filtered = it.filter { c -> c.isDigit() }.take(2)
                                        val month = filtered.toIntOrNull() ?: 0
                                        expiryMonth = if (month > 12) "12" else filtered
                                    },
                                    label = { Text("MM") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = expiryYear,
                                    onValueChange = { expiryYear = it.filter { c -> c.isDigit() }.take(2) },
                                    label = { Text("YY") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(4) },
                                    label = { Text("CVV") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            OutlinedTextField(
                                value = issuer,
                                onValueChange = { issuer = it },
                                label = { Text("Issuing Bank") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    // ---- ID Card Fields ----
                    androidx.compose.animation.AnimatedVisibility(visible = selectedCardType == CardType.GOVERNMENT_ID) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = dateOfBirth,
                                    onValueChange = { dateOfBirth = it },
                                    label = { Text("Date of Birth") },
                                    placeholder = { Text("DD/MM/YYYY") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = { gender = it },
                                    label = { Text("Gender") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                            if (selectedSubType == CardSubType.AADHAAR) {
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 3
                                )
                            }
                            if (selectedSubType == CardSubType.DRIVING_LICENSE || selectedSubType == CardSubType.PASSPORT) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = issueDate,
                                        onValueChange = { issueDate = it },
                                        label = { Text("Issue Date") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = validUntil,
                                        onValueChange = { validUntil = it },
                                        label = { Text("Valid Until") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }
                                OutlinedTextField(
                                    value = authorityName,
                                    onValueChange = { authorityName = it },
                                    label = { Text("Issuing Authority") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // ---- Notes ----
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val expiryDate = if (expiryMonth.isNotBlank() && expiryYear.isNotBlank()) {
                            "$expiryMonth/$expiryYear"
                        } else null

                        onSave(
                            Card(
                                cardId = existingCard?.cardId ?: 0,
                                cardName = cardName,
                                cardType = selectedCardType,
                                cardSubType = selectedSubType,
                                cardNumber = cardNumber,
                                cardNumberMasked = CardUtils.maskCardNumber(cardNumber),
                                holderName = holderName,
                                expiryDate = expiryDate,
                                cvv = cvv.ifBlank { null },
                                issuer = issuer.ifBlank { null },
                                cardNetwork = detectedNetwork,
                                dateOfBirth = dateOfBirth.ifBlank { null },
                                gender = gender.ifBlank { null },
                                address = address.ifBlank { null },
                                fatherName = fatherName.ifBlank { null },
                                issueDate = issueDate.ifBlank { null },
                                validUntil = validUntil.ifBlank { null },
                                authorityName = authorityName.ifBlank { null },
                                notes = notes,
                                baseColor = existingCard?.baseColor ?: CardUtils.getRandomCardColor(),
                                frontImageUri = frontPhotoUri,
                                backImageUri = backPhotoUri,
                                createdAt = existingCard?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isEditing) "Update" else "Save", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CardPhotoSection(
    label: String,
    imageUri: String?,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        if (imageUri != null) {
            // Show thumbnail with a remove button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                AsyncImage(
                    model = imageUri.toImageModel(),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // Remove button
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Camera")
                }
                FilledTonalButton(
                    onClick = onGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Gallery")
                }
            }
        }
    }
}

private fun String.toImageModel(): Any {
    return if (startsWith("content://")) Uri.parse(this) else File(this)
}

private fun launchCamera(
    context: android.content.Context,
    side: String,
    currentFrontFile: File?,
    currentBackFile: File?,
    setFrontFile: (File) -> Unit,
    setBackFile: (File) -> Unit,
    launch: (Uri) -> Unit
) {
    val storageDir = File(context.filesDir, "card_photos").also { it.mkdirs() }
    val photoFile = File(storageDir, "${side}_${System.currentTimeMillis()}.jpg")
    if (side == "front") setFrontFile(photoFile) else setBackFile(photoFile)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    launch(uri)
}

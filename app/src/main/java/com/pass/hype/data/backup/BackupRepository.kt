package com.pass.hype.data.backup

import android.content.ContentValues
import android. content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.pass.hype.data.room.dao.CardDao
import com. pass.hype. data.room.dao.PasswordDao
import com.pass. hype.data. room.model.Card
import com.pass.hype.data.room. model.CardNetwork
import com.pass.hype.data.room.model. CardSubType
import com. pass.hype. data.room.model.CardType
import com.pass.hype.data.room.model. Passwords
import com. pass.hype. utils.CardUtils
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.withContext
import java. io.File
import java.io.FileOutputStream
import java. text.SimpleDateFormat
import java.util. Locale

sealed class BackupResult {
    data class Success(val uri: Uri?, val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val passwordCount: Int, val cardCount: Int) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

class BackupRepository(
    private val passwordDao: PasswordDao,
    private val cardDao: CardDao,
    private val context: Context
) {
    private val gson:  Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    companion object {
        private const val TAG = "BackupRepository"
        private const val CURRENT_BACKUP_VERSION = 2
    }

    suspend fun createBackup(pin: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            // Get all data from database
            val passwords = passwordDao.getAllPasswordsSync()
            val cards = cardDao.getAllCardsSync()

            Log.d(TAG, "Creating backup:  ${passwords.size} passwords, ${cards. size} cards")

            // Convert to backup format
            val backupData = BackupData(
                version = CURRENT_BACKUP_VERSION,
                createdAt = System.currentTimeMillis(),
                passwords = passwords.map { it.toBackup() },
                cards = cards.map { it.toBackup() }
            )

            // Serialize to JSON
            val jsonData = gson.toJson(backupData)

            // Encrypt with PIN
            val encryptedData = EncryptionManager. encrypt(jsonData, pin)
                ?: return@withContext BackupResult.Error("Failed to encrypt backup data")

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis())
            val fileName = "hypepass_backup_$timestamp.hpb"

            val uri = saveBackupFile(fileName, encryptedData)
                ?: return@withContext BackupResult. Error("Failed to save backup file")

            Log.d(TAG, "Backup created successfully: $uri")

            BackupResult. Success(
                uri = uri,
                message = "Backup created:  ${passwords.size} passwords, ${cards.size} cards"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            BackupResult.Error("Backup failed: ${e.message}")
        }
    }

    suspend fun restoreBackup(
        uri: Uri,
        pin: String,
        replaceExisting: Boolean = false
    ): RestoreResult = withContext(Dispatchers. IO) {
        try {
            // Read encrypted data from file
            val encryptedData = readBackupFile(uri)
                ?: return@withContext RestoreResult.Error("Failed to read backup file")

            // Decrypt with PIN
            val jsonData = EncryptionManager. decrypt(encryptedData, pin)
                ?: return@withContext RestoreResult.Error("Invalid PIN or corrupted backup")

            // Detect backup version
            val version = detectBackupVersion(jsonData)
            Log.d(TAG, "Detected backup version: $version")

            // Parse based on version
            val (passwords, cards) = when (version) {
                1 -> parseV1Backup(jsonData)
                2 -> parseV2Backup(jsonData)
                else -> return@withContext RestoreResult.Error("Unsupported backup version:  $version")
            }

            // Clear existing data if requested
            if (replaceExisting) {
                Log.d(TAG, "Clearing existing data")
                passwordDao.deleteAllPasswords()
                cardDao.deleteAllCards()
            }

            // Restore passwords
            passwords.forEach { password ->
                passwordDao.upsertPassword(password)
            }
            Log.d(TAG, "Restored ${passwords.size} passwords")

            // Restore cards
            cards.forEach { card ->
                cardDao.upsertCard(card)
            }
            Log. d(TAG, "Restored ${cards. size} cards")

            RestoreResult. Success(
                passwordCount = passwords.size,
                cardCount = cards. size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            RestoreResult. Error("Restore failed: ${e.message}")
        }
    }

    private fun detectBackupVersion(jsonData: String): Int {
        return try {
            val jsonObject = JsonParser.parseString(jsonData).asJsonObject
            jsonObject.get("version")?.asInt ?: 1
        } catch (e:  Exception) {
            Log.e(TAG, "Failed to detect version, assuming v1", e)
            1
        }
    }

    private fun parseV1Backup(jsonData: String): Pair<List<Passwords>, List<Card>> {
        val backupDataV1 = gson. fromJson(jsonData, BackupDataV1::class.java)

        val passwords = backupDataV1.passwords. map { it.toEntity() }
        val cards = backupDataV1.cards.map { it.toNewCardEntity() }

        return Pair(passwords, cards)
    }

    private fun parseV2Backup(jsonData: String): Pair<List<Passwords>, List<Card>> {
        val backupData = gson.fromJson(jsonData, BackupData::class. java)

        val passwords = backupData.passwords. map { it.toEntity() }
        val cards = backupData.cards.map { it.toEntity() }

        return Pair(passwords, cards)
    }

    private fun saveBackupFile(fileName: String, data:  String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(fileName, data)
        } else {
            saveToExternalStorage(fileName, data)
        }
    }

    private fun saveWithMediaStore(fileName:  String, data: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns. DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns. MIME_TYPE, "application/octet-stream")
            put(MediaStore. MediaColumns.RELATIVE_PATH, "Documents/hype.pass")
        }

        val resolver = context.contentResolver
        val uri = resolver. insert(MediaStore. Files.getContentUri("external"), contentValues)

        return uri?. also { fileUri ->
            resolver.openOutputStream(fileUri)?.use { stream ->
                stream. write(data.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun saveToExternalStorage(fileName: String, data: String): Uri? {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment. DIRECTORY_DOCUMENTS),
            "hype. pass"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        return try {
            FileOutputStream(file).use { stream ->
                stream. write(data.toByteArray(Charsets.UTF_8))
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save file", e)
            null
        }
    }

    private fun readBackupFile(uri: Uri): String? {
        return try {
            context.contentResolver. openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read file", e)
            null
        }
    }

    // Password conversion extensions
    private fun Passwords.toBackup() = PasswordBackup(
        appName = appName,
        appIcon = appIcon,
        email = email,
        password = password,
        editTime = editTime,
        edited = edited,
        notes = notes
    )

    private fun PasswordBackup.toEntity() = Passwords(
        appName = appName,
        appIcon = appIcon,
        email = email,
        password = password,
        editTime = editTime,
        edited = edited,
        notes = notes
    )

    // New Card conversion extensions
    private fun Card.toBackup() = CardBackup(
        cardName = cardName,
        cardType = cardType. name,
        cardSubType = cardSubType. name,
        cardNumber = cardNumber,
        cardNumberMasked = cardNumberMasked,
        holderName = holderName,
        expiryDate = expiryDate,
        cvv = cvv,
        issuer = issuer,
        cardNetwork = cardNetwork?. name,
        issuingCountry = issuingCountry,
        dateOfBirth = dateOfBirth,
        gender = gender,
        address = address,
        fatherName = fatherName,
        issueDate = issueDate,
        validFrom = validFrom,
        validUntil = validUntil,
        authorityName = authorityName,
        notes = notes,
        tags = tags,
        category = category,
        isFavorite = isFavorite,
        isPinned = isPinned,
        isLocked = isLocked,
        customFields = customFields,
        baseColor = baseColor,
        frontImageUri = frontImageUri,
        backImageUri = backImageUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastAccessedAt = lastAccessedAt,
        accessCount = accessCount
    )

    private fun CardBackup.toEntity() = Card(
        cardName = cardName,
        cardType = try { CardType.valueOf(cardType) } catch (e: Exception) { CardType. CUSTOM },
        cardSubType = try { CardSubType.valueOf(cardSubType) } catch (e: Exception) { CardSubType.CUSTOM },
        cardNumber = cardNumber,
        cardNumberMasked = cardNumberMasked,
        holderName = holderName,
        expiryDate = expiryDate,
        cvv = cvv,
        issuer = issuer,
        cardNetwork = cardNetwork?.let {
            try { CardNetwork.valueOf(it) } catch (e: Exception) { null }
        },
        issuingCountry = issuingCountry,
        dateOfBirth = dateOfBirth,
        gender = gender,
        address = address,
        fatherName = fatherName,
        issueDate = issueDate,
        validFrom = validFrom,
        validUntil = validUntil,
        authorityName = authorityName,
        notes = notes,
        tags = tags,
        category = category,
        isFavorite = isFavorite,
        isPinned = isPinned,
        isLocked = isLocked,
        customFields = customFields,
        baseColor = baseColor,
        frontImageUri = frontImageUri,
        backImageUri = backImageUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastAccessedAt = lastAccessedAt,
        accessCount = accessCount
    )

    // Legacy card migration - convert old format to new format
    private fun LegacyCardBackup.toNewCardEntity(): Card {
        val detectedNetwork = CardUtils.detectCardNetwork(cardNumber)
        val detectedSubType = when {
            cardNumber.length == 16 && cardNumber.all { it.isDigit() } -> CardSubType.DEBIT_CARD
            else -> CardSubType. CUSTOM
        }

        return Card(
            cardName = cardHolder. take(20).ifBlank { "My Card" },
            cardType = CardType.FINANCIAL,
            cardSubType = detectedSubType,
            cardNumber = cardNumber,
            cardNumberMasked = CardUtils.maskCardNumber(cardNumber),
            holderName = cardHolder,
            expiryDate = expires,
            cvv = cvv,
            issuer = null,
            cardNetwork = detectedNetwork,
            issuingCountry = "India",
            notes = "Migrated from legacy backup",
            baseColor = baseColor,
            createdAt = System. currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
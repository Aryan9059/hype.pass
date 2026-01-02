package com.pass.hype.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.pass.hype.data.room.dao.CardDao
import com.pass.hype.data.room.model.Cards
import com.pass.hype.data.room.dao.PasswordDao
import com.pass.hype.data.room.model.Passwords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

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
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun createBackup(pin: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            // Get all data from database
            val passwords = passwordDao.getAllPasswordsSync()
            val cards = cardDao.getAllCardsSync()

            // Convert to backup format
            val backupData = BackupData(
                passwords = passwords.map { it.toBackup() },
                cards = cards.map { it.toBackup() }
            )

            // Serialize to JSON
            val jsonData = json. encodeToString(backupData)

            // Encrypt with PIN
            val encryptedData = EncryptionManager. encrypt(jsonData, pin)
                ?: return@withContext BackupResult. Error("Failed to encrypt backup data")

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis())
            val fileName = "hypepass_backup_$timestamp.hpb"

            val uri = saveBackupFile(fileName, encryptedData)
                ?: return@withContext BackupResult. Error("Failed to save backup file")

            BackupResult.Success(
                uri = uri,
                message = "Backup created:  ${passwords.size} passwords, ${cards.size} cards"
            )
        } catch (e: Exception) {
            Log.e("BackupRepository", "Backup failed", e)
            BackupResult.Error("Backup failed: ${e.message}")
        }
    }

    suspend fun restoreBackup(
        uri: Uri,
        pin: String,
        replaceExisting: Boolean = false
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            // Read encrypted data from file
            val encryptedData = readBackupFile(uri)
                ?: return@withContext RestoreResult.Error("Failed to read backup file")

            // Decrypt with PIN
            val jsonData = EncryptionManager.decrypt(encryptedData, pin)
                ?: return@withContext RestoreResult.Error("Invalid PIN or corrupted backup")

            // Parse JSON
            val backupData = try {
                json. decodeFromString<BackupData>(jsonData)
            } catch (_: Exception) {
                return@withContext RestoreResult.Error("Invalid backup format")
            }

            // Clear existing data if requested
            if (replaceExisting) {
                passwordDao.deleteAllPasswords()
                cardDao.deleteAllCards()
            }

            // Restore passwords
            backupData.passwords.forEach { backup ->
                passwordDao.upsertPassword(backup.toEntity())
            }

            // Restore cards
            backupData.cards.forEach { backup ->
                cardDao.upsertCard(backup.toEntity())
            }

            RestoreResult.Success(
                passwordCount = backupData.passwords.size,
                cardCount = backupData. cards.size
            )
        } catch (e: Exception) {
            Log. e("BackupRepository", "Restore failed", e)
            RestoreResult.Error("Restore failed:  ${e.message}")
        }
    }

    private fun saveBackupFile(fileName: String, data: String): Uri? {
        return if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(fileName, data)
        } else {
            saveToExternalStorage(fileName, data)
        }
    }

    private fun saveWithMediaStore(fileName:  String, data: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns. MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns. RELATIVE_PATH, "Documents/hype.pass")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files. getContentUri("external"), contentValues)

        return uri?. also { fileUri ->
            resolver.openOutputStream(fileUri)?.use { stream ->
                stream. write(data.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun saveToExternalStorage(fileName: String, data: String): Uri? {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "hype. pass"
        )
        if (!directory.exists()) {
            directory. mkdirs()
        }

        val file = File(directory, fileName)
        return try {
            FileOutputStream(file).use { stream ->
                stream.write(data. toByteArray(Charsets.UTF_8))
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("BackupRepository", "Failed to save file", e)
            null
        }
    }

    private fun readBackupFile(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream. bufferedReader().readText()
            }
        } catch (e:  Exception) {
            Log.e("BackupRepository", "Failed to read file", e)
            null
        }
    }

    // Extension functions for conversion
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

    private fun Cards.toBackup() = CardBackup(
        cardHolder = cardHolder,
        cardNumber = cardNumber,
        expires = expires,
        cvv = cvv,
        baseColor = baseColor
    )

    private fun CardBackup. toEntity() = Cards(
        cardHolder = cardHolder,
        cardNumber = cardNumber,
        expires = expires,
        cvv = cvv,
        baseColor = baseColor
    )
}
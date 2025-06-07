package com.pass.hype.presentation.settings

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pass.hype.HypePass.Companion.cardDatabase
import com.pass.hype.HypePass.Companion.passwordDatabase
import com.pass.hype.R
import com.pass.hype.data.local.cards.CardDatabase
import com.pass.hype.data.local.passwords.PasswordDatabase
import com.pass.hype.presentation.components.RestoreTile
import com.pass.hype.presentation.components.SettingsTile
import com.pass.hype.presentation.components.dialog.ChangePinDialog
import com.pass.hype.utils.RecoveryKeyAlertDialog
import com.pass.hype.utils.decryptDatabaseWithPin
import com.pass.hype.utils.encryptDatabaseWithPin
import com.pass.hype.utils.recreateApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    val context = LocalContext.current

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val pinStored = sharedPreferences.getString("stored_value", "") ?: ""

    var isChangePinDialogOpen by rememberSaveable { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    var showRecoveryDialog by rememberSaveable { mutableStateOf(false) }
    if(showRecoveryDialog) {
        RecoveryKeyAlertDialog(context) {
            showRecoveryDialog = false
        }
    }

    ChangePinDialog(
        isOpen = isChangePinDialogOpen,
        oldPin = oldPin,
        newPin = newPin,
        isStart = false,
        onDismissRequest = {
            isChangePinDialogOpen = false
            oldPin = ""
            newPin = ""
                           },
        onOldPinChanged = { oldPin = it },
        onNewPinChanged = { newPin = it },
        onChange = {
            sharedPreferences.edit { putInt("pinLength", newPin.length) }
            sharedPreferences.edit { putString("stored_value", newPin) }
            isChangePinDialogOpen = false
            Toast.makeText(context, "PIN changed successfully", Toast.LENGTH_SHORT).show()       },
        storedValue = pinStored
    )

    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    if(showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup your Data") },
            text = { Text("Saves a secure, PIN-protected backup of your data to the Documents/hype.pass folder on your device.") },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        exportDatabase(
                            context = context,
                            databaseName = "Passwords",
                            database = passwordDatabase,
                            onSuccess = { uri ->
                                Toast.makeText(context, "Backup created successfully", Toast.LENGTH_LONG).show()
                            },
                            onError = { errorMessage ->
                                Log.e("Export", "Export failed: $errorMessage")
                            }
                        )
                        exportDatabase(
                            context = context,
                            databaseName = "Cards",
                            database = cardDatabase,
                            onSuccess = { uri ->
                                Log.d("Export", "Export successful to $uri")
                            },
                            onError = { errorMessage ->
                                Log.e("Export", "Export failed: $errorMessage")
                                Toast.makeText(context, "Backup failed. $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }) {
                    Text("Backup Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(0) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .graphicsLayer {
            alpha = alphaAnimation.value
        }, horizontalAlignment = Alignment.CenterHorizontally){
        item {
            TopAppBar(title = { Text(text = "Settings", style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily(Font(R.font.password))) })
            Spacer(Modifier.size(4.dp))

            Image(
                painter = painterResource(R.drawable.banner),
                contentDescription = "Settings Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                    .graphicsLayer {
                        alpha = alphaAnimation.value
                    }
            )
            Spacer(Modifier.size(12.dp))
            Text("Security", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(start = 20.dp))

            SettingsTile(
                icon = painterResource(R.drawable.pin),
                title = "Change PIN",
                summary = "Change the security PIN of the app",
                onClick = {
                    isChangePinDialogOpen = true
                }
            )

            SettingsTile(
                icon = painterResource(R.drawable.fingerprint),
                title = "Enable Fingerprint",
                summary = "Enable biometric unlock",
                onClick = {}
            )

            SettingsTile(
                icon = painterResource(R.drawable.forget),
                title = "Forgot PIN",
                summary = "Reset your PIN using the recovery key",
                onClick = {
                    showRecoveryDialog = true
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.size(12.dp))
            Text("Backup & Restore", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(start = 20.dp))

            SettingsTile(
                icon = painterResource(R.drawable.backup),
                title = "Backup Passwords & Cards",
                summary = "Create a secure backup on your device",
                onClick = {
                    showBackupDialog = true
                }
            )

            RestoreTile(onSuccess = {
                Toast.makeText(context, "Import successful", Toast.LENGTH_LONG).show()
            }, onError = { errorMessage ->
                Log.e("Import", "Import failed: $errorMessage")
                Toast.makeText(context, "Import failed: $errorMessage", Toast.LENGTH_LONG).show()
            })

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.size(12.dp))
            Text("Miscellaneous", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(start = 20.dp))

            SettingsTile(
                        icon = painterResource(R.drawable.code),
                title = "Source Code",
                summary = "View, improve & pull our source code",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://github.com/Aryan9059/hype.pass".toUri())
                    context.startActivity(intent)
                }
            )

            SettingsTile(
                icon = painterResource(R.drawable.privacy),
                title = "Privacy Policy",
                summary = "Read the privacy policy of the app",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://github.com/Aryan9059/hype.pass/blob/master/sources/privacy_policy.md".toUri())
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.size(40.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Created with 💜 by ", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Aryan Srivastava", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.size(4.dp))
            Text(text = "Currently using v${context.packageManager.getPackageInfo(context.packageName, 0).versionName}", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline, fontSize = 12.sp))
            Spacer(Modifier.size(16.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun exportDatabase(
    context: Context,
    databaseName: String,
    database: RoomDatabase,
    onSuccess: (Uri) -> Unit,
    onError: (String) -> Unit
) = withContext(Dispatchers.IO) {
    try {
        database.close()
        delay(500)

        val pinStored = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("stored_value", "") ?: ""
        val dbFile = context.getDatabasePath(databaseName)

        if (!dbFile.exists()) {
            withContext(Dispatchers.Main) {
                onError("Database file not found")
            }
            return@withContext
        }

        val encryptedFile = encryptDatabaseWithPin(dbFile, pinStored, context)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val fileName = "${databaseName.substringBeforeLast(".")}_$timeStamp.db"

        val uri = saveToDocuments(encryptedFile, fileName, context)

        withContext(Dispatchers.Main) {
            if (uri != null) {
                onSuccess(uri)
            } else {
                onError("Failed to create file in Documents")
            }
        }

    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            onError("Export failed: ${e.message}")
        }
    }

    when (databaseName) {
        "Passwords" -> passwordDatabase = Room.databaseBuilder(
                context,
                PasswordDatabase::class.java,
                databaseName
            ).fallbackToDestructiveMigration(false).build()
        "Cards" -> cardDatabase = Room.databaseBuilder(
                context,
                CardDatabase::class.java,
                databaseName
            ).fallbackToDestructiveMigration(false).build()
    }

    if(databaseName == "Passwords") recreateApp(context)
}

private fun saveToDocuments(sourceFile: File, fileName: String, context: Context): Uri? {
    return try {
        val resolver: ContentResolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sqlite3")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/hype.pass")
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        } else {
            resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        }

        uri?.let { fileUri ->
            resolver.openOutputStream(fileUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            fileUri
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

suspend fun importDatabase(
    context: Context,
    sourceUri: Uri,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    pinStored: String
) = withContext(Dispatchers.IO) {
    val databaseName = if(sourceUri.lastPathSegment?.contains("Passwords") == true) "Passwords" else "Cards"
    val database = if (databaseName == "Passwords") passwordDatabase else cardDatabase

    Log.d("Import", "Importing database: $databaseName")

    try {
        database.close()
        delay(500)

        if (pinStored.isEmpty()) {
            withContext(Dispatchers.Main) {
                onError("No PIN found for decryption")
            }
            return@withContext
        }

        val tempEncryptedFile = createTempFileFromUri(sourceUri, context)
        if (tempEncryptedFile == null) {
            withContext(Dispatchers.Main) {
                onError("Failed to read source file")
            }
            return@withContext
        }

        val decryptedFile = decryptDatabaseWithPin(tempEncryptedFile, pinStored, context)

        val targetDbFile = context.getDatabasePath(databaseName)

        if (targetDbFile.exists()) {
            val backupFile = File(targetDbFile.parent, "${databaseName}.backup")
            targetDbFile.copyTo(backupFile, overwrite = true)
        }

        decryptedFile?.copyTo(targetDbFile, overwrite = true)
        tempEncryptedFile.delete()
        decryptedFile?.delete()

        withContext(Dispatchers.Main) {
            onSuccess()
        }

    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            onError(e.message.toString())
        }
    }

    when (databaseName) {
        "Passwords" -> passwordDatabase = Room.databaseBuilder(
            context,
            PasswordDatabase::class.java,
            databaseName
        ).fallbackToDestructiveMigration(false).build()
        "Cards" -> cardDatabase = Room.databaseBuilder(
            context,
            CardDatabase::class.java,
            databaseName
        ).fallbackToDestructiveMigration(false).build()
    }

    recreateApp(context)
}

private fun createTempFileFromUri(uri: Uri, context: Context): File? {
    return try {
        val resolver = context.contentResolver
        val tempFile = File.createTempFile("import_db_", ".tmp", context.cacheDir)

        resolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        tempFile
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}





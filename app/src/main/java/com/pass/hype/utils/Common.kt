package com.pass.hype.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pass.hype.R
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

val appList = listOf(
    "Amazon", "Apple", "ChatGPT", "Facebook", "GitHub", "Google", "Instagram",
    "LinkedIn", "Microsoft", "Netflix", "Pinterest", "Reddit", "Snapchat",
    "Spotify","TikTok", "X")

val cardList = listOf(
    "VISA", "MASTERCARD", "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS", "OTHER")

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

    }

fun detectCardCompany(number: String): String {
    return when (number. firstOrNull()) {
        '5', '2' -> "MASTERCARD"
        '4' -> "VISA"
        '3' -> "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS"
        else -> "OTHER"
    }
}

//Generate a random 18 character strong password
fun generateStrongPassword(length: Int = 18): String {
    val upperCaseLetters = ('A'..'Z').toList()
    val lowerCaseLetters = ('a'..'z').toList()
    val numbers = ('0'..'9').toList()
    val specialCharacters = listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '=', '[', ']', '?')

    val password = mutableListOf<Char>().apply {
        add(upperCaseLetters.random())
        add(lowerCaseLetters.random())
        add(numbers.random())
        add(specialCharacters.random())

        val remainingLength = length - 4
        val allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters

        repeat(remainingLength) {
            add(allCharacters.random())
        }
    }
    return password.shuffled().joinToString("")
}

//Function to check strength of a password
fun String.getPasswordStrength(): String {
    if (isEmpty()) return "Very Weak"

    var score = 0
    var hasUpper = false
    var hasLower = false
    var hasDigit = false
    var hasSpecial = false

    score += when {
        length >= 16 -> 3
        length >= 12 -> 2
        length >= 8 -> 1
        else -> 0
    }

    var consecutiveCount = 1
    var previousChar = '\u0000'

    for (char in this) {
        when {
            char.isUpperCase() -> hasUpper = true
            char.isLowerCase() -> hasLower = true
            char.isDigit() -> hasDigit = true
            char.code > 32 && !char.isLetterOrDigit() -> hasSpecial = true
        }

        if (char == previousChar) {
            consecutiveCount++
            if (consecutiveCount >= 3) {
                score--
            }
        } else {
            consecutiveCount = 1
        }
        previousChar = char

        if (hasUpper && hasLower && hasDigit && hasSpecial && length >= 8) {
            break
        }
    }

    if (hasUpper) score++
    if (hasLower) score++
    if (hasDigit) score++
    if (hasSpecial) score++

    val lowerCase = lowercase()
    if (lowerCase.contains("password") ||
        lowerCase.contains("123456") ||
        contains("abc") ||
        contains("qwerty")) {
        score -= 2
    }

    score = score.coerceIn(0, 7)

    return when (score) {
        0, 1 -> "Very Weak"
        2 -> "Weak"
        3, 4 -> "Fair"
        5 -> "Good"
        else -> "Strong"
    }
}

//Function to encrypt a file using a PIN
fun encryptDatabaseWithPin(dbFile: File, pin: String, context: Context): File {
    try {
        val encryptedFile = File(context.cacheDir, "${dbFile.name}_encrypted")
        val inputData = dbFile.readBytes()

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val secretKey = factory.generateSecret(spec)
        val key = SecretKeySpec(secretKey.encoded, "AES")

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encryptedData = cipher.doFinal(inputData)

        val outputData = ByteArray(salt.size + iv.size + encryptedData.size)
        System.arraycopy(salt, 0, outputData, 0, salt.size)
        System.arraycopy(iv, 0, outputData, salt.size, iv.size)
        System.arraycopy(encryptedData, 0, outputData, salt.size + iv.size, encryptedData.size)

        encryptedFile.writeBytes(outputData)
        return encryptedFile
    } catch (e: Exception) {
        Log.e("Encryption", "Failed to encrypt database: ${e.message}")
        return dbFile
    }
}

//Function to decrypt a file using a PIN
fun decryptDatabaseWithPin(encryptedFile: File, pin: String, context: Context): File? {
    return try {
        val decryptedFile = File(context.cacheDir, "${encryptedFile.name.replace("_encrypted", "")}_decrypted")
        val inputData = encryptedFile.readBytes()

        val salt = inputData.copyOfRange(0, 16)
        val iv = inputData.copyOfRange(16, 32)
        val encryptedData = inputData.copyOfRange(32, inputData.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val secretKey = factory.generateSecret(spec)
        val key = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decryptedData = cipher.doFinal(encryptedData)

        decryptedFile.writeBytes(decryptedData)

        if (!isValidSQLiteDatabase(decryptedFile)) {
            decryptedFile.delete()
            throw InvalidPinException("Invalid file")
        }

        decryptedFile
    } catch (_: javax.crypto.BadPaddingException) {
        Log.e("Decryption", "Bad padding")
        throw InvalidPinException("Invalid PIN")
    } catch (_: javax.crypto.IllegalBlockSizeException) {
        Log.e("Decryption", "Illegal block size")
        throw InvalidPinException("Invalid or corrupted file")
    } catch (_: InvalidPinException) {
        Log.e("Decryption", "Invalid PIN")
        throw InvalidPinException("Invalid PIN")
    } catch (_: Exception) {
        Log.e("Decryption", "Failed to decrypt database")
        throw DecryptionException("Failed to decrypt database")
    }
}

private fun isValidSQLiteDatabase(file: File): Boolean {
    return try {
        val bytes = file.readBytes()
        val sqliteHeader = "SQLite format 3\u0000".toByteArray()

        if (bytes.size < sqliteHeader.size) {
            return false
        }

        for (i in sqliteHeader.indices) {
            if (bytes[i] != sqliteHeader[i]) {
                return false
            }
        }

        true
    } catch (e: Exception) {
        Log.e("Validation", "Error validating SQLite file: ${e.message}")
        false
    }
}

class InvalidPinException(message: String) : Exception(message)
class DecryptionException(message: String) : Exception(message)

fun recreateApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}

//Generate a strong recovery key
fun generateStrongRecoveryKey(length: Int = 12): String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val secureRandom = SecureRandom()
    return (1..length)
        .map { charset[secureRandom.nextInt(charset.length)] }
        .joinToString("")
}

//Alert to restore pin using recovery key
@Composable
fun RecoveryKeyAlertDialog(
    context: Context,
    onDismiss: () -> Unit,
) {
    var enteredRecoveryKey by remember { mutableStateOf("") }
    val onBoardSharedPrefs: SharedPreferences = context.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
    val pinSharedPrefs: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val recoveryKey = onBoardSharedPrefs.getString("RecoveryKey", "")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore PIN") },
        text = {
            Column {
                Text("Please enter your recovery key to restore your PIN")
                Spacer(modifier = Modifier.height(16.dp))

                var keyVisibility: Boolean by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = enteredRecoveryKey,
                    onValueChange = {
                        enteredRecoveryKey = it
                    },
                    label = { Text("Recovery Key") },
                    textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.password))),
                    visualTransformation = if (keyVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (keyVisibility) R.drawable.eye_close else R.drawable.eye_open

                        val description = if (keyVisibility) "Hide Key" else "Show Key"

                        IconButton(onClick = {keyVisibility = !keyVisibility}){
                            Icon(painter = painterResource(image), description, modifier = Modifier.size(20.dp))
                        }
                    },
                    isError = enteredRecoveryKey != recoveryKey,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (recoveryKey == enteredRecoveryKey) Toast.makeText(context, "Your PIN is: ${pinSharedPrefs.getString("stored_value", "")}", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                enabled = recoveryKey == enteredRecoveryKey
            ) {
                Text("Show PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
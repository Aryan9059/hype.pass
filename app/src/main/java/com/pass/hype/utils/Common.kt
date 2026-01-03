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

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

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
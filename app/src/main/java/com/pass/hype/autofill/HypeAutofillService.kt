package com.pass.hype.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.content.SharedPreferences
import android.os.CancellationSignal
import android.os.Build
import android.service.autofill. AutofillService
import android.service.autofill. FillCallback
import android.service.autofill. FillRequest
import android.service.autofill. FillResponse
import android.service.autofill.SaveCallback
import android. service.autofill.SaveRequest
import android.util.Log
import androidx.annotation.RequiresApi
import com.pass.hype.autofill.builder.ResponseBuilder
import com.pass.hype.autofill.model.ParsedStructure
import com.pass.hype.autofill.parser.StructureParser
import com.pass.hype.autofill.repository.AutofillRepository
import com.pass.hype.data.room.model.Passwords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class HypeAutofillService :  AutofillService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers. IO)
    private lateinit var repository: AutofillRepository
    private lateinit var responseBuilder: ResponseBuilder
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HypeAutofillService created")
        repository = AutofillRepository(applicationContext)
        responseBuilder = ResponseBuilder(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal:  CancellationSignal,
        callback: FillCallback
    ) {
        Log. d(TAG, "onFillRequest called")

        val structure = request.fillContexts. lastOrNull()?.structure
        if (structure == null) {
            Log. d(TAG, "No structure found")
            callback.onSuccess(null)
            return
        }

        // Parse the structure
        val parser = StructureParser(structure)
        val parsedStructure = parser.parse()

        Log.d(TAG, "Parsed structure:  packageName=${parsedStructure.packageName}, " +
                "webDomain=${parsedStructure.webDomain}, " +
                "hasValidFields=${parsedStructure.hasValidFields}")

        if (!parsedStructure.hasValidFields) {
            Log. d(TAG, "No valid fields found")
            callback.onSuccess(null)
            return
        }

        // Skip our own app
        if (parsedStructure.packageName == packageName) {
            Log.d(TAG, "Skipping own app")
            callback.onSuccess(null)
            return
        }

        serviceScope.launch {
            try {
                val isAuthenticated = isUserAuthenticated()
                Log.d(TAG, "User authenticated: $isAuthenticated")

                val passwords = if (isAuthenticated) {
                    repository.getAllPasswords()
                } else {
                    emptyList()
                }

                Log.d(TAG, "Found ${passwords.size} passwords")

                val response = responseBuilder. buildResponse(
                    parsedStructure = parsedStructure,
                    passwords = passwords,
                    isAuthenticated = isAuthenticated
                )

                Log.d(TAG, "Response built: ${response != null}")
                callback.onSuccess(response)
            } catch (e:  Exception) {
                Log.e(TAG, "Error building fill response", e)
                callback.onSuccess(null)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log. d(TAG, "onSaveRequest called")

        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            callback.onSuccess()
            return
        }

        val parser = StructureParser(structure)
        val parsedStructure = parser.parse()

        if (!parsedStructure. hasValidFields) {
            callback.onSuccess()
            return
        }

        // Extract values from the structure
        var username:  String? = null
        var email: String? = null
        var password:  String? = null

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            windowNode.rootViewNode?. let { node ->
                extractValues(node, parsedStructure) { field, value ->
                    when (field) {
                        "username" -> username = value
                        "email" -> email = value
                        "password" -> password = value
                    }
                }
            }
        }

        val usernameOrEmail = email ?: username
        if (usernameOrEmail. isNullOrEmpty() || password. isNullOrEmpty()) {
            callback.onSuccess()
            return
        }

        // Determine app name from package or domain
        val appName = parsedStructure.webDomain?. substringBefore(".")
            ?. replaceFirstChar { it.uppercase() }
            ?: parsedStructure.packageName. substringAfterLast(".")
                . replaceFirstChar { it.uppercase() }

        serviceScope.launch {
            try {
                val newPassword = Passwords(
                    appName = appName,
                    appIcon = appName.lowercase(),
                    email = usernameOrEmail,
                    password = password,
                    editTime = System.currentTimeMillis().toString(),
                    edited = false,
                    notes = "Saved via Autofill from ${parsedStructure.identifier}"
                )
                repository.savePassword(newPassword)
                callback.onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving password", e)
                callback.onFailure("Failed to save password")
            }
        }
    }

    private fun extractValues(
        node: AssistStructure. ViewNode,
        parsedStructure: ParsedStructure,
        onValueFound: (String, String) -> Unit
    ) {
        val autofillId = node.autofillId
        val value = node.autofillValue?. textValue?. toString()

        if (autofillId != null && ! value.isNullOrEmpty()) {
            when (autofillId) {
                parsedStructure.usernameField?. autofillId -> onValueFound("username", value)
                parsedStructure. emailField?.autofillId -> onValueFound("email", value)
                parsedStructure.passwordField?.autofillId -> onValueFound("password", value)
            }
        }

        for (i in 0 until node.childCount) {
            extractValues(node.getChildAt(i), parsedStructure, onValueFound)
        }
    }

    private fun isUserAuthenticated(): Boolean {
        val lastAuthTime = prefs. getLong(KEY_LAST_AUTH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val authTimeout = prefs.getLong(KEY_AUTH_TIMEOUT, DEFAULT_AUTH_TIMEOUT)
        return (currentTime - lastAuthTime) < authTimeout
    }

    override fun onConnected() {
        super.onConnected()
        Log. d(TAG, "Autofill service connected")
    }

    override fun onDisconnected() {
        super. onDisconnected()
        Log. d(TAG, "Autofill service disconnected")
    }

    companion object {
        private const val TAG = "HypeAutofillService"
        const val PREFS_NAME = "autofill_prefs"
        const val KEY_LAST_AUTH_TIME = "last_auth_time"
        const val KEY_AUTH_TIMEOUT = "auth_timeout"
        const val DEFAULT_AUTH_TIMEOUT = 5 * 60 * 1000L // 5 minutes

        fun setAuthenticated(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context. MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
                .apply()
        }

        fun clearAuthentication(context: Context) {
            context. getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_AUTH_TIME, 0)
                .apply()
        }
    }
}
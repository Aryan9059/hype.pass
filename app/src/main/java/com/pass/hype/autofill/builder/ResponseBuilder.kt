package com.pass.hype.autofill.builder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.service.autofill. Dataset
import android.service.autofill. FillResponse
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.pass.hype.R
import com.pass.hype.autofill.AutofillAuthActivity
import com.pass.hype.autofill.AutofillSelectionActivity
import com.pass.hype.autofill.model.ParsedStructure
import com.pass.hype.data.room.model.Passwords

class ResponseBuilder(
    private val context:  Context
) {
    fun buildResponse(
        parsedStructure: ParsedStructure,
        passwords: List<Passwords>,
        isAuthenticated: Boolean
    ): FillResponse?  {
        if (! parsedStructure.hasValidFields) return null

        val responseBuilder = FillResponse. Builder()

        if (! isAuthenticated) {
            // Build authentication response
            return buildAuthResponse(parsedStructure, responseBuilder)
        }

        // Filter passwords that match the app/website
        val matchingPasswords = filterMatchingPasswords(passwords, parsedStructure)

        if (matchingPasswords. isEmpty()) {
            // Show option to search all passwords
            val searchDataset = buildSearchDataset(parsedStructure)
            responseBuilder.addDataset(searchDataset)
        } else {
            // Add matching passwords as datasets
            matchingPasswords.take(5).forEach { password ->
                val dataset = buildDataset(password, parsedStructure)
                if (dataset != null) {
                    responseBuilder.addDataset(dataset)
                }
            }

            // Add "More options" dataset
            if (matchingPasswords.size > 5 || passwords.size > matchingPasswords.size) {
                val moreOptionsDataset = buildSearchDataset(parsedStructure)
                responseBuilder.addDataset(moreOptionsDataset)
            }
        }

        // Configure save info
        val saveInfoBuilder = buildSaveInfo(parsedStructure)
        if (saveInfoBuilder != null) {
            responseBuilder.setSaveInfo(saveInfoBuilder)
        }

        return try {
            responseBuilder.build()
        } catch (e:  Exception) {
            null
        }
    }

    private fun buildAuthResponse(
        parsedStructure: ParsedStructure,
        responseBuilder: FillResponse. Builder
    ): FillResponse?  {
        val authPresentation = RemoteViews(context.packageName, R.layout.autofill_auth_item).apply {
            setTextViewText(R.id.autofill_title, "Unlock HypePass")
            setTextViewText(R.id.autofill_subtitle, "Tap to authenticate")
            setImageViewResource(R. id.autofill_icon, R. drawable.lock)
        }

        val authIntent = Intent(context, AutofillAuthActivity::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, parsedStructure.packageName)
            putExtra(EXTRA_WEB_DOMAIN, parsedStructure. webDomain)
            parsedStructure.usernameField?.let {
                putExtra(EXTRA_USERNAME_ID, it.autofillId)
            }
            parsedStructure.emailField?.let {
                putExtra(EXTRA_EMAIL_ID, it.autofillId)
            }
            parsedStructure. passwordField?.let {
                putExtra(EXTRA_PASSWORD_ID, it.autofillId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_AUTH,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent. FLAG_MUTABLE
        )

        val datasetBuilder = Dataset.Builder(authPresentation)

        parsedStructure.usernameField?.let {
            datasetBuilder.setValue(it.autofillId, null)
        }
        parsedStructure.emailField?.let {
            datasetBuilder.setValue(it.autofillId, null)
        }
        parsedStructure.passwordField?.let {
            datasetBuilder.setValue(it.autofillId, null)
        }

        datasetBuilder.setAuthentication(pendingIntent. intentSender)

        return try {
            responseBuilder.addDataset(datasetBuilder. build()).build()
        } catch (e: Exception) {
            null
        }
    }

    private fun buildDataset(
        password: Passwords,
        parsedStructure: ParsedStructure
    ): Dataset? {
        val presentation = RemoteViews(context.packageName, R.layout. autofill_item).apply {
            setTextViewText(R.id.autofill_title, password.appName)
            setTextViewText(R.id.autofill_subtitle, password.email)
            setImageViewResource(R. id.autofill_icon, R. drawable.password)
        }

        val datasetBuilder = Dataset.Builder(presentation)
        var hasValue = false

        parsedStructure.usernameField?.let { field ->
            datasetBuilder.setValue(
                field.autofillId,
                AutofillValue. forText(password.email)
            )
            hasValue = true
        }

        parsedStructure.emailField?.let { field ->
            datasetBuilder.setValue(
                field.autofillId,
                AutofillValue. forText(password. email)
            )
            hasValue = true
        }

        parsedStructure.passwordField?.let { field ->
            datasetBuilder.setValue(
                field. autofillId,
                AutofillValue.forText(password.password)
            )
            hasValue = true
        }

        return if (hasValue) {
            try {
                datasetBuilder. build()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    private fun buildSearchDataset(parsedStructure: ParsedStructure): Dataset {
        val presentation = RemoteViews(context.packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_title, "Search HypePass")
            setTextViewText(R.id.autofill_subtitle, "Find more passwords")
            setImageViewResource(R.id.autofill_icon, R.drawable.search)
        }

        val searchIntent = Intent(context, AutofillSelectionActivity::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, parsedStructure.packageName)
            putExtra(EXTRA_WEB_DOMAIN, parsedStructure.webDomain)
            parsedStructure. usernameField?. let {
                putExtra(EXTRA_USERNAME_ID, it.autofillId)
            }
            parsedStructure.emailField?.let {
                putExtra(EXTRA_EMAIL_ID, it. autofillId)
            }
            parsedStructure. passwordField?.let {
                putExtra(EXTRA_PASSWORD_ID, it.autofillId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_SEARCH,
            searchIntent,
            PendingIntent. FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val datasetBuilder = Dataset. Builder(presentation)

        parsedStructure.usernameField?.let {
            datasetBuilder.setValue(it.autofillId, null)
        }
        parsedStructure. emailField?.let {
            datasetBuilder.setValue(it. autofillId, null)
        }
        parsedStructure.passwordField?. let {
            datasetBuilder.setValue(it.autofillId, null)
        }

        datasetBuilder.setAuthentication(pendingIntent.intentSender)

        return datasetBuilder.build()
    }

    private fun buildSaveInfo(parsedStructure: ParsedStructure): SaveInfo? {
        val saveTypes = mutableListOf<Int>()
        val requiredIds = mutableListOf<android.view.autofill.AutofillId>()
        val optionalIds = mutableListOf<android.view. autofill.AutofillId>()

        parsedStructure.passwordField?.let {
            saveTypes. add(SaveInfo.SAVE_DATA_TYPE_PASSWORD)
            requiredIds.add(it.autofillId)
        }

        parsedStructure.usernameField?.let {
            saveTypes. add(SaveInfo. SAVE_DATA_TYPE_USERNAME)
            optionalIds.add(it.autofillId)
        }

        parsedStructure.emailField?. let {
            saveTypes.add(SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS)
            optionalIds.add(it.autofillId)
        }

        if (requiredIds.isEmpty()) return null

        val saveType = saveTypes.fold(0) { acc, type -> acc or type }

        val builder = SaveInfo.Builder(saveType, requiredIds. toTypedArray())

        if (optionalIds. isNotEmpty()) {
            builder.setOptionalIds(optionalIds.toTypedArray())
        }

        return builder.build()
    }

    private fun filterMatchingPasswords(
        passwords: List<Passwords>,
        parsedStructure:  ParsedStructure
    ): List<Passwords> {
        val identifier = parsedStructure.identifier. lowercase()
        val webDomain = parsedStructure.webDomain?.lowercase()
        val packageName = parsedStructure.packageName. lowercase()

        return passwords.filter { password ->
            val appNameLower = password.appName.lowercase()

            // Match by app name
            appNameLower.contains(identifier) ||
                    identifier.contains(appNameLower) ||
                    // Match by web domain
                    (webDomain != null && (
                            appNameLower.contains(webDomain. substringBefore(". ")) ||
                                    webDomain.contains(appNameLower)
                            )) ||
                    // Match by package name
                    packageName.contains(appNameLower) ||
                    appNameLower.split(" ").any { word ->
                        packageName.contains(word) || webDomain?. contains(word) == true
                    }
        }. sortedByDescending { password ->
            // Prioritize exact matches
            val appNameLower = password.appName.lowercase()
            when {
                appNameLower == identifier -> 3
                webDomain?. contains(appNameLower) == true -> 2
                packageName.contains(appNameLower) -> 1
                else -> 0
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_WEB_DOMAIN = "web_domain"
        const val EXTRA_USERNAME_ID = "username_id"
        const val EXTRA_EMAIL_ID = "email_id"
        const val EXTRA_PASSWORD_ID = "password_id"
        const val REQUEST_CODE_AUTH = 1001
        const val REQUEST_CODE_SEARCH = 1002
    }
}
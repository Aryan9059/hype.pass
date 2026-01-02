package com.pass.hype.autofill.parser

import com.pass.hype.autofill.model.AutofillFieldInfo
import com.pass.hype.autofill.model.ParsedStructure
import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import com.pass.hype.autofill.model.FieldType

class StructureParser(
    private val structure: AssistStructure
) {
    private var packageName: String = ""
    private var webDomain: String? = null
    private var usernameField: AutofillFieldInfo? = null
    private var passwordField: AutofillFieldInfo? = null
    private var emailField: AutofillFieldInfo? = null

    fun parse(): ParsedStructure {
        packageName = structure.activityComponent?. packageName ?: ""

        for (i in 0 until structure. windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            windowNode.rootViewNode?. let { parseNode(it) }
        }

        return ParsedStructure(
            packageName = packageName,
            webDomain = webDomain,
            usernameField = usernameField,
            passwordField = passwordField,
            emailField = emailField
        )
    }

    private fun parseNode(node: AssistStructure. ViewNode) {
        // Extract web domain if available
        node.webDomain?.let { domain ->
            if (domain.isNotEmpty()) {
                webDomain = domain. lowercase().removePrefix("www.")
            }
        }

        // Check if this is an autofillable field
        val autofillId = node.autofillId
        val autofillType = node.autofillType
        val autofillHints = node.autofillHints?. toList() ?: emptyList()
        val inputType = node.inputType
        val hint = node.hint?. lowercase() ?: ""
        val idEntry = node.idEntry?. lowercase() ?: ""
        val className = node.className?. lowercase() ?: ""

        if (autofillId != null && isEditText(className)) {
            val fieldType = determineFieldType(autofillHints, inputType, hint, idEntry)

            if (fieldType != FieldType.UNKNOWN) {
                val fieldInfo = AutofillFieldInfo(
                    autofillId = autofillId,
                    autofillType = autofillType,
                    autofillHints = autofillHints,
                    fieldType = fieldType,
                    webDomain = webDomain,
                    packageName = packageName
                )

                when (fieldType) {
                    FieldType.USERNAME -> if (usernameField == null) usernameField = fieldInfo
                    FieldType. EMAIL -> if (emailField == null) emailField = fieldInfo
                    FieldType.PASSWORD -> if (passwordField == null) passwordField = fieldInfo
                    FieldType.UNKNOWN -> {}
                }
            }
        }

        // Recursively parse child nodes
        for (i in 0 until node.childCount) {
            parseNode(node.getChildAt(i))
        }
    }

    private fun isEditText(className: String): Boolean {
        return className. contains("edittext") ||
                className.contains("textinputedittext") ||
                className.contains("appcompatedittext") ||
                className. contains("textview")
    }

    private fun determineFieldType(
        autofillHints: List<String>,
        inputType:  Int,
        hint: String,
        idEntry: String
    ): FieldType {
        // Check autofill hints first (most reliable)
        for (autofillHint in autofillHints) {
            when {
                autofillHint.contains(View.AUTOFILL_HINT_PASSWORD, ignoreCase = true) ->
                    return FieldType.PASSWORD
                autofillHint.contains(View.AUTOFILL_HINT_USERNAME, ignoreCase = true) ->
                    return FieldType.USERNAME
                autofillHint. contains(View. AUTOFILL_HINT_EMAIL_ADDRESS, ignoreCase = true) ->
                    return FieldType.EMAIL
            }
        }

        // Check input type
        val inputTypeClass = inputType and InputType.TYPE_MASK_CLASS
        val inputTypeVariation = inputType and InputType.TYPE_MASK_VARIATION

        if (inputTypeClass == InputType.TYPE_CLASS_TEXT) {
            when (inputTypeVariation) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType. TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> return FieldType.PASSWORD

                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType. TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> return FieldType.EMAIL
            }
        }

        // Check hint and id for common patterns
        val combinedText = "$hint $idEntry"

        return when {
            PASSWORD_PATTERNS.any { combinedText.contains(it) } -> FieldType.PASSWORD
            EMAIL_PATTERNS.any { combinedText. contains(it) } -> FieldType. EMAIL
            USERNAME_PATTERNS.any { combinedText. contains(it) } -> FieldType. USERNAME
            else -> FieldType. UNKNOWN
        }
    }

    companion object {
        private val PASSWORD_PATTERNS = listOf(
            "password", "passwd", "pass", "pwd", "secret", "credential"
        )
        private val EMAIL_PATTERNS = listOf(
            "email", "e-mail", "mail"
        )
        private val USERNAME_PATTERNS = listOf(
            "username", "user", "login", "account", "userid", "user_id", "user-id"
        )
    }
}
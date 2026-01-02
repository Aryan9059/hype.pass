package com.pass.hype.autofill.model

import android.view.View
import android.view.autofill.AutofillId

data class AutofillFieldInfo(
    val autofillId: AutofillId,
    val autofillType: Int,
    val autofillHints: List<String>,
    val fieldType: FieldType,
    val webDomain: String?  = null,
    val packageName: String? = null
)

enum class FieldType {
    USERNAME,
    EMAIL,
    PASSWORD,
    UNKNOWN
}

data class ParsedStructure(
    val packageName: String,
    val webDomain: String?,
    val usernameField: AutofillFieldInfo?,
    val passwordField: AutofillFieldInfo?,
    val emailField: AutofillFieldInfo?
) {
    val hasValidFields: Boolean
        get() = usernameField != null || passwordField != null || emailField != null

    val identifier: String
        get() = webDomain ?: packageName
}
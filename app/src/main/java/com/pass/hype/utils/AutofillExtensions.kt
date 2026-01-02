package com.pass.hype.utils

import android.content.Context
import android.os.Build
import android.view.autofill.AutofillManager

/**
 * Check if the app's autofill service is currently enabled
 */
fun Context.isAutofillServiceEnabled(): Boolean {
    if (Build.VERSION. SDK_INT < Build.VERSION_CODES.O) {
        return false
    }

    val autofillManager = getSystemService(AutofillManager::class.java)
    return autofillManager?. hasEnabledAutofillServices() == true
}

/**
 * Check if autofill is supported on this device
 */
fun Context.isAutofillSupported(): Boolean {
    if (Build.VERSION. SDK_INT < Build.VERSION_CODES.O) {
        return false
    }

    val autofillManager = getSystemService(AutofillManager::class.java)
    return autofillManager?.isAutofillSupported == true
}

/**
 * Request to disable autofill for a specific view
 */
fun Context.disableAutofill() {
    if (Build.VERSION.SDK_INT >= Build. VERSION_CODES. O) {
        val autofillManager = getSystemService(AutofillManager:: class.java)
        autofillManager?.disableAutofillServices()
    }
}
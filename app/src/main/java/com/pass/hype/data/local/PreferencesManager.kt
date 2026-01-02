package com.pass.hype.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val pinPrefs: SharedPreferences =
        context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE)

    private val onBoardPrefs: SharedPreferences =
        context.getSharedPreferences(ONBOARD_PREFS_NAME, Context.MODE_PRIVATE)

    val storedPin: String
        get() = pinPrefs.getString(KEY_STORED_VALUE, "") ?: ""

    val isFingerprintEnabled: Boolean
        get() = pinPrefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)

    val pinLength: Int
        get() = pinPrefs.getInt(KEY_PIN_LENGTH, 0)

    val isUserNew: Boolean
        get() = onBoardPrefs. getString(KEY_IS_USER_NEW, null) == null

    fun updatePin(newPin: String) {
        pinPrefs.edit { putString(KEY_STORED_VALUE, newPin) }
    }

    fun setUserOnboarded() {
        onBoardPrefs.edit { putString(KEY_IS_USER_NEW, "No") }
    }

    companion object {
        private const val PIN_PREFS_NAME = "MyPrefs"
        private const val ONBOARD_PREFS_NAME = "onBoarding"
        private const val KEY_STORED_VALUE = "stored_value"
        private const val KEY_FINGERPRINT_ENABLED = "isFingerprintEnabled"
        private const val KEY_PIN_LENGTH = "pinLength"
        private const val KEY_IS_USER_NEW = "isUserNew"
    }
}
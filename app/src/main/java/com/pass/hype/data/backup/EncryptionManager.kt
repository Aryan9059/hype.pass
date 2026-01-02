package com.pass.hype.data.backup

import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 16

    fun encrypt(data: String, pin:  String): String? {
        return try {
            val salt = ByteArray(SALT_SIZE).apply {
                SecureRandom().nextBytes(this)
            }
            val iv = ByteArray(IV_SIZE).apply {
                SecureRandom().nextBytes(this)
            }

            val key = deriveKey(pin, salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

            val encryptedData = cipher. doFinal(data. toByteArray(Charsets.UTF_8))

            // Combine:  salt + iv + encrypted data
            val combined = ByteArray(salt.size + iv. size + encryptedData.size)
            System.arraycopy(salt, 0, combined, 0, salt.size)
            System. arraycopy(iv, 0, combined, salt.size, iv.size)
            System.arraycopy(encryptedData, 0, combined, salt.size + iv.size, encryptedData. size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log. e("EncryptionManager", "Encryption failed", e)
            null
        }
    }

    fun decrypt(encryptedData: String, pin: String): String? {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

            val salt = combined.copyOfRange(0, SALT_SIZE)
            val iv = combined.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
            val encrypted = combined.copyOfRange(SALT_SIZE + IV_SIZE, combined.size)

            val key = deriveKey(pin, salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EncryptionManager", "Decryption failed", e)
            null
        }
    }

    private fun deriveKey(pin: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory. getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey. encoded, "AES")
    }
}
package com.example.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Holds the user's own Gemini key. It is encrypted with a non-exportable AndroidKeyStore AES key
 * and never leaves the device (excluded from cloud backup / device transfer).
 */
class ApiKeyStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): String {
        val stored = prefs.getString(KEY_GEMINI, null) ?: return ""
        return when {
            stored.startsWith(ENCRYPTED_PREFIX) -> decrypt(stored.removePrefix(ENCRYPTED_PREFIX)).orEmpty()
            stored.startsWith(PLAIN_PREFIX) -> stored.removePrefix(PLAIN_PREFIX)
            else -> ""
        }.trim()
    }

    fun hasKey(): Boolean = get().isNotBlank()

    /** Random per-install id the shared proxy uses for rate limiting. Carries no personal data. */
    fun deviceId(): String =
        prefs.getString(KEY_DEVICE_ID, null) ?: java.util.UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    fun set(key: String) {
        val trimmed = key.trim()
        if (trimmed.isBlank()) return
        // If the device keystore is broken (some OEMs), keep the key usable in app-private storage.
        val value = encrypt(trimmed)?.let { ENCRYPTED_PREFIX + it } ?: (PLAIN_PREFIX + trimmed)
        prefs.edit().putString(KEY_GEMINI, value).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_GEMINI).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String? = try {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(cipherText, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }

    private fun decrypt(payload: String): String? = try {
        val (iv, cipherText) = payload.split(":", limit = 2).map { Base64.decode(it, Base64.NO_WRAP) }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        }
        String(cipher.doFinal(cipherText), Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }

    companion object {
        // Must stay in sync with the <exclude> entries in backup_rules.xml / data_extraction_rules.xml.
        const val PREFS_NAME = "ai_settings"
        private const val KEY_GEMINI = "gemini_api_key"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ALIAS = "job_tracker_gemini_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFIX = "v1:"
        private const val PLAIN_PREFIX = "p:"
    }
}

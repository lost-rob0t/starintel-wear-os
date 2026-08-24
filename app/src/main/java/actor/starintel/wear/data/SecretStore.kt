package actor.starintel.wear.data

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecretStore(context: Context) {
    private val prefs = context.getSharedPreferences("starintel.secrets", Context.MODE_PRIVATE)

    fun putToken(token: String) {
        if (token.isBlank()) {
            prefs.edit().remove(KEY_TOKEN).apply()
            return
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(token.toByteArray(Charsets.UTF_8))

        val packed = ByteArray(cipher.iv.size + ciphertext.size)
        cipher.iv.copyInto(packed, 0)
        ciphertext.copyInto(packed, cipher.iv.size)

        prefs.edit()
            .putString(KEY_TOKEN, Base64.encodeToString(packed, Base64.NO_WRAP))
            .apply()
    }

    fun getToken(): String? {
        val encoded = prefs.getString(KEY_TOKEN, null) ?: return null

        return runCatching {
            val packed = Base64.decode(encoded, Base64.NO_WRAP)
            require(packed.size > IV_BYTES)

            val iv = packed.copyOfRange(0, IV_BYTES)
            val ciphertext = packed.copyOfRange(IV_BYTES, packed.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        }.getOrNull()
    }

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance("AES", KEYSTORE).run {
            init(
                android.security.keystore.KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "starintel.wear.api.token"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_TOKEN = "api_token"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}

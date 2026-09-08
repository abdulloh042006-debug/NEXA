package ai.nexa.core.data.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** Stores only a Keystore-wrapped random SQLCipher passphrase in the no-backup directory. */
@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val keyFile = AtomicFile(context.noBackupFilesDir.resolve(KEY_FILE_NAME))

    @Synchronized
    fun getOrCreate(): ByteArray = if (keyFile.baseFile.exists()) read() else create()

    private fun create(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())
            updateAAD(AAD)
        }
        val ciphertext = cipher.doFinal(passphrase)
        val output = keyFile.startWrite()
        try {
            val data = DataOutputStream(output)
            data.writeInt(FILE_VERSION)
            data.writeInt(cipher.iv.size)
            data.write(cipher.iv)
            data.writeInt(ciphertext.size)
            data.write(ciphertext)
            data.flush()
            keyFile.finishWrite(output)
        } catch (error: IOException) {
            keyFile.failWrite(output)
            passphrase.fill(0)
            throw error
        }
        return passphrase
    }

    private fun read(): ByteArray = DataInputStream(keyFile.openRead()).use { data ->
        check(data.readInt() == FILE_VERSION) { "Unsupported database key file version" }
        val ivLength = data.readInt()
        require(ivLength in MIN_IV_BYTES..MAX_IV_BYTES) { "Invalid database key IV length" }
        val iv = ByteArray(ivLength).also(data::readFully)
        val ciphertextLength = data.readInt()
        require(ciphertextLength in MIN_CIPHERTEXT_BYTES..MAX_CIPHERTEXT_BYTES) {
            "Invalid encrypted database key length"
        }
        val ciphertext = ByteArray(ciphertextLength).also(data::readFully)
        check(data.read() == -1) { "Unexpected trailing database key data" }
        Cipher.getInstance(CIPHER_TRANSFORMATION).run {
            init(Cipher.DECRYPT_MODE, getOrCreateWrappingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            updateAAD(AAD)
            doFinal(ciphertext)
        }
    }

    private fun getOrCreateWrappingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "nexa.primary.database.wrap.v1"
        const val KEY_FILE_NAME = "nexa-primary-key.v1"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val FILE_VERSION = 1
        const val PASSPHRASE_BYTES = 32
        const val GCM_TAG_BITS = 128
        const val MIN_IV_BYTES = 12
        const val MAX_IV_BYTES = 32
        const val MIN_CIPHERTEXT_BYTES = PASSPHRASE_BYTES + GCM_TAG_BITS / 8
        const val MAX_CIPHERTEXT_BYTES = 128
        val AAD = "NEXA_PRIMARY_DATABASE_KEY_V1".toByteArray()
    }
}

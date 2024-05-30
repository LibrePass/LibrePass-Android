package dev.medzik.librepass.android.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dev.medzik.android.crypto.KeyStore
import dev.medzik.android.crypto.KeyStoreAlias
import dev.medzik.libcrypto.Hex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.nio.charset.StandardCharsets


private enum class KeyAlias : KeyStoreAlias {
    DataStore
}

@Serializable
data class SecretsStore(
    val aesKey: String
)

private object SecretsStoreSerializer : Serializer<SecretsStore> {
    override val defaultValue = SecretsStore("")

    override suspend fun readFrom(input: InputStream): SecretsStore {
        val cipherTextWithIVBuilder = StringBuilder()
        BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
            var c: Int
            while ((reader.read().also { c = it }) != -1) {
                cipherTextWithIVBuilder.append(c.toChar())
            }
        }

        // initialization vector length in hex string
        val ivLength = 12 * 2

        // extract IV and Cipher Text from hex string
        val iv = cipherTextWithIVBuilder.substring(0, ivLength)
        val cipherText = cipherTextWithIVBuilder.substring(ivLength)

        // decrypt cipher text
        val cipher = KeyStore.initForDecryption(KeyAlias.DataStore, Hex.decode(iv), false)

        val decrypted = KeyStore.decrypt(cipher, cipherText)

        return Json.decodeFromString(String(decrypted))
    }

    override suspend fun writeTo(
        t: SecretsStore,
        output: OutputStream
    ) {
        val json = Json.encodeToString(t)

        val cipher = KeyStore.initForEncryption(KeyAlias.DataStore, false)
        val cipherData = KeyStore.encrypt(cipher, json.toByteArray())

        val cipherText = cipherData.initializationVector + cipherData.cipherText

        OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
            writer.write(cipherText)
        }
    }
}

private val Context.secretsDataStore: DataStore<SecretsStore> by dataStore(
    fileName = "secretsStore.pb",
    serializer = SecretsStoreSerializer
)

fun readSecretsStore(context: Context): SecretsStore {
    return runBlocking { context.secretsDataStore.data.first() }
}

suspend fun writeSecretsStore(context: Context, preference: SecretsStore) {
    context.secretsDataStore.updateData { preference }
}

suspend fun deleteSecretsStore(context: Context) {
    context.secretsDataStore.updateData { SecretsStore("") }
}

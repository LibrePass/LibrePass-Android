package dev.medzik.librepass.android.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dev.medzik.android.crypto.EncryptedDataStore
import dev.medzik.android.crypto.EncryptedDataStoreSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SecretsStore(
    val aesKey: String
) : EncryptedDataStore

private object SecretsStoreSerializer : EncryptedDataStoreSerializer<SecretsStore> {
    override val defaultValue = SecretsStore("")
    override val keyStoreAlias = DataStoreKeyAlias

    override fun decode(str: String): SecretsStore {
        return Json.decodeFromString(str)
    }

    override fun encode(t: SecretsStore): String {
        return Json.encodeToString(t)
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

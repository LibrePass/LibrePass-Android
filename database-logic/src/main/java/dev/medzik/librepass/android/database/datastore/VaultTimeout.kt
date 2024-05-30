package dev.medzik.librepass.android.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class VaultTimeout(
    val timeout: VaultTimeoutValue = VaultTimeoutValue.FIFTEEN_MINUTES,
    val expires: Long = System.currentTimeMillis()
)

enum class VaultTimeoutValue(val minutes: Int) {
    INSTANT(0),
    ONE_MINUTE(1),
    FIVE_MINUTES(5),
    FIFTEEN_MINUTES(15),
    THIRTY_MINUTES(30),
    ONE_HOUR(60),
    NEVER(-1);

    companion object {
        fun fromMinutes(minutes: Int): VaultTimeoutValue {
            for (value in entries) {
                if (value.minutes == minutes) {
                    return value
                }
            }

            throw IllegalArgumentException()
        }
    }
}

private object VaultTimeoutSerializer : Serializer<VaultTimeout> {
    override val defaultValue = VaultTimeout()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): VaultTimeout {
        return Json.decodeFromStream(input)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t: VaultTimeout,
        output: OutputStream
    ) = Json.encodeToStream(t, output)
}

private val Context.vaultTimeoutDataStore: DataStore<VaultTimeout> by dataStore(
    fileName = "vaultTimeout.pb",
    serializer = VaultTimeoutSerializer
)

fun readVaultTimeout(context: Context): VaultTimeout {
    return runBlocking { context.vaultTimeoutDataStore.data.first() }
}

suspend fun writeVaultTimeout(context: Context, preference: VaultTimeout) {
    context.vaultTimeoutDataStore.updateData { preference }
}

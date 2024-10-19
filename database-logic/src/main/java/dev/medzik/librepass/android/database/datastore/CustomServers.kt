package dev.medzik.librepass.android.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class CustomServers(
    val name: String,
    val address: String
)

private object CustomServersSerializer : Serializer<List<CustomServers>> {
    override val defaultValue = emptyList<CustomServers>()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): List<CustomServers> {
        return Json.decodeFromStream(input)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t: List<CustomServers>,
        output: OutputStream
    ) = Json.encodeToStream(t, output)
}

private val Context.customServersDataStore: DataStore<List<CustomServers>> by dataStore(
    fileName = "customServers.pb",
    serializer = CustomServersSerializer
)

suspend fun readCustomServers(context: Context): List<CustomServers> {
    return context.customServersDataStore.data.first()
}

suspend fun writeCustomServers(context: Context, preference: List<CustomServers>) {
    context.customServersDataStore.updateData { preference }
}

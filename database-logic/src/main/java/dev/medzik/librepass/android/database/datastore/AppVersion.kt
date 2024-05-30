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
data class AppVersion(
    val lastVersionLaunched: Int
)

private object AppVersionSerializer : Serializer<AppVersion> {
    override val defaultValue = AppVersion(0)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): AppVersion {
        return Json.decodeFromStream(input)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t: AppVersion,
        output: OutputStream
    ) = Json.encodeToStream(t, output)
}

private val Context.appVersionDataStore: DataStore<AppVersion> by dataStore(
    fileName = "appVersion.pb",
    serializer = AppVersionSerializer
)

fun readAppVersion(context: Context): AppVersion {
    return runBlocking { context.appVersionDataStore.data.first() }
}

suspend fun writeAppVersion(context: Context, preference: AppVersion) {
    context.appVersionDataStore.updateData { preference }
}

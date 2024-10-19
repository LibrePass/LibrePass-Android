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
data class PasswordGeneratorPreference(
    val length: Int = 16,
    val capitalize: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true
)

private object PasswordGeneratorPreferenceSerializer : Serializer<PasswordGeneratorPreference> {
    override val defaultValue = PasswordGeneratorPreference()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): PasswordGeneratorPreference {
        return Json.decodeFromStream(input)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t: PasswordGeneratorPreference,
        output: OutputStream
    ) = Json.encodeToStream(t, output)
}

private val Context.passwordGeneratorPreferenceDataStore: DataStore<PasswordGeneratorPreference> by dataStore(
    fileName = "passwordGeneratorPreference.pb",
    serializer = PasswordGeneratorPreferenceSerializer
)

suspend fun readPasswordGeneratorPreference(context: Context): PasswordGeneratorPreference {
    return context.passwordGeneratorPreferenceDataStore.data.first()
}

suspend fun writePasswordGeneratorPreference(context: Context, preference: PasswordGeneratorPreference) {
    context.passwordGeneratorPreferenceDataStore.updateData { preference }
}

package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStoreUtils.readEncrypted
import dev.medzik.android.cryptoutils.DataStoreUtils.writeEncrypted
import kotlinx.coroutines.runBlocking

val Context.dataStoreSecrets by preferencesDataStore(name = "librepass_secrets")

private const val DS_PRIVATE_KEY = "private_key"
private const val DS_SECRET_KEY = "secret_key"

suspend fun Context.getUserSecrets(): UserDataStoreSecrets? {
    val privateKey = dataStoreSecrets.readEncrypted(DS_PRIVATE_KEY)
    val secretKey = dataStoreSecrets.readEncrypted(DS_SECRET_KEY)

    return if (!privateKey.isNullOrBlank() && !secretKey.isNullOrBlank()) {
        UserDataStoreSecrets(
            privateKey = privateKey,
            secretKey = secretKey
        )
    } else null
}

fun Context.getUserSecretsSync() = runBlocking { getUserSecrets() }

suspend fun Context.writeUserSecrets(secrets: UserDataStoreSecrets) {
    dataStoreSecrets.writeEncrypted(DS_PRIVATE_KEY, secrets.privateKey)
    dataStoreSecrets.writeEncrypted(DS_SECRET_KEY, secrets.secretKey)
}

data class UserDataStoreSecrets(
    val privateKey: String,
    val secretKey: String
)

package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStoreUtils.readEncrypted

val Context.dataStoreSecrets by preferencesDataStore(name = "librepass_secrets")

const val DS_PRIVATE_KEY = "private_key"
const val DS_SECRET_KEY = "secret_key"

fun Context.getPrivateKeyFromDataStore() = dataStoreSecrets.readEncrypted(DS_SECRET_KEY)
fun Context.getSecretKeyFromDataStore() = dataStoreSecrets.readEncrypted(DS_SECRET_KEY)

package dev.medzik.librepass.android.business

import android.content.Context
import dev.medzik.librepass.android.database.Credentials
import dev.medzik.librepass.android.database.injection.DatabaseProvider
import dev.medzik.librepass.client.api.CipherClient
import java.util.Date
import java.util.concurrent.TimeUnit

suspend fun syncCiphers(
    context: Context,
    credentials: Credentials,
    client: CipherClient,
    vault: VaultCache
) {
    val currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(Date().time)

    val repository = DatabaseProvider.provideRepository(context)

    val localCiphers = repository.cipher.getAll(credentials.userId)

    val lastSync = credentials.lastSync ?: 0
    val lastSyncDate = Date(TimeUnit.SECONDS.toMillis(lastSync))

    val needUpload = localCiphers.filter { it.needUpload }.map { it.encryptedCipher }
    // TODO: delete ciphers using this method
    val syncResponse = client.sync(lastSyncDate, needUpload, emptyList())

    // last sync is zero for first sync
    if (lastSync != 0L) {
        // synchronize the local database with the server database
        vault.sync(syncResponse)
    } else {
        // save all ciphers
        for (cipher in syncResponse.ciphers) {
            vault.save(cipher)
        }
    }

    // update the last sync date
    repository.credentials.update(credentials.copy(lastSync = currentTimeSeconds))
}

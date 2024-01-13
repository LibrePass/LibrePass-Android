package dev.medzik.librepass.android

import android.content.Context
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import kotlinx.coroutines.runBlocking

object Migrations {
    fun update(
        context: Context,
        repository: Repository
    ) {
        if (repository.credentials.get() == null) {
            context.writeKey(StoreKey.AppVersionCode, BuildConfig.VERSION_CODE)
            return
        }

        val lastVersionCode = context.readKey(StoreKey.AppVersionCode)
        var versionCode = lastVersionCode

        while (versionCode < BuildConfig.VERSION_CODE) {
            when (versionCode) {
                -1 -> disableBiometric(repository)
                11 -> disableBiometric(repository)
            }

            versionCode++
        }

        context.writeKey(StoreKey.AppVersionCode, versionCode)
    }

    private fun disableBiometric(repository: Repository) {
        val credentials = repository.credentials.get() ?: return

        runBlocking {
            repository.credentials.update(
                credentials.copy(
                    biometricReSetup = true,
                    biometricAesKey = null,
                    biometricAesKeyIV = null
                )
            )
        }
    }
}

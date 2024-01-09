package dev.medzik.librepass.android

import android.content.Context
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.utils.SecretStore
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
                -1 -> merge4To5(context, repository)
            }

            versionCode++
        }

        context.writeKey(StoreKey.AppVersionCode, versionCode)
    }

    private fun merge4To5(
        context: Context,
        repository: Repository
    ) {
        val credentials = repository.credentials.get() ?: return

        SecretStore.delete(context)

        runBlocking {
            repository.credentials.update(
                credentials.copy(
                    biometricEnabled = false,
                    biometricPrivateKey = null,
                    biometricPrivateKeyIV = null
                )
            )
        }
    }
}

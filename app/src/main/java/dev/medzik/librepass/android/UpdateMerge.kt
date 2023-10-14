package dev.medzik.librepass.android

import android.content.Context
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import kotlinx.coroutines.runBlocking

object UpdateMerge {
    fun update(context: Context) {
        if (context.getRepository().credentials.get() == null) {
            context.writeKey(StoreKey.AppVersionCode, BuildConfig.VERSION_CODE)
            return
        }

        val lastVersionCode = context.readKey(StoreKey.AppVersionCode)
        var versionCode = lastVersionCode

        while (versionCode < BuildConfig.VERSION_CODE) {
            when (versionCode) {
                -1 -> merge4To5(context)
            }

            versionCode++
        }

        context.writeKey(StoreKey.AppVersionCode, versionCode)
    }

    private fun merge4To5(context: Context) {
        val repositoryCredentials = context.getRepository().credentials
        val credentials = repositoryCredentials.get()
            ?: return

        SecretStore.delete(context)

        runBlocking {
            repositoryCredentials.update(
                credentials.copy(
                    biometricProtectedPrivateKey = null,
                    biometricProtectedPrivateKeyIV = null,
                    biometricEnabled = false
                )
            )
        }
    }
}

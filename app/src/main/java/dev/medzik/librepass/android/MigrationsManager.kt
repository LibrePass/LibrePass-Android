package dev.medzik.librepass.android

import android.content.Context
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.database.datastore.AppVersion
import dev.medzik.librepass.android.database.datastore.readAppVersion
import dev.medzik.librepass.android.database.datastore.writeAppVersion
import kotlinx.coroutines.runBlocking

object MigrationsManager {
    fun run(
        context: Context,
        repository: Repository
    ) {
        if (repository.credentials.get() == null) {
            runBlocking { writeAppVersion(context, AppVersion(BuildConfig.VERSION_CODE)) }
            return
        }

        val appVersion = readAppVersion(context)
        var versionCode = appVersion.lastVersionLaunched

        while (versionCode < BuildConfig.VERSION_CODE) {
            when (versionCode) {
                0 -> disableBiometric(repository)
            }

            versionCode++
        }

        runBlocking { writeAppVersion(context, AppVersion(versionCode)) }
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

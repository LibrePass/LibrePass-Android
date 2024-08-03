package dev.medzik.librepass.android.database

import android.content.Context
import dev.medzik.librepass.android.database.tables.CredentialsDao
import dev.medzik.librepass.android.database.tables.CustomServerDao
import dev.medzik.librepass.android.database.tables.LocalCipherDao

/**
 * Repository interface provides database DAOs.
 */
interface Repository {
    val credentials: CredentialsDao
    val cipher: LocalCipherDao
    val customServer: CustomServerDao
}

/**
 * Repository class provides access to the database.
 */
class RepositoryImpl(context: Context) : Repository {
    // get database instance
    private val database = DatabaseProvider.getInstance(context)

    override val credentials = database.credentialsDao()
    override val cipher = database.cipherDao()
    override val customServer = database.customServerDao()
}

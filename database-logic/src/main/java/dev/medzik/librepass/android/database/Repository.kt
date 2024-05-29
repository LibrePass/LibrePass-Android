package dev.medzik.librepass.android.database

import android.content.Context

/**
 * Repository interface provides database DAOs.
 */
interface RepositoryInterface {
    val credentials: CredentialsDao
    val cipher: LocalCipherDao
}

/**
 * Repository class provides access to the database.
 */
class Repository(context: Context) : RepositoryInterface {
    // get database instance
    private val database = DatabaseProvider.getInstance(context)

    override val credentials = database.credentialsDao()
    override val cipher = database.cipherDao()
}

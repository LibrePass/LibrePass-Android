package dev.medzik.librepass.android.data

import android.content.Context

/**
 * Repository interface.
 * @property credentials Credentials DAO.
 * @property cipher Cipher DAO.
 */
interface RepositoryInterface {
    val credentials: CredentialsDao
    val cipher: CipherDao
}

/**
 * Repository provides access to the database.
 */
class Repository(context: Context) : RepositoryInterface {
    // get database instance
    private val database = LibrePassDatabaseProvider.getInstance(context)

    override val credentials = database.credentialsDao()
    override val cipher = database.cipherDao()
}

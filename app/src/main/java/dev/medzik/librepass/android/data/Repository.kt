package dev.medzik.librepass.android.data

import android.content.Context
import java.util.UUID

interface RepositoryInterface {
    val credentials: CredentialsDao
    val cipher: CipherDao
}

class Repository(context: Context) : RepositoryInterface {
    private val database = LibrePassDatabaseProvider.getInstance(context)

    override val credentials = RepositoryCredentials(database.credentialsDao())
    override val cipher = CipherCredentials(database.cipherDao())
}

class RepositoryCredentials(private val credentialsDao: CredentialsDao) : CredentialsDao {
    override suspend fun insert(credentials: Credentials) {
        credentialsDao.insert(credentials)
    }

    override fun get(): Credentials? {
        return credentialsDao.get()
    }

    override suspend fun update(credentials: Credentials) {
        credentialsDao.update(credentials)
    }

    override suspend fun drop() {
        credentialsDao.drop()
    }
}

class CipherCredentials(private val cipherDao: CipherDao) : CipherDao {
    override suspend fun insert(cipherTable: CipherTable) {
        cipherDao.insert(cipherTable)
    }

    override fun get(id: UUID): CipherTable? {
        return cipherDao.get(id)
    }

    override fun getAll(owner: UUID): List<CipherTable> {
        return cipherDao.getAll(owner)
    }

    override suspend fun update(cipherTable: CipherTable) {
        cipherDao.update(cipherTable)
    }

    override suspend fun drop(owner: UUID) {
        cipherDao.drop(owner)
    }
}

package dev.medzik.librepass.android.data

import android.content.Context

class Repository(context: Context) : CredentialsDao {
    private val credentialsDao = CredentialsDatabaseProvider.getInstance(context).credentialsDao()

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

package dev.medzik.librepass.android.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 2,
    entities = [Credentials::class, LocalCipher::class],
    exportSchema = false
)
abstract class LibrePassDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao

    abstract fun cipherDao(): LocalCipherDao
}

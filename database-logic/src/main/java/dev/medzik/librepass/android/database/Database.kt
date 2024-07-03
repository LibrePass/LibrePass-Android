package dev.medzik.librepass.android.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 3,
    entities = [Credentials::class, LocalCipher::class, CustomServer::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class LibrePassDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao
    abstract fun cipherDao(): LocalCipherDao
    abstract fun customServerDao(): CustomServerDao
}

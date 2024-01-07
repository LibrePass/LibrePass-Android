package dev.medzik.librepass.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 2,
    entities = [Credentials::class, LocalCipher::class],
    exportSchema = false
)
abstract class LibrePassDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao

    abstract fun cipherDao(): CipherDao
}

/**
 * Database provider singleton class.
 */
object LibrePassDatabaseProvider {
    private var database: LibrePassDatabase? = null

    /**
     * Get database instance. If database is not initialized, it will be initialize.
     *
     * @param context Application context.
     * @return Database instance.
     */
    fun getInstance(context: Context): LibrePassDatabase {
        if (database == null) {
            database =
                Room.databaseBuilder(
                    context,
                    LibrePassDatabase::class.java,
                    "librepass.db"
                )
                    .addMigrations(DatabaseMigrations.MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
        }

        return database as LibrePassDatabase
    }
}

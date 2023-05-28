package dev.medzik.librepass.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Credentials::class, CipherTable::class, Settings::class],
    version = 1,
    exportSchema = false
)
abstract class LibrePassDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao
    abstract fun cipherDao(): CipherDao
    abstract fun settingsDao(): SettingsDao
}

/**
 * Database provider singleton class.
 */
object LibrePassDatabaseProvider {
    private var database: LibrePassDatabase? = null

    /**
     * Get database instance. If database is not initialized, it will be created.
     * @param context Application context.
     * @return Database instance.
     */
    fun getInstance(context: Context): LibrePassDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context,
                LibrePassDatabase::class.java,
                "librepass.db"
            )
                .allowMainThreadQueries()
                .build()
        }

        return database as LibrePassDatabase
    }
}

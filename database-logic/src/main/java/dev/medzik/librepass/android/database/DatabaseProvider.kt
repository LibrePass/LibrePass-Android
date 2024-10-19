package dev.medzik.librepass.android.database

import android.content.Context
import androidx.room.Room

/**
 * Database provider singleton class.
 */
object DatabaseProvider {
    private var database: LibrePassDatabase? = null

    /**
     * Get database instance. If database is not initialized, it will be initialize.
     *
     * @param context application context
     * @return Database instance.
     */
    fun getInstance(context: Context): LibrePassDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
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
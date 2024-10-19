package dev.medzik.librepass.android.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Room

object DatabaseProvider {
    private var database: LibrePassDatabase? = null

    /**
     * Get database instance. If the database is not initialized, it will be initialize.
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
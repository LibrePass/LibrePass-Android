package dev.medzik.librepass.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Credentials::class], version = 1)
abstract class CredentialsDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao
}

object CredentialsDatabaseProvider {
    private var database: CredentialsDatabase? = null

    fun getInstance(context: Context): CredentialsDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context,
                CredentialsDatabase::class.java,
                "credentials.db"
            )
                .allowMainThreadQueries()
                .build()
        }

        return database!!
    }
}

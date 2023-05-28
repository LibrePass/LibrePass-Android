package dev.medzik.librepass.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SettingsDao {
    @Insert
    fun insert(settings: Settings)

    @Query("SELECT * FROM settings LIMIT 1")
    fun get(): Settings?

    @Update
    fun update(settings: Settings)
}

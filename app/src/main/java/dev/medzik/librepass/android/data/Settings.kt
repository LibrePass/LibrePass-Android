package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Settings(
    @PrimaryKey
    val id: Int = 0,

    val dynamicColor: Boolean = true,
    val theme: Int = 0
)

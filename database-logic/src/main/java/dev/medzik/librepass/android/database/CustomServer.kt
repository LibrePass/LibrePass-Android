package dev.medzik.librepass.android.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomServer(
    val name: String,
    @PrimaryKey
    val address: String
)

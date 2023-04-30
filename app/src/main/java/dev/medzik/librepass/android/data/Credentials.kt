package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Credentials(
    @PrimaryKey val userId: UUID,
    val email: String,

    val accessToken: String,
    val refreshToken: String,

    val encryptionKey: String,

    val lastSync: Long? = null,
)

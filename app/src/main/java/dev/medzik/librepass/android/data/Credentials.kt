package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Credentials(
    @PrimaryKey val userId: UUID,
    val email: String,

    var accessToken: String,
    var refreshToken: String,

    var encryptionKey: String,
)

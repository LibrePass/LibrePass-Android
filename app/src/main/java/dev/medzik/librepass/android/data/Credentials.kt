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
    // If network error when vault unlocking
    // Require to refresh credentials when network returns
    val requireRefresh: Boolean = false,

    val encryptionKey: String,

    // for biometric auth
    val biometricEncryptionKey: String? = null,
    val biometricEncryptionKeyIV: String? = null,

    // settings
    val biometricEnabled: Boolean = false,

    val lastSync: Long? = null
)

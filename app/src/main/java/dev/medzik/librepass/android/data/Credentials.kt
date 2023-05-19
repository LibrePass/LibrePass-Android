package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Credentials(
    @PrimaryKey val userId: UUID,
    val email: String,

    val accessToken: String,
    val encryptionKey: String,

    // argon2id parameters
    val memory: Int,
    val iterations: Int,
    val parallelism: Int,
    val version: Int,

    // for biometric auth
    val biometricEncryptionKey: String? = null,
    val biometricEncryptionKeyIV: String? = null,

    // settings
    val biometricEnabled: Boolean = false,

    val lastSync: Long? = null
)

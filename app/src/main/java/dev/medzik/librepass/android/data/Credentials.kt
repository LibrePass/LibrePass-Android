package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Credentials(
    @PrimaryKey
    val userId: UUID,
    val email: String,

    val apiUrl: String? = null,
    val apiKey: String,
    val publicKey: String,

    val lastSync: Long? = null,

    // argon2id parameters
    val memory: Int,
    val iterations: Int,
    val parallelism: Int,

    // for biometric auth
    val biometricProtectedPrivateKey: String? = null,
    val biometricProtectedPrivateKeyIV: String? = null,

    // settings
    val biometricEnabled: Boolean = false
)

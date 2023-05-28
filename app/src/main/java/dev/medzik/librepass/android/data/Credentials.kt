package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Credentials for a user.
 * @property userId unique user id
 * @property email user email
 * @property accessToken access token for the API
 * @property encryptionKey encryption key for encrypting/decrypting (encrypted using password)
 * @property lastSync last time the data was synced with the server
 * @property memory argon2id memory parameter
 * @property iterations argon2id iterations parameter
 * @property parallelism argon2id parallelism parameter
 * @property version argon2id version parameter
 * @property biometricEncryptionKey encryption key for encrypting/decrypting (encrypted using biometric)
 * @property biometricEncryptionKeyIV initialization vector for biometricEncryptionKey
 * @property biometricEnabled whether biometric auth is enabled
 * @property dynamicColor whether dynamic colors are enabled
 * @property darkMode whether dark mode is enabled (0 = system, 1 = light, 2 = dark)
 */
@Entity
data class Credentials(
    @PrimaryKey
    val userId: UUID,
    val email: String,

    val accessToken: String,
    val encryptionKey: String,

    val lastSync: Long? = null,

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
    val dynamicColor: Boolean = true,
    val darkMode: Int = 0
)

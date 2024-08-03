package dev.medzik.librepass.android.database.tables

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.util.*

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
    val biometricAesKey: String? = null,
    val biometricAesKeyIV: String? = null,
    val biometricReSetup: Boolean = false
)

@Dao
interface CredentialsDao {
    @Insert
    suspend fun insert(credentials: Credentials)

    @Query("SELECT * FROM credentials LIMIT 1")
    fun get(): Credentials?

    @Update
    suspend fun update(credentials: Credentials)

    @Query("DELETE FROM credentials")
    suspend fun drop()
}

package dev.medzik.android.crypto

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.medzik.android.crypto.DataStore.delete
import dev.medzik.android.crypto.DataStore.deleteEncrypted
import dev.medzik.android.crypto.DataStore.read
import dev.medzik.android.crypto.DataStore.readEncrypted
import dev.medzik.android.crypto.DataStore.write
import dev.medzik.android.crypto.DataStore.writeEncrypted
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private val Context.dataStore by preferencesDataStore(name = "crypto_datastore_tests")

@RunWith(AndroidJUnit4::class)
class DataStoreTests {
    enum class KeyAlias : KeyStoreAlias {
        TEST_DATASTORE_ENCRYPTED
    }

    private val testKey = stringPreferencesKey("test_key")

    @Test
    fun testDataStore() =
        runBlocking {
            val value = "Hello World!"

            val context = InstrumentationRegistry.getInstrumentation().context

            context.dataStore.write(testKey, value)
            assertEquals(value, context.dataStore.read(testKey))

            context.dataStore.delete(testKey)
            assertEquals(null, context.dataStore.read(testKey))
        }

    private val preferenceKeyEnc = "test_enc_key"

    @Test
    fun testEncryptedDataStore() =
        runBlocking {
            val value = "Hello World!"

            val context = InstrumentationRegistry.getInstrumentation().context

            context.dataStore.writeEncrypted(KeyAlias.TEST_DATASTORE_ENCRYPTED, preferenceKeyEnc, value.toByteArray())
            assertEquals(value, String(context.dataStore.readEncrypted(KeyAlias.TEST_DATASTORE_ENCRYPTED, preferenceKeyEnc)!!))

            context.dataStore.deleteEncrypted(preferenceKeyEnc)
            assertEquals(null, context.dataStore.readEncrypted(KeyAlias.TEST_DATASTORE_ENCRYPTED, preferenceKeyEnc))
        }
}

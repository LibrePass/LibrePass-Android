package dev.medzik.librepass.android.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.medzik.librepass.android.data.CipherDao
import dev.medzik.librepass.android.utils.Vault
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultModule {
    @Singleton
    @Provides
    fun providesVault(cipherRepository: CipherDao): Vault {
        return Vault(cipherRepository)
    }
}

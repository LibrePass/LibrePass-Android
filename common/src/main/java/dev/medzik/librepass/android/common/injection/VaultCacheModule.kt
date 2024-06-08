package dev.medzik.librepass.android.common.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.medzik.librepass.android.common.VaultCache
import dev.medzik.librepass.android.database.LocalCipherDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultCacheModule {
    @Singleton
    @Provides
    fun providesVault(cipherRepository: LocalCipherDao): VaultCache {
        return VaultCache(cipherRepository)
    }
}

package dev.medzik.librepass.android.business.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.medzik.librepass.android.business.VaultCache
import dev.medzik.librepass.android.database.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultCacheModule {
    @Singleton
    @Provides
    fun provideVaultCache(repository: Repository): VaultCache {
        return VaultCache(repository)
    }
}

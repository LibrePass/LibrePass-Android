package dev.medzik.librepass.android.database.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.medzik.librepass.android.database.CredentialsDao
import dev.medzik.librepass.android.database.LocalCipherDao
import dev.medzik.librepass.android.database.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Singleton
    @Provides
    fun providesRepository(
        @ApplicationContext context: Context
    ): Repository {
        return Repository(context)
    }

    @Singleton
    @Provides
    fun providesCipherRepository(repository: Repository): LocalCipherDao {
        return repository.cipher
    }

    @Singleton
    @Provides
    fun provideCredentialRepository(repository: Repository): CredentialsDao {
        return repository.credentials
    }
}

package dev.medzik.librepass.android.database.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.medzik.librepass.android.database.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseProvider {
    @Singleton
    @Provides
    fun provideRepository(@ApplicationContext context: Context): Repository {
        return Repository(context)
    }
}

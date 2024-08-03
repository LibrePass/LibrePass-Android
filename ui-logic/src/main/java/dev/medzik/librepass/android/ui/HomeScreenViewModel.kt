package dev.medzik.librepass.android.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medzik.librepass.android.database.tables.Credentials
import dev.medzik.librepass.android.database.Repository
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun getCredentials(): Credentials? {
        return repository.credentials.get()
    }
}

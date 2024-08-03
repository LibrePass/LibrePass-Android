package dev.medzik.librepass.android.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medzik.librepass.android.database.tables.CustomServer
import dev.medzik.librepass.android.database.Repository
import javax.inject.Inject

@HiltViewModel
class ChoiceServerViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun getCustomServers() = repository.customServer.getAll()
    suspend fun insertCustomServer(customServer: CustomServer) =
        repository.customServer.insert(customServer)
}

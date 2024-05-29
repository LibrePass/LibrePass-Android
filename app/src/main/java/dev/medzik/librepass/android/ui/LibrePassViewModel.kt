package dev.medzik.librepass.android.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medzik.librepass.android.database.CredentialsDao
import dev.medzik.librepass.android.database.LocalCipherDao
import dev.medzik.librepass.android.utils.Vault
import javax.inject.Inject

@HiltViewModel
class LibrePassViewModel @Inject constructor(
    val cipherRepository: LocalCipherDao,
    val credentialRepository: CredentialsDao,
    val vault: Vault
) : ViewModel()

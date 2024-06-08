package dev.medzik.librepass.android.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medzik.librepass.android.common.VaultCache
import dev.medzik.librepass.android.database.CredentialsDao
import dev.medzik.librepass.android.database.LocalCipherDao
import javax.inject.Inject

@HiltViewModel
class LibrePassViewModel @Inject constructor(
    val cipherRepository: LocalCipherDao,
    val credentialRepository: CredentialsDao,
    val vault: VaultCache
) : ViewModel()

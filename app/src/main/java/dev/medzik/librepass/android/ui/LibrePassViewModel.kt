package dev.medzik.librepass.android.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medzik.librepass.android.data.CipherDao
import dev.medzik.librepass.android.data.CredentialsDao
import javax.inject.Inject

@HiltViewModel
class LibrePassViewModel
    @Inject
    constructor(
        val cipherRepository: CipherDao,
        val credentialRepository: CredentialsDao
    ) : ViewModel()

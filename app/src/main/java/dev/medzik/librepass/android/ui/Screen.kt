package dev.medzik.librepass.android.ui

enum class Screen(data: String? = null) {
    Welcome,
    Register,
    Login,
    Unlock,
    Dashboard("{encryptionKey}"),
    CipherViewScreen("{cipherId}?encryptionKey={encryptionKey}"),
    CipherAdd("{encryptionKey}"),

    ; // <- semicolon is required

    val get = if (data != null) "${name}/${data}" else name

    companion object {
        fun Dashboard(encryptionKey: String) = Dashboard.get.replace("{encryptionKey}", encryptionKey)
        fun CipherViewScreen(cipherId: String, encryptionKey: String) = CipherViewScreen.get.replace("{cipherId}", cipherId).replace("{encryptionKey}", encryptionKey)
        fun CipherAdd(encryptionKey: String) = CipherViewScreen.get.replace("{encryptionKey}", encryptionKey)
    }
}

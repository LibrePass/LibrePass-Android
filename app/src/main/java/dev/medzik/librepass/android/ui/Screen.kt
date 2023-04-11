package dev.medzik.librepass.android.ui

enum class Screen(route: String? = null) {
    Welcome,
    Register,
    Login,
    Unlock,
    Dashboard("dashboard/{encryptionKey}")

    ; // <- semicolon is required

    val get = route ?: name

    companion object {
        fun Dashboard(encryptionKey: String) = Dashboard.get.replace("{encryptionKey}", encryptionKey)
    }
}

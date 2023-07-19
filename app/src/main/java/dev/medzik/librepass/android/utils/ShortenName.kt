package dev.medzik.librepass.android.utils

const val SHORTEN_NAME_LENGTH = 16
const val SHORTEN_USERNAME_LENGTH = 20

fun shortenName(name: String, length: Int): String {
    return if (name.length > length) {
        name.take(length) + "..."
    } else {
        name
    }
}

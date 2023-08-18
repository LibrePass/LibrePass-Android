package dev.medzik.librepass.android.utils

const val SHORTEN_NAME_LENGTH = 16
const val SHORTEN_USERNAME_LENGTH = 20

/**
 * Returns the string shortened to the given length.
 * @param length the length of characters to shorten
 * @return the string shortened
 */
fun String.shorten(length: Int) = if (this.length > length) take(length) + "..." else this

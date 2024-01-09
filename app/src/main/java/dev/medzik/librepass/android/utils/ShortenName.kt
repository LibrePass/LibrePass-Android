package dev.medzik.librepass.android.utils

const val SHORTEN_NAME_LENGTH = 16
const val SHORTEN_USERNAME_LENGTH = 20

/**
 * Returns a string shortened to the specified length.
 *
 * @param length Length of characters to which it will be shortened.
 * @return The shortened string.
 */
fun String.shorten(length: Int) = if (this.length > length) take(length) + "..." else this

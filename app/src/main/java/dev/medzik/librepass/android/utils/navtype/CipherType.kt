package dev.medzik.librepass.android.utils.navtype

import android.os.Bundle
import androidx.navigation.NavType
import dev.medzik.librepass.types.cipher.CipherType

val CipherTypeType = object : NavType<CipherType>(
    isNullableAllowed = false
) {
    override fun get(bundle: Bundle, key: String): CipherType {
        val ordinal = bundle.getInt(key)
        return CipherType.from(ordinal)
    }

    override fun parseValue(value: String): CipherType {
        return CipherType.from(value.toInt())
    }

    override fun serializeAsValue(value: CipherType): String {
        return value.ordinal.toString()
    }

    override fun put(bundle: Bundle, key: String, value: CipherType) {
        bundle.putInt(key, value.ordinal)
    }
}

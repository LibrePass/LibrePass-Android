package dev.medzik.librepass.android.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

sealed class StoreKey<T>(
    val preferenceKey: Preferences.Key<T>,
    val default: T
) {
    data object DynamicColor : StoreKey<Boolean>(
        booleanPreferencesKey("dynamic_color"),
        true
    )

    data object Theme : StoreKey<Int>(
        intPreferencesKey("theme"),
        ThemeValues.SYSTEM.ordinal
    )

    data object PasswordLength : StoreKey<Int>(
        intPreferencesKey("password_length"),
        15
    )

    data object PasswordCapitalize : StoreKey<Boolean>(
        booleanPreferencesKey("password_capitalize"),
        true
    )

    data object PasswordIncludeNumbers : StoreKey<Boolean>(
        booleanPreferencesKey("password_include_numbers"),
        true
    )

    data object PasswordIncludeSymbols : StoreKey<Boolean>(
        booleanPreferencesKey("password_include_symbols"),
        true
    )

    data object VaultTimeout : StoreKey<Int>(
        intPreferencesKey("vault_timeout"),
        VaultTimeoutValues.FIVE_MINUTES.seconds
    )

    data object VaultExpiresAt : StoreKey<Long>(
        longPreferencesKey("vault_expires_at"),
        System.currentTimeMillis()
    )

    data object CustomServers : StoreKey<Set<String>>(
        stringSetPreferencesKey("custom_servers"),
        emptySet()
    )
}

enum class ThemeValues {
    SYSTEM,
    LIGHT,
    DARK
}

enum class VaultTimeoutValues(val seconds: Int) {
    INSTANT(0),
    ONE_MINUTE(1 * 60),
    FIVE_MINUTES(5 * 60),
    FIFTEEN_MINUTES(15 * 60),
    THIRTY_MINUTES(30 * 60),
    ONE_HOUR(1 * 60 * 60),
    NEVER(-1);

    companion object {
        fun fromSeconds(seconds: Int): VaultTimeoutValues {
            for (value in values())
                if (value.seconds == seconds)
                    return value

            throw IllegalArgumentException("No matching VaultTimeoutValues for seconds: $seconds")
        }
    }
}

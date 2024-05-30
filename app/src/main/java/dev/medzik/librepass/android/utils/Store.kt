package dev.medzik.librepass.android.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

sealed class StoreKey<T>(
    val preferenceKey: Preferences.Key<T>,
    val default: T
) {
    data object AppVersionCode : StoreKey<Int>(
        intPreferencesKey("app_version_code"),
        -1
    )
}

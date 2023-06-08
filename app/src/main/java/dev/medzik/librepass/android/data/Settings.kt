package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Settings(
    // id is always 0, so there is only one row in the table
    @PrimaryKey
    val id: Int = 0,

    val dynamicColor: Boolean = true,
    val theme: Int = 0,

    // password generator preferences
    val passwordLength: Int = 15,
    val passwordCapitalize: Boolean = true,
    val passwordIncludeNumbers: Boolean = true,
    val passwordIncludeSymbols: Boolean = true
)

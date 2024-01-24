package dev.medzik.android.autofill

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
enum class FieldType {
    Text,
    Phone,
    Email,
    Username,
    Password,
    Unknown
}

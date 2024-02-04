package dev.medzik.android.autofill.entities

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillItem(
    val id: UUID,
    val name: String,
    val username: String?,
    val password: String?
)

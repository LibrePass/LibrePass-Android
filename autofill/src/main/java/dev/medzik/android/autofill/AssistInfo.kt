package dev.medzik.android.autofill

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
data class AssistInfo(
    val fields: List<AssistField>,
    val url: String?
)

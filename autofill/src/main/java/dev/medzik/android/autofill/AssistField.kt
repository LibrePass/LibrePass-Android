package dev.medzik.android.autofill

import android.os.Build
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
data class AssistField(
    val id: AutofillId,
    val value: AutofillValue?,
    val type: FieldType
)

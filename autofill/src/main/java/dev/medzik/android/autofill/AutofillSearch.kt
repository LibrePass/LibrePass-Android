package dev.medzik.android.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import dev.medzik.android.autofill.entities.AssistInfo
import dev.medzik.android.autofill.entities.AutofillItem
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
interface AutofillSearch {
    fun search(assistInfo: AssistInfo): List<AutofillItem>

    fun fill(
        id: UUID,
        assistInfo: AssistInfo
    )
}

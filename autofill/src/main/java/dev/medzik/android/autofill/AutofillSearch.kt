package dev.medzik.android.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import dev.medzik.android.autofill.entities.AssistInfo
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillSearchResponse(
    val name: String,
    val username: String?
)

@RequiresApi(Build.VERSION_CODES.O)
interface AutofillSearch {
    fun search(assistInfo: AssistInfo): List<AutofillSearchResponse>

    fun fill(
        id: UUID,
        assistInfo: AssistInfo
    )
}

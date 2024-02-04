package dev.medzik.android.autofill.manager

import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.medzik.android.autofill.AutofillSearch
import dev.medzik.android.autofill.entities.AssistInfo
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val autofillSearch: AutofillSearch
    ) {
        suspend fun createMenuPresentationDataset(assistInfo: AssistInfo): List<Dataset> {
            val itemSuggestions = autofillSearch.search(assistInfo)

            return emptyList()
        }
    }

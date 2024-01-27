package dev.medzik.android.autofill.manager

import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.medzik.android.autofill.entities.AssistInfo
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillManager
    @Inject
    constructor(
        @ApplicationContext val context: Context
    ) {
        suspend fun createMenuPresentationDataset(assistInfo: AssistInfo): List<Dataset> {
        }
    }

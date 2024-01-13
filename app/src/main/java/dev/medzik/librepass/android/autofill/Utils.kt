package dev.medzik.librepass.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.FillContext
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
object Utils {
    fun getWindowNodes(fillContexts: List<FillContext>): List<AssistStructure.WindowNode> {
        val fillContext =
            fillContexts
                .lastOrNull { !it.structure.activityComponent.className.contains("PopupWindow") }
                ?: return emptyList()

        val structure = fillContext.structure

        return if (structure.windowNodeCount > 0) {
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) }
        } else {
            emptyList()
        }
    }
}

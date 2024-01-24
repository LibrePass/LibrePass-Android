package dev.medzik.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
object AutofillUtils {
    private const val TAG = "AutofillUtils"

    fun getAssistInfo(
        request: FillRequest,
        windowNode: AssistStructure.WindowNode,
    ): AssistInfo? {
        val assistInfo = NodeParser().parse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) {
            Log.d(TAG, "No fields found")
            return null
        }
        Log.d(TAG, "Found fields: ${assistInfo.fields.map { it.type }.joinToString()}")

        return assistInfo
    }

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

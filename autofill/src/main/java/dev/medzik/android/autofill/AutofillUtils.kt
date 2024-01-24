package dev.medzik.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.FillContext
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
object AutofillUtils {
    private const val TAG = "AutofillUtils"

    fun getAssistInfo(windowNode: AssistStructure.WindowNode): AssistInfo? {
        val assistInfo = NodeParser().parse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) {
            Log.d(TAG, "No fields found")
            return null
        }

        // if all fields types are Text, ignore them
        if (assistInfo.fields.all { it.type == FieldType.Text }) {
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

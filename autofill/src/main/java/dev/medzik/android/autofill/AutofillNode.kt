package dev.medzik.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.EditText
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillNode(
    val id: AutofillId?,
    val value: AutofillValue?,
    val className: String?,
    val autofillHints: List<String>,
    val htmlAttributes: List<Pair<String, String>>,
    val inputType: Int,
    val children: List<AutofillNode>,
    val url: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun AssistStructure.ViewNode.toAutofillNode(): AutofillNode {
    return AutofillNode(
        id = autofillId,
        value = autofillValue,
        className = className,
        autofillHints = autofillHints?.toList()?.filter { it.isNullOrBlank() }.orEmpty(),
        htmlAttributes =
            (htmlInfo?.attributes?.toList() ?: emptyList())
                .filter { it.first != null && it.second != null }
                .filter { it.first.isNotBlank() && it.second.isNotBlank() }
                .map { it.first.lowercase() to it.second.lowercase() },
        inputType = inputType,
        children = (0 until childCount).map { getChildAt(it).toAutofillNode() },
        url = getUrl()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun AutofillNode.isEditText() = className == EditText::class.java.name

@RequiresApi(Build.VERSION_CODES.O)
private fun AssistStructure.ViewNode.getUrl(): String? {
    val domain = webDomain
    if (domain.isNullOrBlank()) return null

    val scheme =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webScheme ?: "https"
        } else {
            "https"
        }

    return "$scheme://$domain"
}

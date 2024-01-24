package dev.medzik.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class NodeParser {
    private var url: String? = null
    private val assistFields = mutableListOf<AssistField>()

    fun parse(node: AssistStructure.ViewNode) = parse(node.toAutofillNode())

    private fun parse(node: AutofillNode): AssistInfo {
        parseViewNode(node)

        return AssistInfo(
            fields = assistFields,
            url = url
        )
    }

    private fun parseViewNode(node: AutofillNode) {
        if (url.isNullOrBlank()) {
            url = node.url
        }

        if (node.children.isEmpty()) {
            val assistField = getAssistField(node)
            if (assistField != null) {
                assistFields.add(assistField)
            }
        } else {
            // parse children nodes
            node.children.forEach {
                parseViewNode(it)
            }
        }
    }

    private fun getAssistField(node: AutofillNode): AssistField? {
        val fieldType = nodeSupportsAutofill(node) ?: return null

        return AssistField(
            id = node.id!!,
            value = node.value,
            type = fieldType
        )
    }

    private fun nodeSupportsAutofill(node: AutofillNode): FieldType? {
        if (!node.isEditText()) {
            // TODO: DELETE
            Log.d(TAG, "Discarding node because it is not edit text")
            Log.d(TAG, "id -> ${node.id}")
            Log.d(TAG, "className -> ${node.className}")
            Log.d(TAG, "url -> ${node.url}")

            return null
        }

        if (node.id == null) {
            Log.d(TAG, "Discarding node because its id is null")
            return null
        }

        return when (val fieldType = detectFieldType(node)) {
            FieldType.Unknown -> null
            else -> fieldType
        }
    }

    private fun detectFieldType(node: AutofillNode): FieldType =
        detectFieldType(
            autofillHints = node.autofillHints.toSet(),
            htmlAttributes = node.htmlAttributes,
            inputType = node.inputType
        )

    private fun detectFieldType(
        autofillHints: Set<String>,
        htmlAttributes: List<Pair<String, String>>,
        inputType: Int
    ): FieldType {
        // detect field using autofill hints
        var fieldType = detectFieldTypeUsingAutofillHints(autofillHints)

        // if not detected, detect using html attributes if they exist
        if (fieldType == FieldType.Unknown && htmlAttributes.isNotEmpty()) {
            fieldType = detectFieldTypeUsingHtmlAttributes(htmlAttributes)
        }

        // if not detected, detect using an android input type
        if (fieldType == FieldType.Unknown) {
            fieldType = detectFieldUsingAndroidInputType(inputType)
        }

        return fieldType
    }

    private fun detectFieldTypeUsingAutofillHints(hints: Set<String>): FieldType {
        hints.forEach {
            when (it) {
                View.AUTOFILL_HINT_PHONE -> return FieldType.Phone
                View.AUTOFILL_HINT_USERNAME -> return FieldType.Username
                View.AUTOFILL_HINT_EMAIL_ADDRESS -> return FieldType.Email
                View.AUTOFILL_HINT_PASSWORD -> return FieldType.Password
            }

            when (sanitizeHint(it)) {
                "email" -> return FieldType.Email
            }
        }

        return FieldType.Unknown
    }

    private fun detectFieldTypeUsingHtmlAttributes(attributes: List<Pair<String, String>>): FieldType {
        val typeAttribute = attributes.firstOrNull { it.first == "type" }
        when (typeAttribute?.second?.lowercase(Locale.ENGLISH)) {
            "tel" -> return FieldType.Phone
            "email" -> return FieldType.Email
            // sometimes the email address field is not marked correctly,
            // guess it is before password field
            "text" -> return FieldType.Text
            "password" -> return FieldType.Password
        }

        return FieldType.Unknown
    }

    private fun sanitizeHint(hint: String): String =
        hint.lowercase(Locale.ENGLISH)
            .replace("-", "")
            .replace(" ", "")

    private fun detectFieldUsingAndroidInputType(inputType: Int): FieldType {
        when {
            androidInputIsVariationType(
                inputType,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            ) -> return FieldType.Email

            androidInputIsVariationType(
                inputType,
                InputType.TYPE_TEXT_VARIATION_NORMAL,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
                InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
            ) -> return FieldType.Text

            androidInputIsVariationType(
                inputType,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            ) -> FieldType.Password
        }

        return FieldType.Unknown
    }

    private fun androidInputIsVariationType(
        inputType: Int,
        vararg type: Int
    ): Boolean {
        type.forEach {
            if (inputType and InputType.TYPE_MASK_VARIATION == it) {
                return true
            }
        }

        return false
    }

    companion object {
        private const val TAG = "NodeExtractor"
    }
}

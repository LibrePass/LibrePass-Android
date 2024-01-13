package dev.medzik.librepass.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class StructureParser(private val structure: AssistStructure) {
    companion object {
        private val TAG = StructureParser::class.java.name
    }

    private var result = AutofillResult()

    fun parse(): AutofillResult? {
        result = AutofillResult()

        mainLoop@ for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val applicationId = windowNode.title.toString().split("/")[0]
            Log.d(TAG, "Autofill applicationId: $applicationId")

            if (parseViewNode(windowNode.rootViewNode))
                break@mainLoop
        }

        return if (result.usernameId != null)
            result
        else
            null
    }

    private fun parseViewNode(node: AssistStructure.ViewNode): Boolean {
        var webDomain: String? = null
//        var webScheme: String? = null

        var returnValue = false

        node.webDomain?.let {
            webDomain = it
            Log.d(TAG, "Autofill domain: $webDomain")
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            node.webDomain?.let {
//                webScheme = it
//                Log.d(TAG, "Autofill scheme: $webScheme")
//            }
//        }

        if (node.visibility == View.VISIBLE) {
            if (node.autofillId != null) {
                val hints = node.autofillId
                if (hints != null) {
                    if (parseNodeByAutofillHint(node))
                        returnValue = true
                } else if (parseNodeByHtml(node)) {
                    returnValue = true
                }

                // TODO: else if (parseNodeByAndroidInput)
            }

            if (webDomain?.isNotEmpty() == true && returnValue)
                return true

            // process each node
            for (i in 0 until node.childCount) {
                if (parseViewNode(node.getChildAt(i)))
                    returnValue = true
                if (webDomain?.isNotEmpty() == true && returnValue)
                    return true
            }
        }

        return returnValue
    }

    private fun parseNodeByAutofillHint(node: AssistStructure.ViewNode): Boolean {
        node.autofillHints?.forEach {
            when {
                it.contains(View.AUTOFILL_HINT_USERNAME, true) ||
                    it.contains(View.AUTOFILL_HINT_EMAIL_ADDRESS, true) ||
                    it.contains("email", true) ||
                    it.contains("e-mail", true) ||
                    it.contains(View.AUTOFILL_HINT_PHONE, true) -> {
                    result.usernameId = node.autofillId
                    result.usernameValue = node.autofillValue
                }

                it.contains(View.AUTOFILL_HINT_PASSWORD, true) -> {
                    result.passwordId = node.autofillId
                    result.passwordValue = node.autofillValue

                    return true
                }

                // ignore autocomplete="off"
                // https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion
                it.equals("off", true) -> {
                    return parseNodeByHtml(node)
                }

                else -> Log.d(TAG, "Unsupported autofill hint: $it")
            }
        }

        return false
    }

    private fun parseNodeByHtml(node: AssistStructure.ViewNode): Boolean {
        when (node.htmlInfo?.tag?.lowercase(Locale.ENGLISH)) {
            "input" -> {
                node.htmlInfo?.attributes?.forEach { attributePair ->
                    when (attributePair.first.lowercase(Locale.ENGLISH)) {
                        "tel", "email" -> {
                            result.usernameId = node.autofillId
                            result.usernameValue = node.autofillValue
                        }

                        // sometimes the email address field is not marked correctly,
                        // guess it is before password field
                        "text" -> {
                            if (result.usernameId == null && result.passwordId == null) {
                                result.usernameId = node.autofillId
                                result.usernameValue = node.autofillValue
                            }
                        }

                        "password" -> {
                            result.passwordId = node.autofillId
                            result.passwordValue = node.autofillValue

                            return true
                        }
                    }
                }
            }
        }

        return false
    }

//    private fun parseByAndroidInput(node: AssistStructure.ViewNode): Boolean {
//        var usernameIdCandidate: AutofillId? = null
//        var usernameValueCandidate: AutofillValue? = null
//
//        when (node.inputType and InputType.TYPE_MASK_CLASS) {
//            InputType.TYPE_CLASS_TEXT -> {
//                when {
//                    androidInputIsVariationType(
//                        node.inputType,
//                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
//                        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
//                    ) -> {
//                        usernameIdCandidate = node.autofillId
//                        usernameValueCandidate = node.autofillValue
//                    }
//
//                    androidInputIsVariationType(
//                        node.inputType,
//                        InputType.TYPE_TEXT_VARIATION_NORMAL,
//                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
//                        InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
//                    ) -> {
//                        usernameIdCandidate = node.autofillId
//                        usernameValueCandidate = node.autofillValue
//                    }
//                }
//            }
//        }
//    }
//
//    private fun androidInputIsVariationType(
//        inputType: Int,
//        vararg type: Int
//    ): Boolean {
//        type.forEach {
//            if (inputType and InputType.TYPE_MASK_VARIATION == it) {
//                return true
//            }
//        }
//
//        return false
//    }

    data class AutofillResult(
        var usernameId: AutofillId? = null,
        var usernameValue: AutofillValue? = null,
        var passwordId: AutofillId? = null,
        var passwordValue: AutofillValue? = null
    ) {
        fun getAutofillIDs(): Array<AutofillId> {
            val all = ArrayList<AutofillId>()

            usernameId?.let { all.add(it) }
            passwordId?.let { all.add(it) }

            return all.toTypedArray()
        }
    }
}

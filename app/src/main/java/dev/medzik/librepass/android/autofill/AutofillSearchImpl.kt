package dev.medzik.librepass.android.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import dev.medzik.android.autofill.AutofillSearch
import dev.medzik.android.autofill.AutofillSearchResponse
import dev.medzik.android.autofill.entities.AssistInfo
import dev.medzik.android.autofill.entities.AutofillItem
import dev.medzik.android.autofill.entities.FieldType
import dev.medzik.librepass.android.utils.Vault
import dev.medzik.librepass.types.cipher.CipherType
import java.util.UUID
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillSearchImpl : AutofillSearch {
    @Inject
    lateinit var vault: Vault

    override fun search(assistInfo: AssistInfo): List<AutofillItem> {
        if (assistInfo.url == null) {
            return emptyList()
        }

        // find ciphers that match the provided url address
        val ciphers = vault.filterByURI(assistInfo.url!!).filter { it.type == CipherType.Login }
        if (ciphers.isEmpty()) {
            return emptyList()
        }

        val searchResponse = mutableListOf<AutofillItem>()

        ciphers.forEach {
            searchResponse.add(
                AutofillItem(
                    id = it.id,
                    name = it.loginData!!.name,
                    username = it.loginData!!.username,
                    password = it.loginData!!.password
                )
            )
        }

        return searchResponse
    }

    override fun fill(
        id: UUID,
        assistInfo: AssistInfo
    ) {
        // get username field (first field with an email, username or phone)
        var usernameField =
            assistInfo.fields.firstOrNull {
                it.type == FieldType.Email ||
                    it.type == FieldType.Username ||
                    it.type == FieldType.Phone
            }
        // if username field does not exist, get text field as username
        // sometimes username/email field is not marked correctly
        if (usernameField == null) {
            usernameField = assistInfo.fields.firstOrNull { it.type == FieldType.Text }
        }

        val passwordField = assistInfo.fields.firstOrNull { it.type == FieldType.Password }

        // TODO
    }
}

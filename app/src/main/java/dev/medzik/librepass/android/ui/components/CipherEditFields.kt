package dev.medzik.librepass.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bastiaanjansen.otp.TOTPGenerator
import com.google.gson.Gson
import dev.medzik.android.components.SecondaryText
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutable
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import java.net.URI

@Composable
fun CipherEditFieldsLogin(
    navController: NavController,
    cipher: Cipher,
    button: @Composable (cipher: Cipher) -> Unit
) {
    var cipherData by rememberMutable(cipher.loginData!!)

    // observe username and password from navController
    // used to get password from password generator
    navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("password")?.observeForever {
            cipherData = cipherData.copy(password = it)
        }
    // observe for cipher from backstack
    navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("cipher")?.observeForever {
            val currentPassword = cipherData.password

            val newCipherData = Gson().fromJson(it, CipherLoginData::class.java)
            cipherData = newCipherData.copy(password = currentPassword)
        }

    TextInputFieldBase(
        label = stringResource(R.string.Name),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.name,
        onValueChange = { cipherData = cipherData.copy(name = it) }
    )

    SecondaryText(
        stringResource(R.string.LoginDetails),
        modifier = Modifier.padding(top = 8.dp)
    )

    TextInputFieldBase(
        label = stringResource(R.string.Username),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.username,
        onValueChange = { cipherData = cipherData.copy(username = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.Password),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.password,
        onValueChange = { cipherData = cipherData.copy(password = it) },
        hidden = true,
        trailingIcon = {
            IconButton(onClick = {
                // save cipher data as json to navController
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "cipher",
                    Gson().toJson(cipherData)
                )

                navController.navigate(Screen.PasswordGenerator)
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
        }
    )

    SecondaryText(
        stringResource(R.string.WebsiteDetails),
        modifier = Modifier.padding(top = 8.dp)
    )

    // show field for each uri
    cipherData.uris?.forEachIndexed { index, uri ->
        TextInputFieldBase(
            label = stringResource(R.string.WebsiteAddress) + " ${index + 1}",
            modifier = Modifier.fillMaxWidth(),
            value = uri,
            onValueChange = {
                cipherData =
                    cipherData.copy(
                        uris =
                            cipherData.uris.orEmpty().toMutableList().apply {
                                this[index] = it
                            }
                    )
            },
            trailingIcon = {
                IconButton(onClick = {
                    cipherData =
                        cipherData.copy(
                            uris =
                                cipherData.uris.orEmpty().toMutableList().apply {
                                    this.removeAt(index)
                                }
                        )
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }
        )
    }

    // button for adding more fields
    Button(
        onClick = {
            cipherData =
                cipherData.copy(
                    uris = cipherData.uris.orEmpty() + ""
                )
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp)
                .padding(top = 8.dp)
    ) {
        Text(stringResource(R.string.AddField))
    }

    SecondaryText(
        stringResource(R.string.TwoFactorAuthorization),
        modifier = Modifier.padding(top = 8.dp)
    )

    val checkOtp =
        !cipher.loginData?.twoFactor.isNullOrEmpty() &&
            runCatching {
                TOTPGenerator.fromURI(URI(cipher.loginData?.twoFactor)).now()
            }.isFailure

    TextInputFieldBase(
        label = stringResource(R.string.AuthenticationKey),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.twoFactor,
        isError = checkOtp,
        onValueChange = { cipherData = cipherData.copy(twoFactor = it) }
    )

    if (checkOtp) {
        Text(
            text = stringResource(R.string.UriIsInvalid),
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier =
                Modifier
                    .padding(horizontal = 6.dp),
        )
    }

    SecondaryText(
        stringResource(R.string.OtherDetails),
        modifier = Modifier.padding(top = 8.dp)
    )

    TextInputFieldBase(
        label = stringResource(R.string.Notes),
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        value = cipherData.notes,
        onValueChange = { cipherData = cipherData.copy(notes = it) }
    )

    button(cipher.copy(loginData = cipherData))
}

@Composable
fun CipherEditFieldsSecureNote(
    cipher: Cipher,
    button: @Composable (cipher: Cipher) -> Unit
) {
    var cipherData by rememberMutable(cipher.secureNoteData!!)

    TextInputFieldBase(
        label = stringResource(R.string.Title),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.title,
        onValueChange = { cipherData = cipherData.copy(title = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.Notes),
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        value = cipherData.note,
        onValueChange = { cipherData = cipherData.copy(note = it) }
    )

    button(cipher.copy(secureNoteData = cipherData))
}

@Composable
fun CipherEditFieldsCard(
    cipher: Cipher,
    button: @Composable (cipher: Cipher) -> Unit
) {
    var cipherData by rememberMutable(cipher.cardData!!)

    TextInputFieldBase(
        label = stringResource(R.string.Name),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.name,
        onValueChange = { cipherData = cipherData.copy(name = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.CardholderName),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.cardholderName,
        onValueChange = { cipherData = cipherData.copy(cardholderName = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.CardNumber),
        keyboardType = KeyboardType.Number,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.number,
        onValueChange = {
            if (!it.all { char -> char.isDigit() })
                return@TextInputFieldBase

            cipherData = cipherData.copy(number = it)
        }
    )

    TextInputFieldBase(
        label = stringResource(R.string.ExpirationMonth),
        keyboardType = KeyboardType.Number,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.expMonth,
        onValueChange = {
            if (it.isEmpty()) {
                cipherData = cipherData.copy(expMonth = null)
                return@TextInputFieldBase
            }

            if (!it.all { char -> char.isDigit() } || it.length > 2 || it.toInt() > 12)
                return@TextInputFieldBase

            cipherData = cipherData.copy(expMonth = it)
        }
    )

    TextInputFieldBase(
        label = stringResource(R.string.ExpirationYear),
        keyboardType = KeyboardType.Number,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.expYear,
        onValueChange = {
            if (it.isEmpty()) {
                cipherData = cipherData.copy(expYear = null)
                return@TextInputFieldBase
            }
            if (!it.all { char -> char.isDigit() } || it.length > 4)
                return@TextInputFieldBase

            cipherData = cipherData.copy(expYear = it)
        }
    )

    TextInputFieldBase(
        label = stringResource(R.string.SecurityCode),
        keyboardType = KeyboardType.Number,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        value = cipherData.code,
        onValueChange = {
            if (!it.all { char -> char.isDigit() })
                return@TextInputFieldBase

            cipherData = cipherData.copy(code = it)
        }
    )

    SecondaryText(
        stringResource(R.string.OtherDetails),
        modifier = Modifier.padding(top = 8.dp)
    )

    TextInputFieldBase(
        label = stringResource(R.string.Notes),
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        value = cipherData.notes,
        onValueChange = { cipherData = cipherData.copy(notes = it) }
    )

    button(cipher.copy(cardData = cipherData))
}

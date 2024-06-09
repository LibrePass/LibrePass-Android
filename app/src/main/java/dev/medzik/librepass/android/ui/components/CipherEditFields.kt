package dev.medzik.librepass.android.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import dev.medzik.android.components.TextFieldValue
import dev.medzik.android.components.colorizePasswordTransformation
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.components.ui.BaseBottomSheet
import dev.medzik.android.components.ui.GroupTitle
import dev.medzik.android.components.ui.SwitcherPreference
import dev.medzik.android.components.ui.rememberBottomSheetState
import dev.medzik.android.components.ui.textfield.AnimatedTextField
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.database.datastore.PasswordGeneratorPreference
import dev.medzik.librepass.android.database.datastore.readPasswordGeneratorPreference
import dev.medzik.librepass.android.database.datastore.writePasswordGeneratorPreference
import dev.medzik.librepass.android.ui.screens.vault.OtpConfigure
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import kotlinx.coroutines.launch
import java.util.Random

enum class PasswordType(val literals: String) {
    NUMERIC("1234567890"),
    LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
    UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    SYMBOLS("!@#\$%^&*()_+-=[]{}\\|;:'\",.<>/?")
}

@Composable
fun CipherEditFieldsLogin(
    navController: NavController,
    cipher: Cipher,
    button: @Composable (cipher: Cipher) -> Unit
) {
    val scope = rememberCoroutineScope()

    var cipherData by rememberMutable(cipher.loginData!!)

    // observe otp uri from navController
    // used to get otp uri from otp configuration screen
    navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("otpUri")?.observeForever {
            Log.d("OTP_OBSERVABLE", "Received URI: $it")
            cipherData = cipherData.copy(twoFactor = it)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.name,
        onValueChange = { cipherData = cipherData.copy(name = it) }
    )

    GroupTitle(
        stringResource(R.string.LoginDetails),
        modifier = Modifier.padding(top = 8.dp)
    )

    TextInputFieldBase(
        label = stringResource(R.string.Email),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.email,
        onValueChange = { cipherData = cipherData.copy(email = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.Username),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.username,
        onValueChange = { cipherData = cipherData.copy(username = it) }
    )

    val passwordGeneratorSheetState = rememberBottomSheetState()

    TextInputFieldBase(
        label = stringResource(R.string.Password),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.password,
        onValueChange = { cipherData = cipherData.copy(password = it) },
        hidden = true
    ) {
        IconButton(
            onClick = { passwordGeneratorSheetState.show() }
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null
            )
        }
    }

    @Composable
    fun PasswordGeneratorSheetContent(onSubmit: (String) -> Unit) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current

        var generatedPassword by rememberMutableString()

        var passwordGeneratorPreference by rememberMutable(PasswordGeneratorPreference())
        LaunchedEffect(Unit) {
            passwordGeneratorPreference = readPasswordGeneratorPreference(context)
        }

        fun generatePassword(): String {
            var letters = PasswordType.LOWERCASE.literals

            if (passwordGeneratorPreference.capitalize) {
                letters += PasswordType.UPPERCASE.literals
            }

            if (passwordGeneratorPreference.includeNumbers) {
                letters += PasswordType.NUMERIC.literals
            }

            if (passwordGeneratorPreference.includeSymbols) {
                letters += PasswordType.SYMBOLS.literals
            }

            return (1..passwordGeneratorPreference.length)
                .map { Random().nextInt(letters.length) }
                .map(letters::get)
                .joinToString("")
        }

        // regenerate on options change
        LaunchedEffect(passwordGeneratorPreference) {
            generatedPassword = generatePassword()
        }

        AnimatedTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = TextFieldValue(
                value = generatedPassword,
//                    editable = false
            ),
            readOnly = true,
            visualTransformation = colorizePasswordTransformation(),
            trailing = {
                Row {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(generatedPassword)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = { generatedPassword = generatePassword() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null
                        )
                    }
                }
            }
        )

        Spacer(
            modifier = Modifier.padding(top = 8.dp)
        )

        AnimatedTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = TextFieldValue(
                value = passwordGeneratorPreference.length.toString(),
                onChange = {
                    try {
                        if (it.length in 1..3 && it.toInt() <= 256) {
                            passwordGeneratorPreference = passwordGeneratorPreference.copy(length = it.toInt())
                            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                        }
                    } catch (e: NumberFormatException) {
                        // ignore, just do not update input value
                    }
                }
            ),
            label = stringResource(R.string.PasswordGenerator_Length),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailing = {
                IconButton(
                    onClick = {
                        if (passwordGeneratorPreference.length > 1) {
                            passwordGeneratorPreference = passwordGeneratorPreference.copy(
                                length = passwordGeneratorPreference.length - 1
                            )
                            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null
                    )
                }

                IconButton(
                    onClick = {
                        if (passwordGeneratorPreference.length < 256) {
                            passwordGeneratorPreference = passwordGeneratorPreference.copy(
                                length = passwordGeneratorPreference.length + 1
                            )
                            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        )

        // Capital letters switch
        SwitcherPreference(
            title = stringResource(R.string.PasswordGenerator_CapitalLetters),
            checked = passwordGeneratorPreference.capitalize,
            onCheckedChange = {
                passwordGeneratorPreference = passwordGeneratorPreference.copy(capitalize = it)
                runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null
                )
            }
        )

        // Numeric switch
        SwitcherPreference(
            title = stringResource(R.string.PasswordGenerator_Numbers),
            checked = passwordGeneratorPreference.includeNumbers,
            onCheckedChange = {
                passwordGeneratorPreference = passwordGeneratorPreference.copy(includeNumbers = it)
                runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Numbers,
                    contentDescription = null
                )
            }
        )

        // Symbols switch
        SwitcherPreference(
            title = stringResource(R.string.PasswordGenerator_Symbols),
            checked = passwordGeneratorPreference.includeSymbols,
            onCheckedChange = {
                passwordGeneratorPreference = passwordGeneratorPreference.copy(includeSymbols = it)
                runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.EuroSymbol,
                    contentDescription = null
                )
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 90.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            onClick = { onSubmit(generatedPassword) }
        ) {
            Text(stringResource(R.string.Submit))
        }
    }

    BaseBottomSheet(state = passwordGeneratorSheetState) {
        PasswordGeneratorSheetContent(
            onSubmit = {
                cipherData = cipherData.copy(password = it)

                scope.launch {
                    passwordGeneratorSheetState.hide()
                }
            }
        )
    }

    GroupTitle(
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
                cipherData = cipherData.copy(
                    uris = cipherData.uris.orEmpty().toMutableList().apply {
                        this[index] = it
                    }
                )
            }
        ) {
            IconButton(
                onClick = {
                    cipherData = cipherData.copy(
                        uris = cipherData.uris.orEmpty().toMutableList().apply {
                            this.removeAt(index)
                        }
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
    }

    // button for adding more fields
    Button(
        onClick = {
            cipherData = cipherData.copy(
                uris = cipherData.uris.orEmpty() + ""
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp)
            .padding(top = 8.dp)
    ) {
        Text(stringResource(R.string.AddField))
    }

    GroupTitle(
        stringResource(R.string.TwoFactorAuthentication),
        modifier = Modifier.padding(top = 8.dp)
    )

    Button(
        onClick = {
            navController.navigate(
                OtpConfigure(
                    cipher.id.toString()
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp)
            .padding(top = 8.dp)
    ) {
        Text(stringResource(R.string.ConfigureTwoFactor))
    }

    if (!cipher.loginData?.twoFactor.isNullOrEmpty()) {
        Button(
            onClick = { cipherData = cipherData.copy(twoFactor = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp)
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.DeleteTwoFactor))
        }
    }

    GroupTitle(
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
        modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.name,
        onValueChange = { cipherData = cipherData.copy(name = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.CardholderName),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.cardholderName,
        onValueChange = { cipherData = cipherData.copy(cardholderName = it) }
    )

    TextInputFieldBase(
        label = stringResource(R.string.CardNumber),
        keyboardType = KeyboardType.Number,
        modifier = Modifier
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
        modifier = Modifier
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
        modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = cipherData.code,
        onValueChange = {
            if (!it.all { char -> char.isDigit() })
                return@TextInputFieldBase

            cipherData = cipherData.copy(code = it)
        }
    )

    GroupTitle(
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

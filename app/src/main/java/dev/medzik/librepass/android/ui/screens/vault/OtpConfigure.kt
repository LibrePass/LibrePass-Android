package dev.medzik.librepass.android.ui.screens.vault

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.Permission
import dev.medzik.android.components.icons.TopAppBarBackIcon
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.ui.ComboBoxDropdown
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.QrCodeScanner
import dev.medzik.librepass.android.ui.components.TextInputFieldBase
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.otp.OTPParameters
import dev.medzik.otp.OTPType
import dev.medzik.otp.TOTPGenerator
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class OtpConfigure(val cipherId: String)

@Composable
fun OtpConfigureScreen(
    navController: NavController,
    args: OtpConfigure,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val cipher = remember { viewModel.vault.find(UUID.fromString(args.cipherId)) } ?: return

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.ConfigureTwoFactor),
                navigationIcon = { TopAppBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            var qrScanning by rememberMutable(true)

            if (qrScanning) {
                Text(
                    text = stringResource(R.string.ScanQrCode),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )

                Permission(
                    permission = Manifest.permission.CAMERA,
                    onGranted = {
                        var wasScanned by rememberMutable(false)
                        var isTotpValid by rememberMutable(false)
                        var totpUri by rememberMutable("")

                        QrCodeScanner {
                            if (!wasScanned && !isTotpValid) {
                                wasScanned = true

                                isTotpValid =
                                    runCatching {
                                        TOTPGenerator.fromUrl(it)
                                    }.isSuccess

                                Log.i("TOTP_QR", "Valid: $isTotpValid")

                                totpUri = it

                                if (isTotpValid) {
                                    navController.previousBackStackEntry!!.savedStateHandle["otpUri"] = totpUri
                                    navController.popBackStack()
                                }
                            }
                        }

                        if (wasScanned) {
                            if (!isTotpValid) {
                                Text(
                                    text = stringResource(R.string.Error_InvalidURI),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    },
                    onDenied = { request ->
                        request()

                        Text(
                            text = "No permissions to use camera.",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                )

                Button(
                    onClick = {
                        qrScanning = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.EnterKeyManually))
                }
            } else {
                var beginParams: OTPParameters? = null
                if (cipher.loginData?.twoFactor != null) {
                    beginParams = OTPParameters.parseUrl(cipher.loginData?.twoFactor)
                }

                var totpSecret by rememberMutable(beginParams?.secret?.encoded?.toString() ?: "")

                var digits by rememberMutable(beginParams?.digits?.value?.toString() ?: "6")
                var type by rememberMutable(beginParams?.type ?: OTPType.TOTP)
                var algorithm by rememberMutable(beginParams?.algorithm ?: OTPParameters.Algorithm.SHA1)

                var period by rememberMutable(beginParams?.period?.value?.toString() ?: "30")
                var counter by rememberMutable(beginParams?.counter?.value?.toString() ?: "0")

                TextInputFieldBase(
                    label = stringResource(R.string.TwoFactorSecret),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    value = totpSecret,
                    onValueChange = { totpSecret = it }
                )

                ComboBoxDropdown(
                    values = OTPType.entries.toTypedArray(),
                    value = type,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.Type))
                    },
                    onValueChange = { type = it }
                )

                ComboBoxDropdown(
                    values = OTPParameters.Algorithm.entries.toTypedArray(),
                    value = algorithm,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.Algorithm))
                    },
                    onValueChange = { algorithm = it }
                )

                TextInputFieldBase(
                    label = stringResource(R.string.Digits),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    keyboardType = KeyboardType.Number,
                    value = digits,
                    onValueChange = { digits = it }
                )

                TextInputFieldBase(
                    label = if (type == OTPType.TOTP) stringResource(R.string.Period) else stringResource(R.string.Counter),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    keyboardType = KeyboardType.Number,
                    value = (if (type == OTPType.TOTP) period else counter).toString(),
                    onValueChange = {
                        if (type == OTPType.TOTP)
                            period = it
                        else
                            counter = it
                    }
                )

                var otpUri by rememberMutable("")
                val otpCodeError =
                    runCatching {
                        otpUri = generateOtpUri(totpSecret, type, digits.toInt(), period.toInt(), counter.toLong(), algorithm)
                    }.isSuccess

                Button(
                    onClick = { qrScanning = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.ScanQrCode))
                }

                Button(
                    onClick = {
                        navController.previousBackStackEntry!!.savedStateHandle["otpUri"] = otpUri
                        navController.popBackStack()
                    },
                    enabled = totpSecret.isNotEmpty() && otpCodeError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.Save))
                }
            }
        }
    }
}

private fun generateOtpUri(
    secret: String,
    type: OTPType,
    digits: Int,
    period: Int,
    counter: Long,
    algorithm: OTPParameters.Algorithm
): String {
    val otpBuilder = OTPParameters.builder()
        .type(type)
        .digits(OTPParameters.Digits.valueOf(digits))
        .secret(OTPParameters.Secret(secret))
        .algorithm(algorithm)

    when (type) {
        OTPType.TOTP -> otpBuilder.period(OTPParameters.Period.valueOf(period))
        OTPType.HOTP -> otpBuilder.counter(OTPParameters.Counter(counter))
    }

    return otpBuilder.build().encodeToUrl()
}

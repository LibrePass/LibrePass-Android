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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.rememberMutable
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.Permission
import dev.medzik.librepass.android.ui.components.QrCodeScanner
import dev.medzik.librepass.android.ui.components.TextInputFieldBase
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.otp.OTPParser
import dev.medzik.otp.TOTPGenerator

@Composable
fun TotpConfigure(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    var totpCode by rememberMutable("")

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.TotpConfigure),
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
        ) {
            var qrScanning by rememberMutable(true)

            if (qrScanning) {
                Text(
                    text = stringResource(R.string.ScanQrCode),
                    fontSize = 24.sp,
                    modifier =
                        Modifier
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
                                        TOTPGenerator.now(OTPParser.parse(it))
                                    }.isSuccess

                                Log.i("TOTP_QR", "Valid: $isTotpValid")

                                totpUri = it

                                if (isTotpValid) {
                                    navController.previousBackStackEntry!!.savedStateHandle["totpUri"] = totpUri
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
                                    modifier =
                                        Modifier
                                            .padding(6.dp),
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
                            modifier =
                                Modifier
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp)
                            .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.EnterKeyManually))
                }
            } else {
                TextInputFieldBase(
                    label = stringResource(R.string.TotpKey),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    value = totpCode,
                    onValueChange = { totpCode = it }
                )

                Button(
                    onClick = { qrScanning = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp)
                            .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.ScanQrCode))
                }

                Button(
                    onClick = {
                        // TODO: add building URI from totpCode

                        // remove remember
                        // val totpCodeValue = totpCode
                        // navController.previousBackStackEntry!!.savedStateHandle["totpUri"] = totpCodeValue
                        navController.popBackStack()
                    },
                    modifier =
                        Modifier
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

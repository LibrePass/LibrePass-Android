package dev.medzik.librepass.android.ui.screens.vault

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.DefaultScaffold
import dev.medzik.librepass.android.ui.TopBarWithBack
import dev.medzik.librepass.android.utils.navtype.CipherTypeType
import dev.medzik.librepass.types.cipher.CipherType
import kotlin.reflect.typeOf

fun NavGraphBuilder.vaultNavigation(navController: NavController) {
    composable<Vault> {
        DefaultScaffold(
            topBar = { VaultScreenTopBar(navController) },
            floatingActionButton = { VaultScreenFloatingActionButton(navController) }
        ) {
            VaultScreen(navController)
        }
    }

    composable<CipherView> {
        val args = it.toRoute<CipherView>()

        CipherViewScreen(navController, args)
    }

    composable<CipherAdd>(
        typeMap = mapOf(typeOf<CipherType>() to CipherTypeType)
    ) {
        val args = it.toRoute<CipherAdd>()

        CipherAddScreen(navController, args)
    }

    composable<CipherEdit> {
        val args = it.toRoute<CipherEdit>()

        CipherEditScreen(navController, args)
    }

    composable<OtpConfigure> {
        val args = it.toRoute<OtpConfigure>()

        OtpConfigureScreen(navController, args)
    }

    composable<PasswordGenerator> {
        DefaultScaffold(
            topBar = { TopBarWithBack(R.string.PasswordGenerator, navController) }
        ) {
            PasswordGeneratorScreen(navController)
        }
    }

    composable<Search> {
        SearchScreen(navController)
    }
}

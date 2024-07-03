package dev.medzik.librepass.android.ui.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable<Signup> {
        SignupScreen(navController)
    }

    composable<Login> {
        LoginScreen(navController)
    }

    composable<ForgotPassword> {
        val args = it.toRoute<ForgotPassword>()
        ForgotPasswordScreen(navController, args)
    }
}

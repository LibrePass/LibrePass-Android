package dev.medzik.librepass.android.ui

enum class Argument {
    EncryptionKey,
    CipherId,

    ;

    val key get() = "{${name.lowercase()}}"
    val get get() = name.lowercase()
}

sealed class Screen(private val route: String, private val arguments: List<String>? = null) {
    object Welcome : Screen("welcome")
    object Register : Screen("register")
    object Login : Screen("login")
    object Unlock : Screen("unlock")
    object Dashboard : Screen("dashboard", listOf(Argument.EncryptionKey.key))
    object CipherView : Screen("cipher-view", listOf(Argument.EncryptionKey.key, Argument.CipherId.key))
    object CipherAdd : Screen("cipher-add", listOf(Argument.EncryptionKey.key))

    val get get() = if (arguments != null) "$route/${arguments.joinToString("/")}" else route

    @Throws(IllegalArgumentException::class)
    fun fill(vararg arguments: Pair<Argument, String>): String {
        if (arguments.size != this.arguments?.size) {
            println("Invalid number of arguments. Expected ${this.arguments?.size}, got ${arguments.size}")
            throw IllegalArgumentException("Invalid number of arguments. Expected ${this.arguments?.size}, got ${arguments.size}")
        }

        var route = this.get

        for (argument in arguments) {
            route = route.replace(argument.first.key, argument.second)
        }

        return route
    }
}

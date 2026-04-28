package com.saffet.wordcrushmobile.ui.navigation

/**
 * Uygulama içi ekranların tip güvenli route tanımları.
 *
 * Argüman alan ekranlar ([Screen.Game]) hem şablon route'u (NavHost tarafı),
 * hem de parametreleri enjekte eden bir yardımcı fabrika ([Screen.Game.createRoute])
 * barındırır.
 *
 * Not: `Screen` süper konstrüktörü çağrılırken `data object`'in kendi
 * üyelerine erişilemez. Bu yüzden route şablonundaki placeholder'lar
 * (`{rows}`, `{cols}`, `{moves}`) literal olarak yazılır ve aynı
 * isimler `ARG_*` sabitlerinde de tekrar edilir — bu sabitler
 * `navArgument(...)` çağrılarında kullanılır.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Username : Screen("username")
    data object Home : Screen("home")
    data object NewGame : Screen("new_game")
    data object Scoreboard : Screen("scoreboard")
    data object Market : Screen("market")

    data object MoveSelection : Screen("move_selection/{rows}/{cols}") {
        const val ARG_ROWS = "rows"
        const val ARG_COLS = "cols"

        fun createRoute(rows: Int, cols: Int): String =
            "move_selection/$rows/$cols"
    }

    data object Game : Screen("game/{rows}/{cols}/{moves}") {
        const val ARG_ROWS = "rows"
        const val ARG_COLS = "cols"
        const val ARG_MOVES = "moves"

        fun createRoute(rows: Int, cols: Int, moves: Int): String =
            "game/$rows/$cols/$moves"
    }
}

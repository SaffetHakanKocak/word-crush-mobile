package com.saffet.wordcrushmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saffet.wordcrushmobile.ui.screens.game.GameScreen
import com.saffet.wordcrushmobile.ui.screens.home.HomeScreen
import com.saffet.wordcrushmobile.ui.screens.market.MarketScreen
import com.saffet.wordcrushmobile.ui.screens.newgame.NewGameScreen
import com.saffet.wordcrushmobile.ui.screens.scoreboard.ScoreboardScreen
import com.saffet.wordcrushmobile.ui.screens.splash.SplashScreen
import com.saffet.wordcrushmobile.ui.screens.username.UsernameScreen

/**
 * Uygulamanın ana NavHost'u. Tüm ekran geçişleri ve argüman aktarımları
 * burada merkezi olarak tanımlanır.
 *
 * Başlangıç akışı:
 * 1. [Screen.Splash] → SplashViewModel DataStore'u okur ve
 *    sonuç route'u yayar. Buradan [Screen.Home] veya [Screen.Username]'e
 *    geçilir; Splash back stack'ten tamamen temizlenir.
 * 2. [Screen.Username] → kayıt başarılıysa [Screen.Home]'a gider.
 * 3. [Screen.NewGame] → seçilen zorluk parametreleri
 *    [Screen.Game.createRoute] ile route'a enjekte edilir.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Username.route) {
            UsernameScreen(
                onSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Username.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNewGame = { navController.navigate(Screen.NewGame.route) },
                onScoreboard = { navController.navigate(Screen.Scoreboard.route) },
                onMarket = { navController.navigate(Screen.Market.route) },
                onChangeUsername = { navController.navigate(Screen.Username.route) }
            )
        }

        composable(Screen.NewGame.route) {
            NewGameScreen(
                onStartGame = { rows, cols, moves ->
                    navController.navigate(Screen.Game.createRoute(rows, cols, moves))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument(Screen.Game.ARG_ROWS) { type = NavType.IntType },
                navArgument(Screen.Game.ARG_COLS) { type = NavType.IntType },
                navArgument(Screen.Game.ARG_MOVES) { type = NavType.IntType }
            )
        ) {
            // Argümanlar GameViewModel tarafından SavedStateHandle üzerinden
            // okunur; ekrana ayrıca iletilmesine gerek yoktur.
            GameScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Scoreboard.route) {
            ScoreboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Market.route) {
            MarketScreen(onBack = { navController.popBackStack() })
        }
    }
}

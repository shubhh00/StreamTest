package com.app.streamtest

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.streamtest.screens.library.LibraryRoot
import com.app.streamtest.screens.log.LogScreen
import com.app.streamtest.screens.player.PlayerRoot

// The three routes of the app (AGENTS.md §4). No nested graphs.
object Routes {
    const val LIBRARY = "library"
    const val LOG = "log"
    const val STREAM_ID_ARG = "streamId"
    const val PLAYER = "player/{$STREAM_ID_ARG}"

    fun player(streamId: String) = "player/$streamId"
}

@Composable
fun AppNavigation() {
    // The NavController keeps the back stack: the list of open screens, in order.
    // The remote's BACK button pops the top screen off automatically.
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryRoot(
                onOpenPlayer = { streamId -> navController.navigate(Routes.player(streamId)) },
                onOpenLog = { navController.navigate(Routes.LOG) { launchSingleTop = true } },
            )
        }
        composable(Routes.LOG) {
            LogScreen(
                onOpenLibrary = { navController.popBackStack(Routes.LIBRARY, inclusive = false) },
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument(Routes.STREAM_ID_ARG) { type = NavType.StringType }),
        ) {
            PlayerRoot()
        }
    }
}

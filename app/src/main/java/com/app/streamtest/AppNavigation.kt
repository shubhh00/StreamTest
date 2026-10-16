package com.app.streamtest

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.streamtest.screens.library.LibraryRoot
import com.app.streamtest.screens.log.LogRoot
import com.app.streamtest.screens.player.PlayerRoot
import com.app.streamtest.screens.settings.SettingsRoot

// The routes of the app. No nested graphs.
object Routes {
    const val LIBRARY = "library"
    const val LOG = "log"
    const val SETTINGS = "settings"
    const val STREAM_ID_ARG = "streamId"
    const val PLAYER = "player/{$STREAM_ID_ARG}"

    fun player(streamId: String) = "player/$streamId"
}

@Composable
fun AppNavigation() {
    // The NavController keeps the back stack: the list of open screens, in order.
    // The remote's BACK button pops the top screen off automatically.
    val navController = rememberNavController()
    val backToLibrary = { navController.popBackStack(Routes.LIBRARY, inclusive = false) }

    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryRoot(
                onOpenPlayer = { streamId -> navController.navigate(Routes.player(streamId)) },
                onOpenLog = { navController.openTab(Routes.LOG) },
                onOpenSettings = { navController.openTab(Routes.SETTINGS) },
            )
        }
        composable(Routes.LOG) {
            LogRoot(
                onOpenLibrary = { backToLibrary() },
                onOpenSettings = { navController.openTab(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsRoot(
                onOpenLibrary = { backToLibrary() },
                onOpenLog = { navController.openTab(Routes.LOG) },
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument(Routes.STREAM_ID_ARG) { type = NavType.StringType }),
        ) {
            PlayerRoot(onClose = { navController.popBackStack() })
        }
    }
}

// Log and Settings always open directly on top of the Library (popUpTo), so switching
// between tabs never piles screens up, and BACK from any tab returns to the Library.
private fun NavController.openTab(route: String) {
    navigate(route) {
        popUpTo(Routes.LIBRARY)
        launchSingleTop = true
    }
}

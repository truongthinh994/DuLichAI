package com.travelai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelai.ui.chat.ChatScreen
import com.travelai.ui.history.HistoryScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TravelAiRoutes.CHAT_ROUTE,
        modifier = modifier
    ) {
        composable(route = TravelAiRoutes.CHAT_ROUTE) {
            ChatScreen(
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.CHAT_ROUTE_WITH_SESSION,
            arguments = listOf(
                navArgument(TravelAiRoutes.SESSION_ID_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ChatScreen(
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.HISTORY_ROUTE) {
            HistoryScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.CHAT_ROUTE)
                    }
                },
                onSessionClick = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId))
                }
            )
        }
    }
}

private object TravelAiRoutes {
    const val CHAT_ROUTE = "chat"
    const val HISTORY_ROUTE = "history"
    const val SESSION_ID_ARG = "sessionId"
    const val CHAT_ROUTE_WITH_SESSION = "$CHAT_ROUTE?$SESSION_ID_ARG={$SESSION_ID_ARG}"

    fun chatRoute(sessionId: Long): String = "$CHAT_ROUTE?$SESSION_ID_ARG=$sessionId"
}

package com.example.warbage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.ui.screens.*

sealed class Screen(val route: String) {
    data object RoleSelect : Screen("role_select")
    data object Home : Screen("home")
    data object MaterialList : Screen("materials")
    data object MaterialDetail : Screen("material_detail/{materialId}") {
        fun createRoute(materialId: Int) = "material_detail/$materialId"
    }
    data object CreateLot : Screen("create_lot/{materialId}/{weight}") {
        fun createRoute(materialId: Int, weight: Double) = "create_lot/$materialId/$weight"
    }
    data object PriceBoard : Screen("price_board")
    data object RecyclerList : Screen("recyclers/{lotId}") {
        fun createRoute(lotId: Int) = "recyclers/$lotId"
    }
    data object RecyclerMap : Screen("recycler_map")
    data object CreateRequest : Screen("create_request/{lotId}/{recyclerId}") {
        fun createRoute(lotId: Int, recyclerId: Int) = "create_request/$lotId/$recyclerId"
    }
    data object TransactionList : Screen("transactions")
    data object AiScanner : Screen("ai_scanner")
    data object RecyclerDashboard : Screen("recycler_dashboard")
    data object RequestDetail : Screen("request_detail/{requestId}") {
        fun createRoute(requestId: Int) = "request_detail/$requestId"
    }
}

@Composable
fun WarbageNavGraph(
    navController: NavHostController,
    database: WarbageDatabase
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelect.route
    ) {
        composable(Screen.RoleSelect.route) {
            RoleSelectScreen(
                onCollectorSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelect.route) { inclusive = true }
                    }
                },
                onRecyclerSelected = {
                    navController.navigate(Screen.RecyclerDashboard.route) {
                        popUpTo(Screen.RoleSelect.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMaterialsClick = { navController.navigate(Screen.MaterialList.route) },
                onPriceBoardClick = { navController.navigate(Screen.PriceBoard.route) },
                onFindRecyclerClick = { navController.navigate(Screen.RecyclerMap.route) },
                onTransactionsClick = { navController.navigate(Screen.TransactionList.route) },
                onAiScannerClick = { navController.navigate(Screen.AiScanner.route) },
                onSwitchRole = {
                    navController.navigate(Screen.RoleSelect.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MaterialList.route) {
            MaterialListScreen(
                database = database,
                onMaterialClick = { materialId ->
                    navController.navigate(Screen.MaterialDetail.createRoute(materialId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MaterialDetail.route,
            arguments = listOf(navArgument("materialId") { type = NavType.IntType })
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getInt("materialId") ?: return@composable
            MaterialDetailScreen(
                database = database,
                materialId = materialId,
                onCreateLot = { matId, weight ->
                    navController.navigate(Screen.CreateLot.createRoute(matId, weight))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateLot.route,
            arguments = listOf(
                navArgument("materialId") { type = NavType.IntType },
                navArgument("weight") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getInt("materialId") ?: return@composable
            val weight = backStackEntry.arguments?.getFloat("weight")?.toDouble() ?: return@composable
            CreateLotScreen(
                database = database,
                materialId = materialId,
                weight = weight,
                onLotCreated = { lotId ->
                    navController.navigate(Screen.RecyclerList.createRoute(lotId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PriceBoard.route) {
            PriceBoardScreen(
                database = database,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RecyclerList.route,
            arguments = listOf(navArgument("lotId") { type = NavType.IntType })
        ) { backStackEntry ->
            val lotId = backStackEntry.arguments?.getInt("lotId") ?: return@composable
            RecyclerListScreen(
                database = database,
                lotId = lotId,
                onRecyclerSelected = { recyclerId ->
                    navController.navigate(Screen.CreateRequest.createRoute(lotId, recyclerId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RecyclerMap.route) {
            RecyclerMapScreen(
                database = database,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateRequest.route,
            arguments = listOf(
                navArgument("lotId") { type = NavType.IntType },
                navArgument("recyclerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val lotId = backStackEntry.arguments?.getInt("lotId") ?: return@composable
            val recyclerId = backStackEntry.arguments?.getInt("recyclerId") ?: return@composable
            CreateRequestScreen(
                database = database,
                lotId = lotId,
                recyclerId = recyclerId,
                onRequestCreated = {
                    navController.navigate(Screen.TransactionList.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TransactionList.route) {
            TransactionListScreen(
                database = database,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AiScanner.route) {
            AiScannerScreen(
                onMaterialDetected = { materialId ->
                    navController.navigate(Screen.MaterialDetail.createRoute(materialId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RecyclerDashboard.route) {
            RecyclerDashboardScreen(
                database = database,
                onRequestClick = { requestId ->
                    navController.navigate(Screen.RequestDetail.createRoute(requestId))
                },
                onSwitchRole = {
                    navController.navigate(Screen.RoleSelect.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.RequestDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.IntType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getInt("requestId") ?: return@composable
            RequestDetailScreen(
                database = database,
                requestId = requestId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

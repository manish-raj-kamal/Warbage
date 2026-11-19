package com.warbage.recycling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.warbage.recycling.navigation.WarbageNavGraph
import com.warbage.recycling.ui.glass.LiquidBackdropHost
import com.warbage.recycling.ui.theme.WarbageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WarbageApplication
        val database = app.database
        val authManager = app.authManager

        setContent {
            WarbageTheme {
                LiquidBackdropHost {
                    val navController = rememberNavController()
                    WarbageNavGraph(
                        navController = navController,
                        database = database,
                        authManager = authManager
                    )
                }
            }
        }
    }
}

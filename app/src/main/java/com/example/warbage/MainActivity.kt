package com.example.warbage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.warbage.navigation.WarbageNavGraph
import com.example.warbage.ui.glass.LiquidBackdropHost
import com.example.warbage.ui.theme.WarbageTheme

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

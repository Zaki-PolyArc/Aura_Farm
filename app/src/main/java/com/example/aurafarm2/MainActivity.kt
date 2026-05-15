package com.example.aurafarm2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aurafarm2.core.theme.*
import com.example.aurafarm2.features.expenses.ExpenseScreen
import com.example.aurafarm2.features.expenses.IncomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainShell()
            }
        }
    }
}

@Composable
fun MainShell() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainerLowest,
                contentColor   = OnSurface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector        = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Expenses"
                        )
                    },
                    label  = { Text("Expenses", style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant,
                        indicatorColor      = PrimaryContainer.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector        = Icons.Outlined.TrendingUp,
                            contentDescription = "Income"
                        )
                    },
                    label  = { Text("Income", style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Secondary,
                        selectedTextColor   = Secondary,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant,
                        indicatorColor      = Secondary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        // innerPadding.calculateBottomPadding() = exact nav bar height
        // Screens use this so their FAB and last list item clear the bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> ExpenseScreen()
                1 -> IncomeScreen()
            }
        }
    }
}
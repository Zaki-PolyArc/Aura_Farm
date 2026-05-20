package com.example.aurafarm2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aurafarm2.core.theme.*
import com.example.aurafarm2.features.expenses.ExpenseScreen
import com.example.aurafarm2.features.expenses.IncomeScreen
import com.example.aurafarm2.features.expenses.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme { MainShell() }
        }
    }
}

// ── Nav item model ─────────────────────────────────────────────

private data class NavItem(
    val icon: ImageVector,
    val label: String
)

private val navItems = listOf(
    NavItem(Icons.Outlined.AccountBalanceWallet, "Expenses"),
    NavItem(Icons.Outlined.Payments,             "Income"),
    NavItem(Icons.Outlined.Settings,             "Settings"),
)

// ── Shell ──────────────────────────────────────────────────────

@Composable
fun MainShell() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            // Custom bottom bar matching the screenshot:
            // icon-only, active tab has a sand-filled rounded square background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLowest)
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) FabBackground
                                    else androidx.compose.ui.graphics.Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { selectedTab = index }) {
                                Icon(
                                    imageVector        = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) FabIcon else OnSurfaceVariant,
                                    modifier           = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> ExpenseScreen()
                1 -> IncomeScreen()
                2 -> SettingsScreen()
            }
        }
    }
}

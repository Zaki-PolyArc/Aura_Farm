package com.example.aurafarm2.features.expenses

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurafarm2.core.theme.*
import kotlinx.coroutines.delay

// ── Dummy data ─────────────────────────────────────────────────

data class IncomeSource(
    val icon: ImageVector,
    val name: String,
    val subtitle: String,
    val amount: Double,
    val percent: Int,
    val color: Color
)

data class IncomeBreakdownItem(
    val label: String,
    val amount: Double,
    val percent: Int,
    val color: Color,
    val fraction: Float
)

private val dummyIncomeSources = listOf(
    IncomeSource(Icons.Outlined.Work,            "Salary",      "Tech Solutions Inc.", 8500.00, 68, SalaryDot),
    IncomeSource(Icons.Outlined.Draw,            "Freelance",   "Design & Consulting", 3000.00, 24, FreelanceDot),
    IncomeSource(Icons.Outlined.AccountBalance,  "Investments", "Dividends & Interest",  950.00,  8, InvestmentDot),
)

private val dummyBreakdown = listOf(
    IncomeBreakdownItem("Salary",      8500.0, 68, SalaryDot,      0.68f),
    IncomeBreakdownItem("Freelance",   3000.0, 24, FreelanceDot,   0.24f),
    IncomeBreakdownItem("Investments",  950.0,  8, InvestmentDot,  0.08f),
)

private const val TOTAL_INCOME  = 12450.00
private const val GROWTH_PERCENT = 8.4

// ── Root screen ────────────────────────────────────────────────

@Composable
fun IncomeScreen(onAddClick: () -> Unit = {}) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedIncomeSection(visible, 0)   { IncomeTopBar() }

            Spacer(Modifier.height(24.dp))

            AnimatedIncomeSection(visible, 80)  { IncomeHeroSection(visible) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 160) { IncomeStreamsSection(visible) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 240) { BreakdownSection(visible) }

            Spacer(Modifier.height(100.dp))
        }

        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            label         = "income_fab_scale"
        )
        IncomeFab(
            onClick  = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }
}

// ── Animated wrapper ───────────────────────────────────────────

@Composable
private fun AnimatedIncomeSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "income_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "income_alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

// ── Top bar ────────────────────────────────────────────────────

@Composable
private fun IncomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Avatar rounded square
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Person,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )
        }

        Text(
            text  = "Focus",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector        = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

// ── Hero ───────────────────────────────────────────────────────

@Composable
private fun IncomeHeroSection(visible: Boolean) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "TOTAL MONTHLY INCOME",
            style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Count-up
        val animVal by animateFloatAsState(
            targetValue   = if (visible) TOTAL_INCOME.toFloat() else 0f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing),
            label         = "income_hero_count"
        )
        Text(
            text      = "$${"%.2f".format(animVal)}",
            style     = MaterialTheme.typography.displayLarge,
            color     = Primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Growth chip — pill with trending up icon
        val chipAlpha by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = tween(400, delayMillis = 600),
            label         = "chip_alpha"
        )
        Row(
            modifier = Modifier
                .graphicsLayer { alpha = chipAlpha }
                .clip(RoundedCornerShape(100.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint               = Primary,
                modifier           = Modifier.size(14.dp)
            )
            Text(
                text  = "+${"%.1f".format(GROWTH_PERCENT)}% from last month",
                style = MaterialTheme.typography.labelMedium,
                color = Primary
            )
        }
    }
}

// ── Income Streams section ─────────────────────────────────────
// Each source is a card — dark surface, icon top-left, percent top-right,
// large name, subtitle, thin separator line, then amount.

@Composable
private fun IncomeStreamsSection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Income Streams",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(20.dp))

        dummyIncomeSources.forEachIndexed { index, source ->
            val cardAlpha by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(400, delayMillis = 200 + index * 100),
                label         = "card_alpha_$index"
            )
            val cardOffset by animateFloatAsState(
                targetValue   = if (visible) 0f else 30f,
                animationSpec = tween(400, delayMillis = 200 + index * 100, easing = FastOutSlowInEasing),
                label         = "card_offset_$index"
            )

            Box(
                Modifier.graphicsLayer {
                    alpha        = cardAlpha
                    translationY = cardOffset.dp.toPx()
                }
            ) {
                IncomeStreamCard(source = source)
            }

            if (index < dummyIncomeSources.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun IncomeStreamCard(source: IncomeSource) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(20.dp)
    ) {
        // Icon + percent row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Icon(
                imageVector        = source.icon,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
            Text(
                text  = "${source.percent}%",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        // Stream name — large light weight
        Text(
            text  = source.name,
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )

        // Subtitle
        Text(
            text  = source.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Thin separator — sand color, partial width matching percent
        Box(
            modifier = Modifier
                .fillMaxWidth(source.percent / 100f)
                .height(1.dp)
                .background(source.color)
        )

        // Full width dim track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineVariant)
        )

        Spacer(Modifier.height(12.dp))

        // Amount
        Text(
            text  = "$${"%.2f".format(source.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Breakdown section ──────────────────────────────────────────

@Composable
private fun BreakdownSection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Breakdown",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(20.dp))

        // Card containing bar + legend list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(20.dp)
        ) {
            // Segmented bar
            val barProgress by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(900, delayMillis = 300, easing = FastOutSlowInEasing),
                label         = "breakdown_bar"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
            ) {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.68f * barProgress + 0.001f)
                            .background(SalaryDot)
                    )
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.24f * barProgress + 0.001f)
                            .background(FreelanceDot)
                    )
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.08f * barProgress + 0.001f)
                            .background(InvestmentDot)
                    )
                    if (barProgress < 1f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f - barProgress)
                                .background(SurfaceContainerHigh)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Legend list — dot, label, amount (percent)
            dummyBreakdown.forEachIndexed { index, item ->
                val itemAlpha by animateFloatAsState(
                    targetValue   = if (visible) 1f else 0f,
                    animationSpec = tween(300, delayMillis = 400 + index * 80),
                    label         = "breakdown_item_$index"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = itemAlpha }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(item.color, CircleShape)
                        )
                        Text(
                            text  = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface
                        )
                    }
                    Text(
                        text  = "$${"%.2f".format(item.amount)} (${item.percent}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                if (index < dummyBreakdown.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Divider)
                    )
                }
            }
        }
    }
}

// ── FAB ────────────────────────────────────────────────────────

@Composable
private fun IncomeFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "income_fab_press"
    )
    val iconRotation by animateFloatAsState(
        targetValue   = if (pressed) 90f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "income_fab_rotate"
    )

    LaunchedEffect(pressed) {
        if (pressed) { delay(280); pressed = false }
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(14.dp))
            .background(FabBackground),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick  = { pressed = true; onClick() },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector        = Icons.Outlined.Add,
                contentDescription = "Add income",
                tint               = FabIcon,
                modifier           = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF13131A)
@Composable
fun IncomeScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme { IncomeScreen() }
}

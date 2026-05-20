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

data class Transaction(
    val icon: ImageVector,
    val name: String,
    val category: String,
    val date: String,
    val amount: Double,
    val categoryColor: Color
)

data class AllocationCategory(
    val label: String,
    val percent: Int,
    val color: Color,
    val fraction: Float
)

private val dummyTransactions = listOf(
    Transaction(Icons.Outlined.ShoppingBag,  "Apple Store Soho",   "Essential", "Today, 2:45 PM", -1299.00, EssentialDot),
    Transaction(Icons.Outlined.Restaurant,   "The Alchemist Bar",  "Luxury",    "Yesterday",       -84.20,  LuxuryDot),
    Transaction(Icons.Outlined.HomeWork,     "Monthly Mortgage",   "Essential", "Oct 01",         -2450.00, EssentialDot),
    Transaction(Icons.Outlined.Subscriptions,"Design System Pro",  "Extra",     "Sep 28",           -29.00, ExtraDot),
)

private val dummyAllocations = listOf(
    AllocationCategory("Essential", 55, EssentialDot, 0.55f),
    AllocationCategory("Luxury",    30, LuxuryDot,    0.30f),
    AllocationCategory("Extra",     15, ExtraDot,     0.15f),
)

private const val MONTHLY_NET    = 4820.50
private const val TOTAL_EXPENSES = 2140.0

// ── Root screen ────────────────────────────────────────────────

@Composable
fun ExpenseScreen(onAddClick: () -> Unit = {}) {
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
            // Top bar
            AnimatedSection(visible, 0) { ExpenseTopBar() }

            Spacer(Modifier.height(24.dp))

            // Hero
            AnimatedSection(visible, 80) { ExpenseHeroSection(visible) }

            Spacer(Modifier.height(48.dp))

            // Allocation
            AnimatedSection(visible, 180) { AllocationSection(visible) }

            Spacer(Modifier.height(48.dp))

            // Recent Activity
            AnimatedSection(visible, 260) { RecentActivitySection(visible) }

            Spacer(Modifier.height(100.dp))
        }

        // FAB — solid sand, bottom right
        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            label         = "fab_scale"
        )
        ExpenseFab(
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
private fun AnimatedSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

// ── Top bar ────────────────────────────────────────────────────

@Composable
private fun ExpenseTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Avatar — rounded square like in screenshot
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

// ── Hero section ───────────────────────────────────────────────

@Composable
private fun ExpenseHeroSection(visible: Boolean) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label — uppercase spaced
        Text(
            text      = "MONTHLY NET BALANCE",
            style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Count-up hero number — large, thin, sand color
        val animVal by animateFloatAsState(
            targetValue   = if (visible) MONTHLY_NET.toFloat() else 0f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing),
            label         = "hero_count"
        )
        Text(
            text      = "+${"%.2f".format(animVal)}",
            style     = MaterialTheme.typography.displayLarge,
            color     = Primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Subtitle
        Text(
            text      = "Income outweighs expenses by 32% this month",
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Allocation section ─────────────────────────────────────────

@Composable
private fun AllocationSection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Header row with total
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Allocation",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text  = "Total Exp: $${"%.0f".format(TOTAL_EXPENSES)}",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        // Segmented bar — no gaps, fills full width
        SegmentedAllocationBar(visible = visible)

        Spacer(Modifier.height(20.dp))

        // Three columns — dot + label + percent, no tile borders
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dummyAllocations.forEach { alloc ->
                AllocationLegendItem(alloc)
            }
        }
    }
}

@Composable
private fun SegmentedAllocationBar(visible: Boolean) {
    val progress by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(900, delayMillis = 200, easing = FastOutSlowInEasing),
        label         = "alloc_bar"
    )

    // Full-width segmented bar matching screenshot — sand | sage | rose
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Essential — sand
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.55f * progress + 0.001f)
                    .background(EssentialDot)
            )
            // Luxury — sage
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.30f * progress + 0.001f)
                    .background(LuxuryDot)
            )
            // Extra — dusty rose
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.15f * progress + 0.001f)
                    .background(ExtraDot)
            )
            // Remaining unfilled portion (animates away)
            if (progress < 1f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f - progress)
                        .background(SurfaceContainerHigh)
                )
            }
        }
    }
}

@Composable
private fun AllocationLegendItem(alloc: AllocationCategory) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(alloc.color, CircleShape)
            )
            Text(
                text  = alloc.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "${alloc.percent}%",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
    }
}

// ── Recent Activity ────────────────────────────────────────────

@Composable
private fun RecentActivitySection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Header with FILTER label
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Recent Activity",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text  = "FILTER",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                color = Primary
            )
        }

        Spacer(Modifier.height(24.dp))

        dummyTransactions.forEachIndexed { index, tx ->
            // Slide in from right, staggered
            val rowOffset by animateFloatAsState(
                targetValue   = if (visible) 0f else 60f,
                animationSpec = tween(400, delayMillis = 300 + index * 70, easing = FastOutSlowInEasing),
                label         = "row_offset_$index"
            )
            val rowAlpha by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(300, delayMillis = 300 + index * 70),
                label         = "row_alpha_$index"
            )

            Box(
                Modifier.graphicsLayer {
                    translationX = rowOffset.dp.toPx()
                    alpha        = rowAlpha
                }
            ) {
                TransactionRow(tx)
            }

            // Thin divider — 1px at 50% opacity
            if (index < dummyTransactions.lastIndex) {
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

@Composable
private fun TransactionRow(tx: Transaction) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "press_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier              = Modifier.weight(1f)
        ) {
            // Icon box — rounded square, dark surface
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = tx.icon,
                    contentDescription = null,
                    tint               = OnSurfaceVariant,
                    modifier           = Modifier.size(22.dp)
                )
            }

            // Name + category • date
            Column {
                Text(
                    text  = tx.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${tx.category} • ${tx.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }

        // Amount
        Text(
            text  = "-$${"%.2f".format(-tx.amount)}",
            style = MaterialTheme.typography.titleSmall,
            color = OnSurface
        )
    }
}

// ── FAB ────────────────────────────────────────────────────────

@Composable
private fun ExpenseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "fab_press"
    )
    val iconRotation by animateFloatAsState(
        targetValue   = if (pressed) 90f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "fab_rotate"
    )

    LaunchedEffect(pressed) {
        if (pressed) { delay(280); pressed = false }
    }

    // Solid sand background, dark icon — matches screenshot exactly
    Box(
        modifier = modifier
            .size(56.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(14.dp))
            .background(FabBackground),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { pressed = true; onClick() },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector        = Icons.Outlined.Add,
                contentDescription = "Add expense",
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
fun ExpenseScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme { ExpenseScreen() }
}

package com.example.aurafarm2.features.expenses

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
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
    val amount: Double,
    val categoryColor: Color
)

data class AllocationCategory(
    val label: String,
    val amount: Double,
    val color: Color,
    val fraction: Float
)

private val dummyTransactions = listOf(
    Transaction(Icons.Outlined.ShoppingCart,  "Whole Market",    "Groceries",    -142.00, EssentialDot),
    Transaction(Icons.Outlined.FlightTakeoff, "Delta Airlines",  "Travel",       -350.00, LuxuryDot),
    Transaction(Icons.Outlined.Subscriptions, "Design Software", "Subscription", -49.99,  ExtraDot),
    Transaction(Icons.Outlined.LocalCafe,     "Artisan Roasters","Dining",       -6.50,   LuxuryDot),
    Transaction(Icons.Outlined.ShoppingCart,  "Trader Joe's",    "Groceries",    -58.30,  EssentialDot),
)

private val dummyAllocations = listOf(
    AllocationCategory("Essential", 1067.0, EssentialDot, 0.60f),
    AllocationCategory("Luxury",     444.0, LuxuryDot,    0.25f),
    AllocationCategory("Extra",      268.0, ExtraDot,     0.15f),
)

private const val MONTHLY_NET    = 3420.50
private const val MONTHLY_INCOME = 5200.00
private const val MONTHLY_OUT    = 1779.00

// ── Root screen ────────────────────────────────────────────────

@Composable
fun ExpenseScreen(onAddClick: () -> Unit = {}) {

    // Master trigger — flips to true on first composition,
    // driving every entry animation on the screen.
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
            AnimatedSection(visible = visible, delayMs = 0) {
                ExpenseTopBar()
            }

            AnimatedSection(visible = visible, delayMs = 80) {
                HeroNetSection(visible = visible)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedSection(visible = visible, delayMs = 180) {
                AllocationSection(visible = visible)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedSection(visible = visible, delayMs = 260) {
                RecentActivitySection(visible = visible)
            }

            Spacer(Modifier.height(96.dp))
        }

        // FAB — springs in with a slight delay
        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            ),
            label = "fab_scale"
        )

        FloatingAddButton(
            onClick  = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }
}

// ── Generic animated wrapper ───────────────────────────────────
// Slides up + fades in. Every section uses this with a staggered delay.

@Composable
private fun AnimatedSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis    = delayMs,
            easing         = FastOutSlowInEasing
        ),
        label = "section_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis    = delayMs,
            easing         = FastOutSlowInEasing
        ),
        label = "section_alpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                this.alpha   = alpha
            }
    ) {
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .border(1.dp, OutlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "G",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
        }

        Text(
            text  = "Focus",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
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
private fun HeroNetSection(visible: Boolean) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Text(
            text  = "MONTHLY NET",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Animated count-up for the hero number
        CountUpNumber(
            target  = MONTHLY_NET,
            visible = visible
        )

        Spacer(Modifier.height(20.dp))

        InOutPill(income = MONTHLY_INCOME, expense = MONTHLY_OUT)
    }
}

// Counts up from 0 to target when visible flips true
@Composable
private fun CountUpNumber(target: Double, visible: Boolean) {
    val animatedValue by animateFloatAsState(
        targetValue   = if (visible) target.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "count_up"
    )

    Text(
        text      = "$${"%.2f".format(animatedValue)}",
        style     = MaterialTheme.typography.displayLarge,
        color     = OnSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun InOutPill(income: Double, expense: Double) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant, RoundedCornerShape(100.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(8.dp).background(IncomeGreen, CircleShape))
            Text(
                text  = "+$${"%.0f".format(income)} In",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }

        Box(Modifier.width(1.dp).height(16.dp).background(OutlineVariant))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(8.dp).background(ExpenseRed, CircleShape))
            Text(
                text  = "-$${"%.0f".format(expense)} Out",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
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
        Text(
            text  = "Allocation",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        AllocationBar(visible = visible)

        Spacer(Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dummyAllocations.forEachIndexed { index, alloc ->
                // Each tile springs in with its own delay
                val tileScale by animateFloatAsState(
                    targetValue   = if (visible) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMediumLow
                    ),
                    label = "tile_scale_$index"
                )
                val tileAlpha by animateFloatAsState(
                    targetValue   = if (visible) 1f else 0f,
                    animationSpec = tween(300, delayMillis = 300 + index * 80),
                    label         = "tile_alpha_$index"
                )

                AllocationTile(
                    allocation = alloc,
                    modifier   = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = tileScale
                            scaleY = tileScale
                            alpha  = tileAlpha
                        }
                )
            }
        }
    }
}

@Composable
private fun AllocationBar(visible: Boolean) {
    // Bar animates width from 0 → full when visible
    val progress by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 200, easing = FastOutSlowInEasing),
        label         = "alloc_bar"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(EssentialDot, LuxuryDot, ExtraDot)
                    )
                )
        )
    }
}

@Composable
private fun AllocationTile(
    allocation: AllocationCategory,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(7.dp).background(allocation.color, CircleShape))
            Text(
                text  = allocation.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text  = "$${"%.0f".format(allocation.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Recent activity ────────────────────────────────────────────

@Composable
private fun RecentActivitySection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Recent Activity",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        dummyTransactions.forEachIndexed { index, transaction ->
            // Each row slides in from right, staggered
            val rowOffset by animateFloatAsState(
                targetValue   = if (visible) 0f else 60f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis    = 300 + index * 70,
                    easing         = FastOutSlowInEasing
                ),
                label = "row_offset_$index"
            )
            val rowAlpha by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(300, delayMillis = 300 + index * 70),
                label         = "row_alpha_$index"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = rowOffset.dp.toPx()
                    alpha        = rowAlpha
                }
            ) {
                TransactionRow(transaction = transaction)
            }

            if (index < dummyTransactions.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 64.dp)
                        .height(1.dp)
                        .background(Divider)
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    // Press scale — spring back on release
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
            .padding(vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh)
                        .border(1.dp, OutlineVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = transaction.icon,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .border(1.5.dp, Background, CircleShape)
                        .background(transaction.categoryColor, CircleShape)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }

            Column {
                Text(
                    text  = transaction.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                Text(
                    text  = transaction.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        Text(
            text  = "-$${"%.2f".format(-transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
    }
}

// ── FAB — no glow ──────────────────────────────────────────────

@Composable
private fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Press feedback
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "fab_press"
    )

    FilledIconButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier
            .size(52.dp)
            .scale(pressScale),
        shape  = RoundedCornerShape(12.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = SurfaceContainerHigh
        )
    ) {
        // Rotate icon on press
        val iconRotation by animateFloatAsState(
            targetValue   = if (pressed) 90f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            ),
            label = "icon_rotate"
        )

        LaunchedEffect(pressed) {
            if (pressed) {
                delay(300)
                pressed = false
            }
        }

        Icon(
            imageVector        = Icons.Outlined.Add,
            contentDescription = "Add expense",
            tint               = Primary,
            modifier           = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF13131A)
@Composable
fun ExpenseScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme {
        ExpenseScreen()
    }
}
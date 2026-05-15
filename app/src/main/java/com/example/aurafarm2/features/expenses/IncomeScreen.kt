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

data class IncomeSource(
    val icon: ImageVector,
    val name: String,
    val tag: String,
    val amount: Double,
    val tagColor: Color
)

data class IncomeCategory(
    val label: String,
    val amount: Double,
    val color: Color,
    val fraction: Float
)

private val dummyIncomeSources = listOf(
    IncomeSource(Icons.Outlined.Work,             "Full-Time Job",    "Salary",    3500.00, EssentialDot),
    IncomeSource(Icons.Outlined.Laptop,           "Freelance Design", "Freelance",  850.00, LuxuryDot),
    IncomeSource(Icons.Outlined.TrendingUp,       "Stock Dividends",  "Passive",    320.00, SecondaryFixedDim),
    IncomeSource(Icons.Outlined.CurrencyBitcoin,  "Crypto Yield",     "Passive",    180.00, ExtraDot),
    IncomeSource(Icons.Outlined.Storefront,       "Side Store",       "Business",   350.00, LuxuryDot),
)

private val dummyIncomeCategories = listOf(
    IncomeCategory("Salary",    3500.0, EssentialDot,      0.65f),
    IncomeCategory("Freelance",  850.0, LuxuryDot,         0.16f),
    IncomeCategory("Passive",    500.0, SecondaryFixedDim,  0.09f),
    IncomeCategory("Business",   350.0, ExtraDot,           0.07f),
)

private const val TOTAL_INCOME   = 5200.00
private const val TOTAL_EXPENSES = 1779.00
private const val SAVINGS_RATE   = 65.8

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
            AnimatedIncomeSection(visible = visible, delayMs = 0) {
                IncomeTopBar()
            }

            AnimatedIncomeSection(visible = visible, delayMs = 80) {
                IncomeHeroSection(visible = visible)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedIncomeSection(visible = visible, delayMs = 180) {
                IncomeBreakdownSection(visible = visible)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedIncomeSection(visible = visible, delayMs = 260) {
                IncomeSourcesSection(visible = visible)
            }

            Spacer(Modifier.height(96.dp))
        }

        // FAB springs in
        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            ),
            label = "fab_scale"
        )

        FloatingIncomeAddButton(
            onClick  = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }
}

// ── Generic animated wrapper ───────────────────────────────────

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

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY.dp.toPx()
            this.alpha   = alpha
        }
    ) {
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
            text  = "Income",
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
private fun IncomeHeroSection(visible: Boolean) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "MONTHLY INCOME",
            style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Count-up
        val animatedValue by animateFloatAsState(
            targetValue   = if (visible) TOTAL_INCOME.toFloat() else 0f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            label         = "income_count_up"
        )
        Text(
            text      = "$${"%.2f".format(animatedValue)}",
            style     = MaterialTheme.typography.displayLarge,
            color     = OnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        SavedSpentPill(
            saved = TOTAL_INCOME - TOTAL_EXPENSES,
            spent = TOTAL_EXPENSES
        )

        Spacer(Modifier.height(16.dp))

        SavingsRateChip(rate = SAVINGS_RATE)
    }
}

@Composable
private fun SavedSpentPill(saved: Double, spent: Double) {
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
                text  = "+$${"%.0f".format(saved)} Saved",
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
                text  = "-$${"%.0f".format(spent)} Spent",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
    }
}

@Composable
private fun SavingsRateChip(rate: Double) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(PrimaryContainer.copy(alpha = 0.12f))
            .border(1.dp, PrimaryContainer.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Outlined.TrendingUp,
            contentDescription = null,
            tint               = Primary,
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text  = "${"%.1f".format(rate)}% savings rate",
            style = MaterialTheme.typography.labelLarge,
            color = Primary
        )
    }
}

// ── Breakdown section ──────────────────────────────────────────

@Composable
private fun IncomeBreakdownSection(visible: Boolean) {
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

        Spacer(Modifier.height(16.dp))

        // Animated bar
        val barProgress by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = tween(900, delayMillis = 200, easing = FastOutSlowInEasing),
            label         = "income_bar"
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
                    .fillMaxWidth(barProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(EssentialDot, LuxuryDot, SecondaryFixedDim, ExtraDot)
                        )
                    )
            )
        }

        Spacer(Modifier.height(12.dp))

        // 2×2 grid with spring scale-in
        listOf(
            dummyIncomeCategories.take(2),
            dummyIncomeCategories.drop(2)
        ).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEachIndexed { colIndex, cat ->
                    val tileIndex = rowIndex * 2 + colIndex
                    val tileScale by animateFloatAsState(
                        targetValue   = if (visible) 1f else 0.85f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMediumLow
                        ),
                        label = "income_tile_scale_$tileIndex"
                    )
                    val tileAlpha by animateFloatAsState(
                        targetValue   = if (visible) 1f else 0f,
                        animationSpec = tween(300, delayMillis = 300 + tileIndex * 80),
                        label         = "income_tile_alpha_$tileIndex"
                    )

                    IncomeTile(
                        category = cat,
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = tileScale
                                scaleY = tileScale
                                alpha  = tileAlpha
                            }
                    )
                }
            }
            if (rowIndex == 0) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun IncomeTile(
    category: IncomeCategory,
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
            Box(Modifier.size(7.dp).background(category.color, CircleShape))
            Text(
                text  = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text  = "$${"%.0f".format(category.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Sources list ───────────────────────────────────────────────

@Composable
private fun IncomeSourcesSection(visible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Sources",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        dummyIncomeSources.forEachIndexed { index, source ->
            val rowOffset by animateFloatAsState(
                targetValue   = if (visible) 0f else 60f,
                animationSpec = tween(400, delayMillis = 300 + index * 70, easing = FastOutSlowInEasing),
                label         = "income_row_offset_$index"
            )
            val rowAlpha by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(300, delayMillis = 300 + index * 70),
                label         = "income_row_alpha_$index"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = rowOffset.dp.toPx()
                    alpha        = rowAlpha
                }
            ) {
                IncomeSourceRow(source = source)
            }

            if (index < dummyIncomeSources.lastIndex) {
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
private fun IncomeSourceRow(source: IncomeSource) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "income_press_$source"
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
                        imageVector        = source.icon,
                        contentDescription = null,
                        tint               = Secondary,
                        modifier           = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .border(1.5.dp, Background, CircleShape)
                        .background(source.tagColor, CircleShape)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }

            Column {
                Text(
                    text  = source.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                Text(
                    text  = source.tag,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        Text(
            text  = "+$${"%.2f".format(source.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = Secondary
        )
    }
}

// ── FAB — no glow ──────────────────────────────────────────────

@Composable
private fun FloatingIncomeAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "income_fab_press"
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
        val iconRotation by animateFloatAsState(
            targetValue   = if (pressed) 90f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            ),
            label = "income_icon_rotate"
        )

        LaunchedEffect(pressed) {
            if (pressed) {
                delay(300)
                pressed = false
            }
        }

        Icon(
            imageVector        = Icons.Outlined.Add,
            contentDescription = "Add income",
            tint               = Secondary,
            modifier           = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF13131A)
@Composable
fun IncomeScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme {
        IncomeScreen()
    }
}
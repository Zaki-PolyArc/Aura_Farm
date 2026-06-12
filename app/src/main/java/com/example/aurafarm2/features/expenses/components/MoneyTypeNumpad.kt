package com.example.aurafarm2.features.expenses.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurafarm2.core.theme.OnSurface
import com.example.aurafarm2.core.theme.Primary
import com.example.aurafarm2.core.theme.SurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyTypeNumpadModal(
    title: String,
    currencySymbol: String,
    initialValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var input by remember { 
        mutableStateOf(if (initialValue > 0) String.format("%.2f", initialValue).removeSuffix(".00") else "") 
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121212), // Deep black background for the modal
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = OnSurface.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Massive display text
            Text(
                text = if (input.isEmpty()) "$currencySymbol 0" else "$currencySymbol $input",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light
                ),
                color = OnSurface,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Numpad Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NumpadKey("1") { if (input.length < 10) input += "1" }
                    NumpadKey("2") { if (input.length < 10) input += "2" }
                    NumpadKey("3") { if (input.length < 10) input += "3" }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NumpadKey("4") { if (input.length < 10) input += "4" }
                    NumpadKey("5") { if (input.length < 10) input += "5" }
                    NumpadKey("6") { if (input.length < 10) input += "6" }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NumpadKey("7") { if (input.length < 10) input += "7" }
                    NumpadKey("8") { if (input.length < 10) input += "8" }
                    NumpadKey("9") { if (input.length < 10) input += "9" }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NumpadKey(".") { if (!input.contains(".") && input.length < 8) input += if (input.isEmpty()) "0." else "." }
                    NumpadKey("0") { if (input.isNotEmpty() && input.length < 10) input += "0" }
                    NumpadKey("DEL", isAction = true) {
                        if (input.isNotEmpty()) input = input.dropLast(1)
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            // Confirm Button (Glassmorphic)
            val isValid = input.toDoubleOrNull() != null && input.toDouble() >= 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(elevation = if (isValid) 16.dp else 0.dp, shape = RoundedCornerShape(32.dp), spotColor = Primary.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isValid) Primary else SurfaceContainerHigh)
                    .clickable(enabled = isValid) {
                        val amount = input.toDoubleOrNull() ?: 0.0
                        onConfirm(amount)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CONFIRM",
                    style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp),
                    color = if (isValid) Color.Black else OnSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun NumpadKey(label: String, isAction: Boolean = false, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, animationSpec = spring(), label = "key_scale")

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isAction && label == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                contentDescription = "Backspace",
                tint = OnSurface,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Normal),
                color = OnSurface
            )
        }
    }
}

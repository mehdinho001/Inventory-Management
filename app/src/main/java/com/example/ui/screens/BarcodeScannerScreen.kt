package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockDanger
import com.example.ui.theme.StockSuccess
import com.example.ui.theme.StockWarning

@Composable
fun BarcodeScannerScreen(
    products: List<Product>,
    onAdjustQuantity: (String, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scannedProduct by remember { mutableStateOf<Product?>(null) }
    var manualInputCode by remember { mutableStateOf("") }
    var adjustmentDelta by remember { mutableIntStateOf(-1) } // Default to -1 (Take 1 unit)
    var adjustmentNotes by remember { mutableStateOf("Picked via Barcode Scanner") }
    var flashEnabled by remember { mutableStateOf(false) }

    fun triggerHapticAndSound() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) {}
    }

    fun handleCodeScanned(code: String) {
        val found = products.find {
            it.codeArticle.equals(code.trim(), ignoreCase = true) ||
            it.barcode.equals(code.trim(), ignoreCase = true)
        }
        if (found != null) {
            triggerHapticAndSound()
            scannedProduct = found
            adjustmentDelta = -1 // Suggested default: Take 1
            adjustmentNotes = "Picked via Barcode Scanner: -1 unit"
        }
    }

    // Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top instruction bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Barcode Scanner",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Aim camera at item barcode or select tag below",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Surface(
                color = if (flashEnabled) IndustrialCyan.copy(alpha = 0.2f) else Slate800,
                shape = CircleShape,
                modifier = Modifier.clickable { flashEnabled = !flashEnabled }
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Flashlight Toggle",
                    tint = if (flashEnabled) IndustrialCyan else Color.Gray,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scanner Viewfinder Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (flashEnabled) Color(0xFF1E293B) else Color(0xFF070B14))
                .border(2.dp, Slate700, RoundedCornerShape(16.dp))
                .testTag("scanner_viewfinder"),
            contentAlignment = Alignment.Center
        ) {
            // Animated Laser Beam
            Canvas(modifier = Modifier.fillMaxSize()) {
                val laserPosY = size.height * laserY
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF22C55E),
                            Color(0xFF4ADE80),
                            Color(0xFF22C55E),
                            Color.Transparent
                        )
                    ),
                    start = Offset(x = 20.dp.toPx(), y = laserPosY),
                    end = Offset(x = size.width - 20.dp.toPx(), y = laserPosY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Viewfinder reticle targeting corners
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 140.dp)
                    .border(1.5.dp, IndustrialCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = IndustrialCyan.copy(alpha = 0.25f),
                    modifier = Modifier.size(64.dp)
                )
            }

            // Scan status banner
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                color = Slate900.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Laser Scanner Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Scan Shelf Barcodes (Simulated physical item scanning)
        Text(
            text = "SIMULATED SHELF BARCODES (TAP TO SCAN)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { item ->
                Surface(
                    color = if (scannedProduct?.codeArticle == item.codeArticle) IndustrialCyan.copy(alpha = 0.2f) else Slate900,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (scannedProduct?.codeArticle == item.codeArticle) IndustrialCyan else Slate700
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clickable { handleCodeScanned(item.codeArticle) }
                        .testTag("scan_tag_${item.codeArticle}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = item.codeArticle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Qty: ${item.quantity}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = if (item.quantity <= item.minThreshold) StockWarning else StockSuccess
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manual Barcode entry row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualInputCode,
                onValueChange = { manualInputCode = it },
                label = { Text("Manual Barcode / Code Article") },
                placeholder = { Text("e.g. ART-1001") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_barcode_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndustrialCyan,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (manualInputCode.isNotBlank()) {
                        handleCodeScanned(manualInputCode)
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_lookup_barcode"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndustrialCyan
                )
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Lookup")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recognition Result & Suggested Adjustment Panel
        AnimatedVisibility(
            visible = scannedProduct != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            scannedProduct?.let { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanned_result_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate900
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(IndustrialCyan)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title + Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Recognized",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.codeArticle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = IndustrialCyan
                                        )
                                    )
                                }
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Current Stock Badge
                            Surface(
                                color = if (item.quantity <= item.minThreshold) StockWarning.copy(alpha = 0.2f) else StockSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (item.quantity <= item.minThreshold) StockWarning else StockSuccess
                                )
                            ) {
                                Text(
                                    text = "Current: ${item.quantity}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.quantity <= item.minThreshold) StockWarning else StockSuccess
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.storage} › ${item.local} › ${item.emplacement}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Suggested adjustment adjustmentDelta selector
                        Text(
                            text = "SUGGESTED STOCK ADJUSTMENT:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // -1 Take button
                            FilledTonalButton(
                                onClick = {
                                    adjustmentDelta = -1
                                    adjustmentNotes = "Scan Pick: -1 unit for maintenance"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (adjustmentDelta == -1) StockDanger else Slate800,
                                    contentColor = if (adjustmentDelta == -1) Color.White else StockDanger
                                )
                            ) {
                                Text("-1 Pick", fontWeight = FontWeight.Bold)
                            }

                            // -5 Bulk Pick
                            FilledTonalButton(
                                onClick = {
                                    adjustmentDelta = -5
                                    adjustmentNotes = "Scan Bulk Pick: -5 units"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (adjustmentDelta == -5) StockDanger else Slate800,
                                    contentColor = if (adjustmentDelta == -5) Color.White else StockDanger
                                )
                            ) {
                                Text("-5 Bulk", fontWeight = FontWeight.Bold)
                            }

                            // +5 Restock
                            FilledTonalButton(
                                onClick = {
                                    adjustmentDelta = 5
                                    adjustmentNotes = "Scan Restock: +5 units inbound"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (adjustmentDelta == 5) StockSuccess else Slate800,
                                    contentColor = if (adjustmentDelta == 5) Color.White else StockSuccess
                                )
                            ) {
                                Text("+5 Inbound", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action execution button
                        val resultingQuantity = item.quantity + adjustmentDelta
                        Button(
                            onClick = {
                                if (resultingQuantity >= 0) {
                                    onAdjustQuantity(item.codeArticle, adjustmentDelta, adjustmentNotes)
                                    scannedProduct = null
                                    manualInputCode = ""
                                }
                            },
                            enabled = resultingQuantity >= 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_confirm_scan_update"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (adjustmentDelta < 0) StockDanger else StockSuccess
                            )
                        ) {
                            Text(
                                text = if (adjustmentDelta < 0) {
                                    "Confirm Pick ($adjustmentDelta)  ›  New Stock: $resultingQuantity"
                                } else {
                                    "Confirm Restock (+$adjustmentDelta)  ›  New Stock: $resultingQuantity"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

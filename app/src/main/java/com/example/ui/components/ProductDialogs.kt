package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

// 1. Full Product Detail Modal
@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onTakeStock: () -> Unit,
    onAddStock: () -> Unit,
    onTransferStock: () -> Unit,
    onExecuteStock: () -> Unit,
    onDeleteProduct: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header: Code + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = product.codeArticle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndustrialCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Ref: ${product.reference} • ${product.category}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stock & Status Box
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT IN-STOCK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "${product.quantity} units",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.quantity <= product.minThreshold) StockWarning else StockSuccess
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ALERT THRESHOLD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "≤ ${product.minThreshold} units",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location Breakdown: Storage, Local, Emplacement
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = IndustrialCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WAREHOUSE LOCATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialCyan
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Storage: ${product.storage}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Text(
                            text = "Local / Zone: ${product.local}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Text(
                            text = "Emplacement: ${product.emplacement}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = IndustrialCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Barcode Visualizer
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barcode stripes simulation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val pattern = listOf(2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 2, 4, 1, 2, 3, 1)
                            pattern.forEach { w ->
                                Box(
                                    modifier = Modifier
                                        .width(w.dp)
                                        .height(34.dp)
                                        .background(Color.Black)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate800, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Operations Grid
                Text(
                    text = "ACTIONS & OPERATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Take Stock
                    FilledTonalButton(
                        onClick = onTakeStock,
                        enabled = product.quantity > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StockDanger.copy(alpha = 0.2f),
                            contentColor = StockDanger
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Take (-)")
                    }

                    // Add Stock
                    FilledTonalButton(
                        onClick = onAddStock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StockSuccess.copy(alpha = 0.2f),
                            contentColor = StockSuccess
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restock (+)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Transfer
                    OutlinedButton(
                        onClick = onTransferStock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transfer")
                    }

                    // Execution / Scrap Damaged
                    OutlinedButton(
                        onClick = onExecuteStock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StockWarning
                        )
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Execution")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delete Button
                TextButton(
                    onClick = onDeleteProduct,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = StockDanger)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Item from Catalog")
                }
            }
        }
    }
}

// 2. Manual Take Stock Dialog (Select Item + enter quantity taken)
@Composable
fun ManualTakeStockDialog(
    products: List<Product>,
    preselectedCode: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var selectedCode by remember { mutableStateOf(preselectedCode ?: products.firstOrNull()?.codeArticle ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentProduct = products.find { it.codeArticle == selectedCode }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Entry: Take Stock", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select item and enter quantity to deduct from global stock.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Select Item Picker if not locked
                Text(
                    text = "Target Item:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Quick selector buttons
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products.take(6)) { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (p.codeArticle == selectedCode) IndustrialCyan else Slate800,
                            modifier = Modifier.clickable { selectedCode = p.codeArticle }
                        ) {
                            Text(
                                text = p.codeArticle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (p.codeArticle == selectedCode) Slate950 else Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                currentProduct?.let { p ->
                    Surface(color = Slate850, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(p.description, style = MaterialTheme.typography.bodySmall.copy(color = Color.White), maxLines = 1)
                            Text("Current stock: ${p.quantity} units", style = MaterialTheme.typography.labelSmall.copy(color = if (p.quantity <= p.minThreshold) StockWarning else StockSuccess))
                            Text("Location: ${p.storage} > ${p.emplacement}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Quantity Taken") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_take_quantity"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndustrialCyan,
                        unfocusedBorderColor = Slate700
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Purpose / Work Order (Optional)") },
                    placeholder = { Text("e.g. Pump P-102 repair") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_take_notes"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndustrialCyan,
                        unfocusedBorderColor = Slate700
                    )
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = StockDanger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 0
                    val available = currentProduct?.quantity ?: 0
                    if (qty <= 0) {
                        errorMessage = "Quantity must be at least 1"
                        return@Button
                    }
                    if (qty > available) {
                        errorMessage = "Insufficient stock! Only $available available."
                        return@Button
                    }
                    onConfirm(selectedCode, qty, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StockDanger),
                modifier = Modifier.testTag("btn_confirm_take_stock")
            ) {
                Text("Confirm Pick", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(14.dp)
    )
}

// 3. Add Item Dialog (Code Article, Description, Reference, Storage, Local, Emplacement, Quantity, Barcode)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (Product) -> Unit
) {
    var codeArticle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var storage by remember { mutableStateOf("Depot Central") }
    var local by remember { mutableStateOf("Local A1") }
    var emplacement by remember { mutableStateOf("Rayon 01 - Bac 01") }
    var quantityText by remember { mutableStateOf("10") }
    var thresholdText by remember { mutableStateOf("5") }
    var priceText by remember { mutableStateOf("25.00") }
    var category by remember { mutableStateOf("Electrical") }
    var barcode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Item", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = codeArticle,
                    onValueChange = {
                        codeArticle = it.uppercase()
                        if (barcode.isBlank()) barcode = it.uppercase()
                        error = null
                    },
                    label = { Text("Code Article (e.g. ART-2001)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_input_code"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Fusible cylindrique 10x38 10A gG") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_input_desc"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference (e.g. REF-FUS-10A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_input_ref"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location fields: Storage, Local, Emplacement
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = storage,
                        onValueChange = { storage = it },
                        label = { Text("Storage") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                    OutlinedTextField(
                        value = local,
                        onValueChange = { local = it },
                        label = { Text("Local") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emplacement,
                    onValueChange = { emplacement = it },
                    label = { Text("Emplacement (Shelf / Bin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity & Threshold
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Initial Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Low Alert Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Unit Price & Barcode
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Unit Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                    )
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = StockDanger, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (codeArticle.isBlank()) {
                            error = "Code Article is required"
                            return@Button
                        }
                        if (description.isBlank()) {
                            error = "Description is required"
                            return@Button
                        }
                        val qty = quantityText.toIntOrNull() ?: 0
                        val threshold = thresholdText.toIntOrNull() ?: 5
                        val price = priceText.toDoubleOrNull() ?: 0.0

                        val newProduct = Product(
                            codeArticle = codeArticle.trim(),
                            description = description.trim(),
                            reference = reference.trim().ifBlank { "REF-$codeArticle" },
                            storage = storage.trim(),
                            local = local.trim(),
                            emplacement = emplacement.trim(),
                            quantity = qty,
                            minThreshold = threshold,
                            unitPrice = price,
                            barcode = barcode.trim().ifBlank { codeArticle.trim() },
                            category = category.trim()
                        )
                        onAdd(newProduct)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_new_product"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan)
                ) {
                    Text("Save Item to Catalog", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 4. Pre-defined Transaction: Transfer Dialog (moving stock between locations)
@Composable
fun TransferStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var newStorage by remember { mutableStateOf(product.storage) }
    var newLocal by remember { mutableStateOf(product.local) }
    var newEmplacement by remember { mutableStateOf(product.emplacement) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Location", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Move stock of ${product.codeArticle} (${product.description}) to a new location.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(color = Slate850, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Current: ${product.fullLocation}",
                        style = MaterialTheme.typography.bodySmall.copy(color = IndustrialCyan),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newStorage,
                    onValueChange = { newStorage = it },
                    label = { Text("New Storage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newLocal,
                    onValueChange = { newLocal = it },
                    label = { Text("New Local") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newEmplacement,
                    onValueChange = { newEmplacement = it },
                    label = { Text("New Emplacement") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transfer Reason (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan, unfocusedBorderColor = Slate700)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newStorage, newLocal, newEmplacement, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan)
            ) {
                Text("Confirm Transfer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(14.dp)
    )
}

// 5. Pre-defined Transaction: Execution Dialog (removing damaged or obsolete stock)
@Composable
fun ExecutionDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var quantityScrappedText by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("Damaged during installation") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val presetReasons = listOf(
        "Damaged during installation",
        "Obsolete / Deprecated reference",
        "Defective on arrival",
        "Expired chemical / shelf-life",
        "Broken during mechanical overhaul"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Execution / Damaged Stock", fontWeight = FontWeight.Bold, color = StockWarning) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Log permanent write-off for damaged, obsolete, or expired units of ${product.codeArticle}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityScrappedText,
                    onValueChange = { quantityScrappedText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantity to Scrap (Max: ${product.quantity})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StockWarning, unfocusedBorderColor = Slate700)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Cause / Execution Category:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))

                presetReasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (reason == r) StockWarning else Slate800)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(r, style = MaterialTheme.typography.bodySmall.copy(color = if (reason == r) Color.White else Color.Gray))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Incident Details / Root Cause") },
                    placeholder = { Text("e.g. Electrical short caused thermal coil trip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StockWarning, unfocusedBorderColor = Slate700)
                )

                error?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = StockDanger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityScrappedText.toIntOrNull() ?: 0
                    if (qty <= 0) {
                        error = "Quantity must be at least 1"
                        return@Button
                    }
                    if (qty > product.quantity) {
                        error = "Cannot scrap more than current stock (${product.quantity})"
                        return@Button
                    }
                    onConfirm(qty, reason, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StockWarning)
            ) {
                Text("Confirm Execution", fontWeight = FontWeight.Bold, color = Slate950)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(14.dp)
    )
}

// 6. Delete Confirmation Dialog (Prevents accidental deletions as requested)
@Composable
fun DeleteConfirmationDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StockDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Item Deletion", fontWeight = FontWeight.Bold, color = StockDanger)
            }
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete '${product.codeArticle} - ${product.description}' from the catalog? This will remove all inventory records and log an audit trail.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = StockDanger),
                modifier = Modifier.testTag("btn_confirm_delete_product")
            ) {
                Text("Delete Item", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(14.dp)
    )
}

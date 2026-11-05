package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.ui.components.AddProductDialog
import com.example.ui.components.CardInventoryView
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.ExecutionDialog
import com.example.ui.components.ManualTakeStockDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.TabularInventoryView
import com.example.ui.components.TransferStockDialog
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockDanger
import com.example.ui.theme.StockWarning
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.ViewDisplayMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterLowStockOnly by viewModel.filterLowStockOnly.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Dialog States
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var productForTake by remember { mutableStateOf<Product?>(null) }
    var productForTransfer by remember { mutableStateOf<Product?>(null) }
    var productForExecution by remember { mutableStateOf<Product?>(null) }
    var productForDelete by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showGenericManualTakeDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Electrical", "Mechanical", "Pneumatics", "Instrumentation", "Safety")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentUser?.role == UserRole.TECHNICIEN) {
                        showGenericManualTakeDialog = true
                    } else {
                        showAddDialog = true
                    }
                },
                containerColor = IndustrialCyan,
                contentColor = Slate950,
                modifier = Modifier.testTag("fab_inventory_action")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (currentUser?.role == UserRole.TECHNICIEN) "Take Stock" else "Add Item"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Search Bar & Display Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by code, desc, ref, location...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = IndustrialCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndustrialCyan,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Toggle Tabular vs Cards
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    IconButton(
                        onClick = {
                            viewModel.displayMode.value = if (displayMode == ViewDisplayMode.TABULAR) {
                                ViewDisplayMode.CARDS
                            } else {
                                ViewDisplayMode.TABULAR
                            }
                        },
                        modifier = Modifier.testTag("toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (displayMode == ViewDisplayMode.TABULAR) Icons.Default.GridView else Icons.Default.TableChart,
                            contentDescription = "Toggle View Mode",
                            tint = IndustrialCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Bar (Category + Low Stock Toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Low Stock Alert Filter Chip
                Surface(
                    color = if (filterLowStockOnly) StockWarning.copy(alpha = 0.2f) else Slate900,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (filterLowStockOnly) StockWarning else Slate700
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { viewModel.filterLowStockOnly.value = !filterLowStockOnly }
                        .testTag("filter_low_stock_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (filterLowStockOnly) StockWarning else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Low Stock Alerts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (filterLowStockOnly) StockWarning else Color.White
                            )
                        )
                    }
                }

                // Category Chips
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) IndustrialCyan.copy(alpha = 0.2f) else Slate900,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) IndustrialCyan else Slate800
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { viewModel.selectedCategory.value = cat }
                            .testTag("filter_category_$cat")
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) IndustrialCyan else Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Results count banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${products.size} item(s) found",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = if (displayMode == ViewDisplayMode.TABULAR) "Tabular View (Swipe horiz.)" else "Card View",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = IndustrialCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Product Presentation
            if (displayMode == ViewDisplayMode.TABULAR) {
                TabularInventoryView(
                    products = products,
                    onProductClick = { selectedProductForDetail = it },
                    onQuickTake = { productForTake = it },
                    onQuickAdd = {
                        viewModel.addStock(it.codeArticle, 1, "Quick restock: +1 unit")
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                CardInventoryView(
                    products = products,
                    onProductClick = { selectedProductForDetail = it },
                    onTakeStock = { productForTake = it },
                    onTransferStock = { productForTransfer = it },
                    onDeleteProduct = { productForDelete = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Modal Dialog Handlers
    selectedProductForDetail?.let { prod ->
        ProductDetailDialog(
            product = prod,
            onDismiss = { selectedProductForDetail = null },
            onTakeStock = {
                productForTake = prod
                selectedProductForDetail = null
            },
            onAddStock = {
                viewModel.addStock(prod.codeArticle, 5, "Batch restock +5 units")
                selectedProductForDetail = null
            },
            onTransferStock = {
                productForTransfer = prod
                selectedProductForDetail = null
            },
            onExecuteStock = {
                productForExecution = prod
                selectedProductForDetail = null
            },
            onDeleteProduct = {
                productForDelete = prod
                selectedProductForDetail = null
            }
        )
    }

    productForTake?.let { prod ->
        ManualTakeStockDialog(
            products = allProducts,
            preselectedCode = prod.codeArticle,
            onDismiss = { productForTake = null },
            onConfirm = { code, qty, notes ->
                viewModel.takeStock(code, qty, notes)
                productForTake = null
            }
        )
    }

    if (showGenericManualTakeDialog) {
        ManualTakeStockDialog(
            products = allProducts,
            onDismiss = { showGenericManualTakeDialog = false },
            onConfirm = { code, qty, notes ->
                viewModel.takeStock(code, qty, notes)
                showGenericManualTakeDialog = false
            }
        )
    }

    productForTransfer?.let { prod ->
        TransferStockDialog(
            product = prod,
            onDismiss = { productForTransfer = null },
            onConfirm = { storage, local, emp, notes ->
                viewModel.transferStock(prod.codeArticle, storage, local, emp, notes)
                productForTransfer = null
            }
        )
    }

    productForExecution?.let { prod ->
        ExecutionDialog(
            product = prod,
            onDismiss = { productForExecution = null },
            onConfirm = { qty, reason, notes ->
                viewModel.executeStock(prod.codeArticle, qty, reason, notes)
                productForExecution = null
            }
        )
    }

    productForDelete?.let { prod ->
        DeleteConfirmationDialog(
            product = prod,
            onDismiss = { productForDelete = null },
            onConfirmDelete = {
                viewModel.deleteProduct(prod.codeArticle)
                productForDelete = null
            }
        )
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newProd ->
                viewModel.addProduct(newProd)
                showAddDialog = false
            }
        )
    }
}

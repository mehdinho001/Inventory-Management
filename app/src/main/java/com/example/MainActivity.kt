package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddProductDialog
import com.example.ui.components.AppDestination
import com.example.ui.components.AuthDialog
import com.example.ui.components.InventoryBottomBar
import com.example.ui.components.InventoryTopBar
import com.example.ui.components.ManualTakeStockDialog
import com.example.ui.screens.BarcodeScannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExcelAndAiScreen
import com.example.ui.screens.InventoryListScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: InventoryViewModel = viewModel()
                var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
                var showAuthDialog by remember { mutableStateOf(false) }
                var showAddProductDialog by remember { mutableStateOf(false) }
                var showManualTakeDialog by remember { mutableStateOf(false) }
                var initialExcelSubTab by remember { mutableStateOf(0) }

                val currentUser by viewModel.currentUser.collectAsState()
                val allProducts by viewModel.allProducts.collectAsState()
                val allTransactions by viewModel.allTransactions.collectAsState()
                val excelSyncState by viewModel.excelSyncState.collectAsState()
                val aiInsight by viewModel.aiInsight.collectAsState()
                val allUsers by viewModel.allUsers.collectAsState()

                val lowStockCount = allProducts.count { it.isLowStock || it.isOutOfStock }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Listen for repository & ViewModel toast / report events
                LaunchedEffect(Unit) {
                    viewModel.uiEvents.collectLatest { event ->
                        when (event) {
                            is UiEvent.ShowToast -> {
                                Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                            }
                            is UiEvent.ShareReport -> {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, event.uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Inventory Master Stock Report")
                                    putExtra(Intent.EXTRA_TEXT, "Attached is the latest export report from Inventory Manager.")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(sendIntent, "Share Inventory Report (Excel / Sheets)")
                                startActivity(chooser)
                            }
                        }
                    }
                }

                // Back navigation handler: return to dashboard if on sub-screens
                BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
                    currentDestination = AppDestination.DASHBOARD
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        InventoryTopBar(
                            currentUser = currentUser,
                            lowStockCount = lowStockCount,
                            onNotificationsClick = {
                                viewModel.filterLowStockOnly.value = true
                                currentDestination = AppDestination.INVENTORY
                            },
                            onProfileClick = {
                                showAuthDialog = true
                            }
                        )
                    },
                    bottomBar = {
                        InventoryBottomBar(
                            currentDestination = currentDestination,
                            onNavigate = { currentDestination = it }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    when (currentDestination) {
                        AppDestination.DASHBOARD -> {
                            DashboardScreen(
                                currentUser = currentUser,
                                products = allProducts,
                                transactions = allTransactions,
                                excelSyncState = excelSyncState,
                                aiInsight = aiInsight,
                                onNavigateToScanner = { currentDestination = AppDestination.SCANNER },
                                onNavigateToInventory = {
                                    viewModel.filterLowStockOnly.value = false
                                    currentDestination = AppDestination.INVENTORY
                                },
                                onNavigateToExcel = {
                                    initialExcelSubTab = 0
                                    currentDestination = AppDestination.EXCEL_AI
                                },
                                onNavigateToAi = {
                                    initialExcelSubTab = 1
                                    currentDestination = AppDestination.EXCEL_AI
                                },
                                onNavigateToHistory = { currentDestination = AppDestination.HISTORY },
                                onOpenAddProductDialog = { showAddProductDialog = true },
                                onOpenManualTakeDialog = { showManualTakeDialog = true },
                                onQuickRoleSwitch = { role -> viewModel.switchRoleQuick(role) },
                                onExportReport = { viewModel.exportReport() },
                                modifier = screenModifier
                            )
                        }

                        AppDestination.INVENTORY -> {
                            InventoryListScreen(
                                viewModel = viewModel,
                                modifier = screenModifier
                            )
                        }

                        AppDestination.SCANNER -> {
                            BarcodeScannerScreen(
                                products = allProducts,
                                onAdjustQuantity = { code, delta, notes ->
                                    if (delta < 0) {
                                        viewModel.takeStock(code, -delta, notes)
                                    } else {
                                        viewModel.addStock(code, delta, notes)
                                    }
                                },
                                modifier = screenModifier
                            )
                        }

                        AppDestination.HISTORY -> {
                            TransactionHistoryScreen(
                                viewModel = viewModel,
                                modifier = screenModifier
                            )
                        }

                        AppDestination.EXCEL_AI -> {
                            ExcelAndAiScreen(
                                viewModel = viewModel,
                                initialSubTab = initialExcelSubTab,
                                modifier = screenModifier
                            )
                        }
                    }
                }

                // Global Dialogs
                if (showAuthDialog) {
                    AuthDialog(
                        currentUser = currentUser,
                        allUsers = allUsers,
                        onDismiss = { showAuthDialog = false },
                        onSelectUser = { viewModel.switchUser(it) },
                        onLogin = { email, pass -> viewModel.login(email, pass) },
                        onRegister = { email, pass, name, role ->
                            viewModel.register(email, pass, name, role)
                        },
                        onLogout = { viewModel.logout() }
                    )
                }

                if (showAddProductDialog) {
                    AddProductDialog(
                        onDismiss = { showAddProductDialog = false },
                        onAdd = { newProd ->
                            viewModel.addProduct(newProd)
                            showAddProductDialog = false
                        }
                    )
                }

                if (showManualTakeDialog) {
                    ManualTakeStockDialog(
                        products = allProducts,
                        onDismiss = { showManualTakeDialog = false },
                        onConfirm = { code, qty, notes ->
                            viewModel.takeStock(code, qty, notes)
                            showManualTakeDialog = false
                        }
                    )
                }
            }
        }
    }
}

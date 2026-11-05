package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiInsight
import com.example.data.model.ExcelSyncMetadata
import com.example.data.model.InventoryTransaction
import com.example.data.model.Product
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.RoleAdminPurple
import com.example.ui.theme.RoleManagerGreen
import com.example.ui.theme.RoleTechSky
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockDanger
import com.example.ui.theme.StockSuccess
import com.example.ui.theme.StockWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    currentUser: User?,
    products: List<Product>,
    transactions: List<InventoryTransaction>,
    excelSyncState: ExcelSyncMetadata,
    aiInsight: AiInsight?,
    onNavigateToScanner: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToExcel: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenAddProductDialog: () -> Unit,
    onOpenManualTakeDialog: () -> Unit,
    onQuickRoleSwitch: (UserRole) -> Unit,
    onExportReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val role = currentUser?.role ?: UserRole.TECHNICIEN
    val lowStockCount = products.count { it.isLowStock }
    val outOfStockCount = products.count { it.isOutOfStock }
    val totalValuation = products.sumOf { it.quantity * it.unitPrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Personalized User Welcome & Role Switcher
        item {
            UserRoleHeader(
                user = currentUser,
                onSwitchRole = onQuickRoleSwitch
            )
        }

        // 2. Role-Specific Dashboards
        when (role) {
            UserRole.TECHNICIEN -> {
                item {
                    TechnicienDashboardContent(
                        products = products,
                        transactions = transactions,
                        onNavigateToScanner = onNavigateToScanner,
                        onOpenManualTakeDialog = onOpenManualTakeDialog,
                        onNavigateToInventory = onNavigateToInventory
                    )
                }
            }
            UserRole.MANAGER -> {
                item {
                    ManagerDashboardContent(
                        products = products,
                        transactions = transactions,
                        totalValuation = totalValuation,
                        lowStockCount = lowStockCount,
                        outOfStockCount = outOfStockCount,
                        onNavigateToInventory = onNavigateToInventory,
                        onOpenAddProduct = onOpenAddProductDialog,
                        onExportReport = onExportReport
                    )
                }
            }
            UserRole.ADMIN -> {
                item {
                    AdminDashboardContent(
                        products = products,
                        transactions = transactions,
                        totalValuation = totalValuation,
                        lowStockCount = lowStockCount,
                        excelSyncState = excelSyncState,
                        aiInsight = aiInsight,
                        onNavigateToInventory = onNavigateToInventory,
                        onNavigateToExcel = onNavigateToExcel,
                        onNavigateToAi = onNavigateToAi,
                        onNavigateToHistory = onNavigateToHistory,
                        onExportReport = onExportReport
                    )
                }
            }
        }

        // 3. Low Stock Alert Banner (shown across all roles if items are low)
        if (lowStockCount > 0 || outOfStockCount > 0) {
            item {
                LowStockAlertCard(
                    lowCount = lowStockCount,
                    outCount = outOfStockCount,
                    onViewAlerts = onNavigateToInventory
                )
            }
        }

        // 4. Recent Activity Log Feed
        item {
            RecentActivityFeed(
                transactions = transactions.take(5),
                onViewAll = onNavigateToHistory
            )
        }
    }
}

@Composable
private fun UserRoleHeader(
    user: User?,
    onSwitchRole: (UserRole) -> Unit
) {
    val roleColor = when (user?.role) {
        UserRole.ADMIN -> RoleAdminPurple
        UserRole.MANAGER -> RoleManagerGreen
        else -> RoleTechSky
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_user_header"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = roleColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = user?.fullName ?: "Staff Member",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = user?.email ?: "technicien@inventory.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Active Role Pill
                Surface(
                    color = roleColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, roleColor),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = user?.role?.displayName?.uppercase(Locale.ROOT) ?: "TECHNICIEN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = roleColor
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate800, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Role Switcher for fast verification and demoing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Switch Role View:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserRole.values().forEach { r ->
                        val isSelected = user?.role == r
                        val btnColor = when (r) {
                            UserRole.ADMIN -> RoleAdminPurple
                            UserRole.MANAGER -> RoleManagerGreen
                            UserRole.TECHNICIEN -> RoleTechSky
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) btnColor else Slate800,
                            modifier = Modifier
                                .clickable { onSwitchRole(r) }
                                .testTag("role_switch_${r.name.lowercase()}")
                        ) {
                            Text(
                                text = r.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Slate950 else Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Technicien Personalized View ---
@Composable
private fun TechnicienDashboardContent(
    products: List<Product>,
    transactions: List<InventoryTransaction>,
    onNavigateToScanner: () -> Unit,
    onOpenManualTakeDialog: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "TECHNICIAN WORKSPACE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RoleTechSky,
                letterSpacing = 1.sp
            )
        )

        // Big Primary Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Scan Barcode",
                subtitle = "Camera / Laser pick",
                icon = Icons.Default.QrCodeScanner,
                color = RoleTechSky,
                onClick = onNavigateToScanner,
                modifier = Modifier.weight(1f)
            )

            ActionTile(
                title = "Take Stock",
                subtitle = "Manual quantity entry",
                icon = Icons.Default.Remove,
                color = StockDanger,
                onClick = onOpenManualTakeDialog,
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricCard(
                title = "Workshop Items",
                value = "${products.count { it.storage.contains("Atelier") || it.storage.contains("Depot") }}",
                subtitle = "Available nearby",
                icon = Icons.Default.Inventory,
                color = RoleTechSky,
                modifier = Modifier.weight(1f)
            )

            StatMetricCard(
                title = "Low in Plant",
                value = "${products.count { it.isLowStock }}",
                subtitle = "Refill pending",
                icon = Icons.Default.Warning,
                color = StockWarning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- Manager Personalized View ---
@Composable
private fun ManagerDashboardContent(
    products: List<Product>,
    transactions: List<InventoryTransaction>,
    totalValuation: Double,
    lowStockCount: Int,
    outOfStockCount: Int,
    onNavigateToInventory: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onExportReport: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "MANAGER OVERVIEW & SUPPLY CHAIN",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RoleManagerGreen,
                letterSpacing = 1.sp
            )
        )

        // KPI Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricCard(
                title = "Inventory Valuation",
                value = "\$${String.format(Locale.US, "%,.0f", totalValuation)}",
                subtitle = "${products.size} Active SKUs",
                icon = Icons.Default.TrendingUp,
                color = RoleManagerGreen,
                modifier = Modifier.weight(1f)
            )

            StatMetricCard(
                title = "Alerts & Outages",
                value = "${lowStockCount + outOfStockCount}",
                subtitle = "$outOfStockCount out of stock",
                icon = Icons.Default.Warning,
                color = if (outOfStockCount > 0) StockDanger else StockWarning,
                modifier = Modifier.weight(1f)
            )
        }

        // Manager Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenAddProduct,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_add_item_manager"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleManagerGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Article", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onExportReport,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_export_manager"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Excel CSV", fontSize = 12.sp)
            }
        }
    }
}

// --- Admin Personalized View ---
@Composable
private fun AdminDashboardContent(
    products: List<Product>,
    transactions: List<InventoryTransaction>,
    totalValuation: Double,
    lowStockCount: Int,
    excelSyncState: ExcelSyncMetadata,
    aiInsight: AiInsight?,
    onNavigateToInventory: () -> Unit,
    onNavigateToExcel: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onExportReport: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "ADMINISTRATOR CONTROL CONSOLE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RoleAdminPurple,
                letterSpacing = 1.sp
            )
        )

        // 3 Key System Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricCard(
                title = "Total Catalog",
                value = "${products.size}",
                subtitle = "Products tracked",
                icon = Icons.Default.Inventory,
                color = RoleAdminPurple,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Valuation",
                value = "\$${String.format(Locale.US, "%,.0f", totalValuation)}",
                subtitle = "Total stock value",
                icon = Icons.Default.TrendingUp,
                color = RoleManagerGreen,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Audit Logs",
                value = "${transactions.size}",
                subtitle = "Actions recorded",
                icon = Icons.Default.Shield,
                color = IndustrialCyan,
                modifier = Modifier.weight(1f)
            )
        }

        // AI Suggestion Banner Widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToAi)
                .testTag("ai_suggestion_banner"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(RoleAdminPurple.copy(alpha = 0.6f))
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RoleAdminPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = RoleAdminPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Inventory Suggestions",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = aiInsight?.title ?: "Generate replenishment predictions & trend advice",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = RoleAdminPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Excel Database Connection Widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToExcel)
                .testTag("excel_sync_banner"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Slate700)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Excel File",
                        tint = RoleManagerGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Excel Database Connection",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${excelSyncState.connectedFileName} (${excelSyncState.lastAction})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }
                Surface(
                    color = RoleManagerGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SYNCED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoleManagerGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("action_tile_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LowStockAlertCard(
    lowCount: Int,
    outCount: Int,
    onViewAlerts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewAlerts)
            .testTag("low_stock_alert_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = StockWarning.copy(alpha = 0.12f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(StockWarning.copy(alpha = 0.5f))
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                tint = StockWarning,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inventory Refill Alert",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = StockWarning
                    )
                )
                Text(
                    text = "$lowCount item(s) below threshold, $outCount completely depleted.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White
                    )
                )
            }
            Text(
                text = "View",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = StockWarning
                )
            )
        }
    }
}

@Composable
private fun RecentActivityFeed(
    transactions: List<InventoryTransaction>,
    onViewAll: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT OPERATIONS AUDIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = IndustrialCyan
                    ),
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "No recorded transactions yet.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                transactions.forEachIndexed { idx, tx ->
                    val color = Color(tx.type.badgeColorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tx.codeArticle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tx.type.label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = color
                                    )
                                )
                            }
                            Text(
                                text = "By ${tx.staffName} (${tx.staffRole}) • ${dateFormat.format(Date(tx.timestamp))}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Delta badge
                        if (tx.quantityChange != 0) {
                            Text(
                                text = if (tx.quantityChange > 0) "+${tx.quantityChange}" else "${tx.quantityChange}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            )
                        }
                    }
                    if (idx < transactions.lastIndex) {
                        HorizontalDivider(color = Slate800, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

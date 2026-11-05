package com.example.data.model

data class ExcelSyncMetadata(
    val connectedFileName: String = "Master_Inventory_Catalog.xlsx",
    val isConnected: Boolean = true,
    val totalItemsCount: Int = 0,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val lastAction: String = "Connected",
    val sheetName: String = "Articles_Stock"
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String, val badgeColorHex: Long) {
    STOCK_OUT("Stock Out / Pick", 0xFFEF4444),
    STOCK_IN("Restock / Inbound", 0xFF10B981),
    TRANSFER("Location Transfer", 0xFF3B82F6),
    EXECUTION("Execution / Damaged", 0xFFF97316),
    MANUAL_ADJUST("Manual Adjustment", 0xFF8B5CF6)
}

@Entity(tableName = "transactions")
data class InventoryTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codeArticle: String,
    val productDescription: String,
    val type: TransactionType,
    val quantityChange: Int, // e.g. -3, +10
    val previousQuantity: Int,
    val newQuantity: Int,
    val staffEmail: String,
    val staffName: String,
    val staffRole: String, // Technicien, Manager, Admin
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val sourceLocation: String = "",
    val destLocation: String = "",
    val executionReason: String = "" // "Damaged during work", "Obsolete reference", "Expired shelf-life"
)

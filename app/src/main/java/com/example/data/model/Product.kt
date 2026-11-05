package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val codeArticle: String,
    val description: String,
    val reference: String,
    val storage: String,
    val local: String,
    val emplacement: String,
    val quantity: Int,
    val minThreshold: Int = 10,
    val barcode: String = codeArticle,
    val category: String = "General",
    val unitPrice: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = quantity <= minThreshold && quantity > 0
    val isOutOfStock: Boolean get() = quantity <= 0
    val fullLocation: String get() = "$storage > $local > $emplacement"
}

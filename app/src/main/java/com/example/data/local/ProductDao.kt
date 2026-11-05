package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY codeArticle ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE codeArticle = :code OR barcode = :code LIMIT 1")
    suspend fun getProductByCodeOrBarcode(code: String): Product?

    @Query("SELECT * FROM products WHERE codeArticle = :code LIMIT 1")
    fun getProductFlow(code: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE quantity <= minThreshold ORDER BY quantity ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE codeArticle = :code")
    suspend fun deleteProductByCode(code: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("UPDATE products SET quantity = :newQty, lastUpdated = :timestamp WHERE codeArticle = :code")
    suspend fun updateQuantity(code: String, newQty: Int, timestamp: Long)

    @Query("UPDATE products SET storage = :storage, local = :local, emplacement = :emplacement, lastUpdated = :timestamp WHERE codeArticle = :code")
    suspend fun updateLocation(code: String, storage: String, local: String, emplacement: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

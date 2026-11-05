package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Product
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Inventory Manager", appName)
    }

    @Test
    fun `product stock threshold calculation`() {
        val lowStockProduct = Product(
            codeArticle = "ART-TEST",
            description = "Test Circuit Breaker",
            reference = "REF-TEST",
            storage = "Depot Central",
            local = "Local A1",
            emplacement = "Rayon 1",
            quantity = 3,
            minThreshold = 5
        )
        assertTrue(lowStockProduct.isLowStock)
        assertFalse(lowStockProduct.isOutOfStock)

        val outOfStockProduct = lowStockProduct.copy(quantity = 0)
        assertTrue(outOfStockProduct.isOutOfStock)
        assertFalse(outOfStockProduct.isLowStock) // isLowStock is quantity > 0 && quantity <= minThreshold

        val normalProduct = lowStockProduct.copy(quantity = 20)
        assertFalse(normalProduct.isLowStock)
        assertFalse(normalProduct.isOutOfStock)
    }

    @Test
    fun `user role parsing`() {
        assertEquals(UserRole.ADMIN, UserRole.fromString("Admin"))
        assertEquals(UserRole.MANAGER, UserRole.fromString("manager"))
        assertEquals(UserRole.TECHNICIEN, UserRole.fromString("Technicien"))
        assertEquals(UserRole.TECHNICIEN, UserRole.fromString("unknown"))
    }
}

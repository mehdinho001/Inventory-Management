package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiInsight
import com.example.data.ai.AiInventoryService
import com.example.data.model.ExcelSyncMetadata
import com.example.data.model.InventoryTransaction
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductSortOrder(val label: String) {
    BY_CODE("Code Article (A-Z)"),
    BY_NAME("Description (A-Z)"),
    BY_QUANTITY_ASC("Quantity (Low to High)"),
    BY_QUANTITY_DESC("Quantity (High to Low)")
}

enum class ViewDisplayMode {
    TABULAR,
    CARDS
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShareReport(val uri: Uri) : UiEvent()
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application, viewModelScope)
    private val aiService = AiInventoryService()

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<InventoryTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<User?> = repository.currentUser
    val excelSyncState: StateFlow<ExcelSyncMetadata> = repository.excelSyncState

    // Search and Filter State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedStorage = MutableStateFlow("All")
    val filterLowStockOnly = MutableStateFlow(false)
    val sortOrder = MutableStateFlow(ProductSortOrder.BY_CODE)
    val displayMode = MutableStateFlow(ViewDisplayMode.TABULAR)

    data class FilterCriteria(
        val query: String,
        val category: String,
        val storage: String,
        val lowStockOnly: Boolean,
        val sort: ProductSortOrder
    )

    private val filterCriteria = combine(
        searchQuery,
        selectedCategory,
        selectedStorage,
        filterLowStockOnly,
        sortOrder
    ) { query, category, storage, lowStockOnly, sort ->
        FilterCriteria(query, category, storage, lowStockOnly, sort)
    }

    // Filtered Products Stream
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        filterCriteria
    ) { products, criteria ->
        var list = products

        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.codeArticle.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.reference.lowercase().contains(q) ||
                it.storage.lowercase().contains(q) ||
                it.local.lowercase().contains(q) ||
                it.emplacement.lowercase().contains(q) ||
                it.barcode.lowercase().contains(q)
            }
        }

        if (criteria.category != "All") {
            list = list.filter { it.category.equals(criteria.category, ignoreCase = true) }
        }

        if (criteria.storage != "All") {
            list = list.filter { it.storage.equals(criteria.storage, ignoreCase = true) }
        }

        if (criteria.lowStockOnly) {
            list = list.filter { it.isLowStock || it.isOutOfStock }
        }

        when (criteria.sort) {
            ProductSortOrder.BY_CODE -> list.sortedBy { it.codeArticle }
            ProductSortOrder.BY_NAME -> list.sortedBy { it.description }
            ProductSortOrder.BY_QUANTITY_ASC -> list.sortedBy { it.quantity }
            ProductSortOrder.BY_QUANTITY_DESC -> list.sortedByDescending { it.quantity }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Insight State
    private val _aiInsight = MutableStateFlow<AiInsight?>(null)
    val aiInsight: StateFlow<AiInsight?> = _aiInsight.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // UI Events
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    // --- Actions ---

    fun takeStock(code: String, quantity: Int, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.takeStock(code, quantity, notes)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Took $quantity unit(s) of ${it.codeArticle}. New stock: ${it.quantity}"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to take stock"))
            }
        }
    }

    fun addStock(code: String, quantity: Int, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.addStock(code, quantity, notes)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Added +$quantity unit(s) to ${it.codeArticle}. New stock: ${it.quantity}"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to add stock"))
            }
        }
    }

    fun adjustStock(code: String, newQuantity: Int, reason: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.adjustStock(code, newQuantity, reason)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Stock reconciled for ${it.codeArticle} to $newQuantity"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to adjust stock"))
            }
        }
    }

    fun transferStock(
        code: String,
        newStorage: String,
        newLocal: String,
        newEmplacement: String,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.transferStock(code, newStorage, newLocal, newEmplacement, notes)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Transferred ${it.codeArticle} to $newStorage > $newLocal"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to transfer stock"))
            }
        }
    }

    fun executeStock(
        code: String,
        quantityScrapped: Int,
        reason: String,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.executeStock(code, quantityScrapped, reason, notes)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Scrapped $quantityScrapped unit(s) of ${it.codeArticle} ($reason)"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to execute stock"))
            }
        }
    }

    fun addProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.addProduct(product)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Product ${product.codeArticle} added successfully!"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to add product"))
            }
        }
    }

    fun deleteProduct(code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteProduct(code)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Product $code removed from catalog"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Failed to delete product"))
            }
        }
    }

    fun importSpreadsheet(uri: Uri) {
        viewModelScope.launch {
            val res = repository.importSpreadsheet(uri)
            res.onSuccess { count ->
                _uiEvents.emit(UiEvent.ShowToast("Excel Sync: Successfully imported $count products!"))
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast("Import Failed: ${it.message}"))
            }
        }
    }

    fun exportReport() {
        viewModelScope.launch {
            val res = repository.exportInventoryReport()
            res.onSuccess { uri ->
                _uiEvents.emit(UiEvent.ShareReport(uri))
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast("Export Failed: ${it.message}"))
            }
        }
    }

    fun resetCatalogToDefault() {
        viewModelScope.launch {
            val res = repository.resetDatabaseToDefault()
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Catalog reset to factory industrial master dataset"))
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast("Reset Failed: ${it.message}"))
            }
        }
    }

    fun requestAiInsight(customPrompt: String = "Analyze stock trends and recommend optimal reorder points") {
        viewModelScope.launch {
            _isAiLoading.value = true
            val products = allProducts.value
            val transactions = allTransactions.value
            val res = aiService.analyzeInventory(products, transactions, customPrompt)
            _isAiLoading.value = false
            res.onSuccess { insight ->
                _aiInsight.value = insight
                _uiEvents.emit(UiEvent.ShowToast("AI Analysis updated successfully"))
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast("AI Analysis error: ${it.message}"))
            }
        }
    }

    // Role switching & Auth helpers
    fun switchUser(user: User) {
        repository.switchActiveUser(user)
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowToast("Switched user to: ${user.fullName} (${user.role.displayName})"))
        }
    }

    fun switchRoleQuick(role: UserRole) {
        val current = currentUser.value
        val users = allUsers.value
        val match = users.find { it.role == role }
        if (match != null) {
            repository.switchActiveUser(match)
        } else if (current != null) {
            repository.switchActiveUser(current.copy(role = role))
        }
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowToast("Active Role: ${role.displayName}"))
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.login(email, pass)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Welcome back, ${it.fullName}!"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Login failed"))
            }
        }
    }

    fun register(email: String, pass: String, name: String, role: UserRole, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.register(email, pass, name, role)
            res.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Account created for ${it.fullName} (${it.role.displayName})"))
                onSuccess()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowToast(it.message ?: "Registration failed"))
            }
        }
    }

    fun logout() {
        repository.logout()
    }
}

package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.InventoryDatabase
import com.example.data.model.ExcelSyncMetadata
import com.example.data.model.InventoryTransaction
import com.example.data.model.Product
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val database = InventoryDatabase.getDatabase(context, scope)
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val userDao = database.userDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allTransactions: Flow<List<InventoryTransaction>> = transactionDao.getAllTransactions()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    // Active session user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Excel Connection State
    private val _excelSyncState = MutableStateFlow(ExcelSyncMetadata())
    val excelSyncState = _excelSyncState.asStateFlow()

    init {
        // Default login as Admin for instant usability, or user can switch anytime
        scope.launch(Dispatchers.IO) {
            val admin = userDao.getUserByEmail("admin@inventory.com")
            if (admin != null) {
                _currentUser.value = admin
            } else {
                // If not yet populated from callback, create a fallback admin session
                val defaultAdmin = User(
                    email = "admin@inventory.com",
                    password = "admin",
                    fullName = "Alexandre Dupont",
                    role = UserRole.ADMIN
                )
                _currentUser.value = defaultAdmin
            }
        }
    }

    // --- Authentication ---
    suspend fun login(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user != null && user.password == pass.trim()) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun register(
        email: String,
        pass: String,
        fullName: String,
        role: UserRole
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }
        val newUser = User(
            email = cleanEmail,
            password = pass.trim(),
            fullName = fullName.trim(),
            role = role
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser
        Result.success(newUser)
    }

    fun switchActiveUser(user: User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- Inventory Operations ---

    suspend fun getProduct(code: String): Product? = withContext(Dispatchers.IO) {
        productDao.getProductByCodeOrBarcode(code.trim())
    }

    /**
     * Operation a: Manual entry (select Item + enter quantity taken)
     * Deducts quantity from global quantity and logs staff member action with timestamp.
     */
    suspend fun takeStock(
        codeArticle: String,
        quantityTaken: Int,
        notes: String = ""
    ): Result<Product> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found: $codeArticle"))

        if (quantityTaken <= 0) {
            return@withContext Result.failure(Exception("Quantity taken must be greater than 0"))
        }
        if (product.quantity < quantityTaken) {
            return@withContext Result.failure(Exception("Insufficient stock! Available: ${product.quantity}, requested: $quantityTaken"))
        }

        val previousQty = product.quantity
        val newQty = previousQty - quantityTaken
        val now = System.currentTimeMillis()

        productDao.updateQuantity(product.codeArticle, newQty, now)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.STOCK_OUT,
            quantityChange = -quantityTaken,
            previousQuantity = previousQty,
            newQuantity = newQty,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Unknown Staff",
            staffRole = user?.role?.displayName ?: "Technicien",
            timestamp = now,
            notes = notes.ifBlank { "Manual pick of $quantityTaken unit(s)" }
        )
        transactionDao.insertTransaction(tx)

        val updated = product.copy(quantity = newQty, lastUpdated = now)
        Result.success(updated)
    }

    /**
     * Add Stock / Restock inbound
     */
    suspend fun addStock(
        codeArticle: String,
        quantityAdded: Int,
        notes: String = ""
    ): Result<Product> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found: $codeArticle"))

        if (quantityAdded <= 0) {
            return@withContext Result.failure(Exception("Quantity added must be greater than 0"))
        }

        val previousQty = product.quantity
        val newQty = previousQty + quantityAdded
        val now = System.currentTimeMillis()

        productDao.updateQuantity(product.codeArticle, newQty, now)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.STOCK_IN,
            quantityChange = quantityAdded,
            previousQuantity = previousQty,
            newQuantity = newQty,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Unknown Staff",
            staffRole = user?.role?.displayName ?: "Manager",
            timestamp = now,
            notes = notes.ifBlank { "Received inbound shipment: +$quantityAdded unit(s)" }
        )
        transactionDao.insertTransaction(tx)

        val updated = product.copy(quantity = newQty, lastUpdated = now)
        Result.success(updated)
    }

    /**
     * Manual Stock Adjustment (audit reconciliation)
     */
    suspend fun adjustStock(
        codeArticle: String,
        newAbsoluteQuantity: Int,
        reason: String
    ): Result<Product> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found: $codeArticle"))

        if (newAbsoluteQuantity < 0) {
            return@withContext Result.failure(Exception("Quantity cannot be negative"))
        }

        val previousQty = product.quantity
        val diff = newAbsoluteQuantity - previousQty
        val now = System.currentTimeMillis()

        productDao.updateQuantity(product.codeArticle, newAbsoluteQuantity, now)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.MANUAL_ADJUST,
            quantityChange = diff,
            previousQuantity = previousQty,
            newQuantity = newAbsoluteQuantity,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Staff",
            staffRole = user?.role?.displayName ?: "Manager",
            timestamp = now,
            notes = reason.ifBlank { "Inventory audit adjustment from $previousQty to $newAbsoluteQuantity" }
        )
        transactionDao.insertTransaction(tx)

        val updated = product.copy(quantity = newAbsoluteQuantity, lastUpdated = now)
        Result.success(updated)
    }

    /**
     * Pre-defined Transaction: Transfer (moving stock between locations)
     */
    suspend fun transferStock(
        codeArticle: String,
        newStorage: String,
        newLocal: String,
        newEmplacement: String,
        notes: String
    ): Result<Product> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found: $codeArticle"))

        val sourceLoc = product.fullLocation
        val destLoc = "$newStorage > $newLocal > $newEmplacement"
        val now = System.currentTimeMillis()

        productDao.updateLocation(product.codeArticle, newStorage, newLocal, newEmplacement, now)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.TRANSFER,
            quantityChange = 0,
            previousQuantity = product.quantity,
            newQuantity = product.quantity,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Staff",
            staffRole = user?.role?.displayName ?: "Technicien",
            timestamp = now,
            notes = notes.ifBlank { "Location transfer from $sourceLoc to $destLoc" },
            sourceLocation = sourceLoc,
            destLocation = destLoc
        )
        transactionDao.insertTransaction(tx)

        val updated = product.copy(
            storage = newStorage,
            local = newLocal,
            emplacement = newEmplacement,
            lastUpdated = now
        )
        Result.success(updated)
    }

    /**
     * Pre-defined Transaction: Execution (removing damaged or obsolete stock)
     */
    suspend fun executeStock(
        codeArticle: String,
        quantityScrapped: Int,
        executionReason: String,
        notes: String
    ): Result<Product> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found: $codeArticle"))

        if (quantityScrapped <= 0) {
            return@withContext Result.failure(Exception("Scrapped quantity must be greater than 0"))
        }
        if (product.quantity < quantityScrapped) {
            return@withContext Result.failure(Exception("Cannot scrap more than current stock (${product.quantity})"))
        }

        val previousQty = product.quantity
        val newQty = previousQty - quantityScrapped
        val now = System.currentTimeMillis()

        productDao.updateQuantity(product.codeArticle, newQty, now)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.EXECUTION,
            quantityChange = -quantityScrapped,
            previousQuantity = previousQty,
            newQuantity = newQty,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Staff",
            staffRole = user?.role?.displayName ?: "Technicien",
            timestamp = now,
            notes = notes.ifBlank { "Execution of $quantityScrapped unit(s): $executionReason" },
            executionReason = executionReason
        )
        transactionDao.insertTransaction(tx)

        val updated = product.copy(quantity = newQty, lastUpdated = now)
        Result.success(updated)
    }

    /**
     * Add new Product
     */
    suspend fun addProduct(product: Product): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = productDao.getProductByCodeOrBarcode(product.codeArticle)
        if (existing != null) {
            return@withContext Result.failure(Exception("An item with Code Article '${product.codeArticle}' already exists!"))
        }
        productDao.insertProduct(product)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.STOCK_IN,
            quantityChange = product.quantity,
            previousQuantity = 0,
            newQuantity = product.quantity,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Staff",
            staffRole = user?.role?.displayName ?: "Manager",
            timestamp = System.currentTimeMillis(),
            notes = "New item added to catalog with initial stock of ${product.quantity}"
        )
        transactionDao.insertTransaction(tx)
        Result.success(Unit)
    }

    /**
     * Delete Product (Requires confirmation)
     */
    suspend fun deleteProduct(codeArticle: String): Result<Unit> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByCodeOrBarcode(codeArticle)
            ?: return@withContext Result.failure(Exception("Product not found"))

        productDao.deleteProductByCode(product.codeArticle)

        val user = _currentUser.value
        val tx = InventoryTransaction(
            codeArticle = product.codeArticle,
            productDescription = product.description,
            type = TransactionType.EXECUTION,
            quantityChange = -product.quantity,
            previousQuantity = product.quantity,
            newQuantity = 0,
            staffEmail = user?.email ?: "system",
            staffName = user?.fullName ?: "Staff",
            staffRole = user?.role?.displayName ?: "Admin",
            timestamp = System.currentTimeMillis(),
            notes = "Product removed from catalog: ${product.description}",
            executionReason = "Item Deleted / Catalog Purge"
        )
        transactionDao.insertTransaction(tx)
        Result.success(Unit)
    }

    // --- Excel Connection & CSV Import / Export ---

    /**
     * Connect an Excel / CSV file as the database
     */
    suspend fun importSpreadsheet(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file stream"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val importedProducts = mutableListOf<Product>()
            var line: String? = reader.readLine()
            var isFirstLine = true
            var delimiter = ","

            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    if (isFirstLine) {
                        isFirstLine = false
                        // Detect delimiter (comma, semicolon, tab)
                        if (trimmed.contains(";")) delimiter = ";"
                        else if (trimmed.contains("\t")) delimiter = "\t"
                        // Skip header if it contains known header words
                        if (trimmed.contains("Code", ignoreCase = true) ||
                            trimmed.contains("Description", ignoreCase = true) ||
                            trimmed.contains("Article", ignoreCase = true)) {
                            line = reader.readLine()
                            continue
                        }
                    }

                    val tokens = parseCsvLine(trimmed, delimiter)
                    if (tokens.size >= 4) {
                        val code = tokens.getOrNull(0)?.trim() ?: ""
                        val desc = tokens.getOrNull(1)?.trim() ?: "Item $code"
                        val ref = tokens.getOrNull(2)?.trim() ?: "REF-$code"
                        val storage = tokens.getOrNull(3)?.trim() ?: "Warehouse A"
                        val local = tokens.getOrNull(4)?.trim() ?: "Zone 1"
                        val emp = tokens.getOrNull(5)?.trim() ?: "Rack 01"
                        val qty = tokens.getOrNull(6)?.toIntOrNull() ?: 10
                        val barcode = tokens.getOrNull(7)?.trim()?.ifBlank { code } ?: code
                        val threshold = tokens.getOrNull(8)?.toIntOrNull() ?: 5
                        val price = tokens.getOrNull(9)?.toDoubleOrNull() ?: 0.0
                        val category = tokens.getOrNull(10)?.trim() ?: "General"

                        if (code.isNotBlank()) {
                            importedProducts.add(
                                Product(
                                    codeArticle = code,
                                    description = desc,
                                    reference = ref,
                                    storage = storage,
                                    local = local,
                                    emplacement = emp,
                                    quantity = qty,
                                    barcode = barcode,
                                    minThreshold = threshold,
                                    unitPrice = price,
                                    category = category
                                )
                            )
                        }
                    }
                }
                line = reader.readLine()
            }
            reader.close()

            if (importedProducts.isEmpty()) {
                return@withContext Result.failure(Exception("No valid product records found in file"))
            }

            productDao.insertProducts(importedProducts)
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_sheet.csv"
            _excelSyncState.value = ExcelSyncMetadata(
                connectedFileName = fileName,
                isConnected = true,
                totalItemsCount = productDao.getProductCount(),
                lastSyncTime = System.currentTimeMillis(),
                lastAction = "Imported ${importedProducts.size} items"
            )

            Result.success(importedProducts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        val delimChar = if (delimiter == ";") ';' else if (delimiter == "\t") '\t' else ','

        for (ch in line.toCharArray()) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == delimChar && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    /**
     * Export Inventory Report to Excel-compatible CSV file and return shareable URI
     */
    suspend fun exportInventoryReport(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val products = database.productDao().getAllProducts()
            // Pull snapshot from Flow
            var snapshot = listOf<Product>()
            // Query count and direct fetch
            val cursor = database.query("SELECT * FROM products ORDER BY codeArticle ASC", null)
            val list = mutableListOf<Product>()
            while (cursor.moveToNext()) {
                val code = cursor.getString(cursor.getColumnIndexOrThrow("codeArticle"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val ref = cursor.getString(cursor.getColumnIndexOrThrow("reference"))
                val storage = cursor.getString(cursor.getColumnIndexOrThrow("storage"))
                val local = cursor.getString(cursor.getColumnIndexOrThrow("local"))
                val emp = cursor.getString(cursor.getColumnIndexOrThrow("emplacement"))
                val qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val threshold = cursor.getInt(cursor.getColumnIndexOrThrow("minThreshold"))
                val barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode"))
                val cat = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("unitPrice"))
                list.add(
                    Product(
                        codeArticle = code,
                        description = desc,
                        reference = ref,
                        storage = storage,
                        local = local,
                        emplacement = emp,
                        quantity = qty,
                        minThreshold = threshold,
                        barcode = barcode,
                        category = cat,
                        unitPrice = price
                    )
                )
            }
            cursor.close()
            snapshot = list

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "Inventory_Report_${dateFormat.format(Date())}.csv"
            val file = File(reportsDir, fileName)

            // UTF-8 BOM so Microsoft Excel automatically opens with correct encoding and columns
            val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            val outputStream = file.outputStream()
            outputStream.write(utf8Bom)

            val writer = outputStream.bufferedWriter(Charsets.UTF_8)
            // Semicolon separator is standard for Excel in international and European formats
            writer.write("Code Article;Description;Reference;Storage;Local;Emplacement;Quantity;Threshold;Status;Unit Price;Valuation;Barcode;Category\n")

            for (p in snapshot) {
                val status = if (p.quantity <= 0) "OUT_OF_STOCK" else if (p.isLowStock) "LOW_STOCK" else "OPTIMAL"
                val valuation = p.quantity * p.unitPrice
                val line = "${escapeCsv(p.codeArticle)};${escapeCsv(p.description)};${escapeCsv(p.reference)};" +
                        "${escapeCsv(p.storage)};${escapeCsv(p.local)};${escapeCsv(p.emplacement)};" +
                        "${p.quantity};${p.minThreshold};$status;${String.format(Locale.US, "%.2f", p.unitPrice)};" +
                        "${String.format(Locale.US, "%.2f", valuation)};${escapeCsv(p.barcode)};${escapeCsv(p.category)}\n"
                writer.write(line)
            }
            writer.flush()
            writer.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            _excelSyncState.value = _excelSyncState.value.copy(
                lastSyncTime = System.currentTimeMillis(),
                lastAction = "Exported $fileName"
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    suspend fun resetDatabaseToDefault(): Result<Unit> = withContext(Dispatchers.IO) {
        productDao.deleteAllProducts()
        transactionDao.deleteAllTransactions()
        // Re-insert seed dataset
        val seedProducts = listOf(
            Product("ART-1001", "Disjoncteur differentiel 16A 30mA 1P+N Acti9", "REF-A9R11216", "Depot Central", "Local A1", "Rayon E03 - Bac 12", 24, 8, "ART-1001", "Electrical", 45.50),
            Product("ART-1002", "Contacteur modulaire 25A 2NO 230V TeSys", "REF-LC1D25BD", "Depot Central", "Local A1", "Rayon E03 - Bac 14", 4, 10, "ART-1002", "Electrical", 32.00),
            Product("ART-1003", "Relais thermique de surcharge 9-13A Classe 10", "REF-LRD16", "Hangar Nord", "Local B2", "Casier C-08", 15, 5, "ART-1003", "Electrical", 58.75),
            Product("ART-1004", "Roulement a billes rigide 6205-2RS1 C3 SKF", "REF-SKF-6205", "Atelier Mecanique", "Local M1", "Tiroir R-02", 2, 8, "ART-1004", "Mechanical", 14.20),
            Product("ART-1005", "Joint torique NBR 70 Shore 35x3mm haute etancheite", "REF-OR-3503", "Atelier Mecanique", "Local M1", "Tiroir J-15", 45, 15, "ART-1005", "Mechanical", 1.80),
            Product("ART-1006", "Verin pneumatique ISO 15552 Ø32 Course 100mm", "REF-FESTO-DSBC", "Zone Pneumatique", "Local P3", "Rayon P02 - Niv 1", 7, 4, "ART-1006", "Pneumatics", 89.00),
            Product("ART-1007", "Distributeur 5/2 bistable 24VDC G1/8 SY Series", "REF-SMC-SY5120", "Zone Pneumatique", "Local P3", "Rayon P02 - Niv 2", 1, 5, "ART-1007", "Pneumatics", 62.40),
            Product("ART-1008", "Capteur inductif M12 PNP NO portee 4mm IP68", "REF-XS612B1PAL2", "Magasin Outillage", "Local O4", "Bac Capteurs-01", 18, 6, "ART-1008", "Instrumentation", 38.90),
            Product("ART-1009", "Transmetteur de pression 0-10 bar 4-20mA G1/4", "REF-DANFOSS-MBS", "Magasin Outillage", "Local O4", "Bac Capteurs-03", 0, 3, "ART-1009", "Instrumentation", 145.00),
            Product("ART-1010", "Gants de protection anti-coupure Niveau D taille 9", "REF-UVE-60070", "Magasin Securite", "Local S1", "Armoire EPI-A", 32, 10, "ART-1010", "Safety", 9.50),
            Product("ART-1011", "Lunettes de protection integrale antibuee incolore", "REF-BOLLE-VIPER", "Magasin Securite", "Local S1", "Armoire EPI-B", 20, 10, "ART-1011", "Safety", 12.00),
            Product("ART-1012", "Cable blinde LiYCY 4x0.75mm2 bobine 100m", "REF-LAPP-0034504", "Depot Central", "Local A2", "Bobines B-01", 6, 3, "ART-1012", "Electrical", 110.00)
        )
        productDao.insertProducts(seedProducts)
        _excelSyncState.value = ExcelSyncMetadata(
            connectedFileName = "Default_Industrial_Catalog.xlsx",
            totalItemsCount = seedProducts.size,
            lastAction = "Reset catalog to factory template"
        )
        Result.success(Unit)
    }
}

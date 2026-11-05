package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.InventoryTransaction
import com.example.data.model.Product
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class, InventoryTransaction::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "inventory_manager_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: InventoryDatabase) {
            val userDao = database.userDao()
            val productDao = database.productDao()
            val txDao = database.transactionDao()

            // 1. Initial Users for all 3 roles
            val seedUsers = listOf(
                User(
                    email = "admin@inventory.com",
                    password = "admin",
                    fullName = "Alexandre Dupont",
                    role = UserRole.ADMIN,
                    department = "Central Operations & Engineering"
                ),
                User(
                    email = "manager@inventory.com",
                    password = "manager",
                    fullName = "Sarah Connor",
                    role = UserRole.MANAGER,
                    department = "Logistics & Supply Chain"
                ),
                User(
                    email = "tech@inventory.com",
                    password = "tech",
                    fullName = "Marc Lavigne",
                    role = UserRole.TECHNICIEN,
                    department = "Field Maintenance & Automation"
                )
            )
            userDao.insertUsers(seedUsers)

            // 2. Initial Comprehensive Product Catalog
            val seedProducts = listOf(
                Product(
                    codeArticle = "ART-1001",
                    description = "Disjoncteur differentiel 16A 30mA 1P+N Acti9",
                    reference = "REF-A9R11216",
                    storage = "Depot Central",
                    local = "Local A1",
                    emplacement = "Rayon E03 - Bac 12",
                    quantity = 24,
                    minThreshold = 8,
                    barcode = "ART-1001",
                    category = "Electrical",
                    unitPrice = 45.50
                ),
                Product(
                    codeArticle = "ART-1002",
                    description = "Contacteur modulaire 25A 2NO 230V TeSys",
                    reference = "REF-LC1D25BD",
                    storage = "Depot Central",
                    local = "Local A1",
                    emplacement = "Rayon E03 - Bac 14",
                    quantity = 4, // LOW STOCK
                    minThreshold = 10,
                    barcode = "ART-1002",
                    category = "Electrical",
                    unitPrice = 32.00
                ),
                Product(
                    codeArticle = "ART-1003",
                    description = "Relais thermique de surcharge 9-13A Classe 10",
                    reference = "REF-LRD16",
                    storage = "Hangar Nord",
                    local = "Local B2",
                    emplacement = "Casier C-08",
                    quantity = 15,
                    minThreshold = 5,
                    barcode = "ART-1003",
                    category = "Electrical",
                    unitPrice = 58.75
                ),
                Product(
                    codeArticle = "ART-1004",
                    description = "Roulement a billes rigide 6205-2RS1 C3 SKF",
                    reference = "REF-SKF-6205",
                    storage = "Atelier Mecanique",
                    local = "Local M1",
                    emplacement = "Tiroir R-02",
                    quantity = 2, // CRITICAL LOW
                    minThreshold = 8,
                    barcode = "ART-1004",
                    category = "Mechanical",
                    unitPrice = 14.20
                ),
                Product(
                    codeArticle = "ART-1005",
                    description = "Joint torique NBR 70 Shore 35x3mm haute etancheite",
                    reference = "REF-OR-3503",
                    storage = "Atelier Mecanique",
                    local = "Local M1",
                    emplacement = "Tiroir J-15",
                    quantity = 45,
                    minThreshold = 15,
                    barcode = "ART-1005",
                    category = "Mechanical",
                    unitPrice = 1.80
                ),
                Product(
                    codeArticle = "ART-1006",
                    description = "Verin pneumatique ISO 15552 Ø32 Course 100mm",
                    reference = "REF-FESTO-DSBC",
                    storage = "Zone Pneumatique",
                    local = "Local P3",
                    emplacement = "Rayon P02 - Niv 1",
                    quantity = 7,
                    minThreshold = 4,
                    barcode = "ART-1006",
                    category = "Pneumatics",
                    unitPrice = 89.00
                ),
                Product(
                    codeArticle = "ART-1007",
                    description = "Distributeur 5/2 bistable 24VDC G1/8 SY Series",
                    reference = "REF-SMC-SY5120",
                    storage = "Zone Pneumatique",
                    local = "Local P3",
                    emplacement = "Rayon P02 - Niv 2",
                    quantity = 1, // CRITICAL
                    minThreshold = 5,
                    barcode = "ART-1007",
                    category = "Pneumatics",
                    unitPrice = 62.40
                ),
                Product(
                    codeArticle = "ART-1008",
                    description = "Capteur inductif M12 PNP NO portee 4mm IP68",
                    reference = "REF-XS612B1PAL2",
                    storage = "Magasin Outillage",
                    local = "Local O4",
                    emplacement = "Bac Capteurs-01",
                    quantity = 18,
                    minThreshold = 6,
                    barcode = "ART-1008",
                    category = "Instrumentation",
                    unitPrice = 38.90
                ),
                Product(
                    codeArticle = "ART-1009",
                    description = "Transmetteur de pression 0-10 bar 4-20mA G1/4",
                    reference = "REF-DANFOSS-MBS",
                    storage = "Magasin Outillage",
                    local = "Local O4",
                    emplacement = "Bac Capteurs-03",
                    quantity = 0, // OUT OF STOCK
                    minThreshold = 3,
                    barcode = "ART-1009",
                    category = "Instrumentation",
                    unitPrice = 145.00
                ),
                Product(
                    codeArticle = "ART-1010",
                    description = "Gants de protection anti-coupure Niveau D taille 9",
                    reference = "REF-UVE-60070",
                    storage = "Magasin Securite",
                    local = "Local S1",
                    emplacement = "Armoire EPI-A",
                    quantity = 32,
                    minThreshold = 10,
                    barcode = "ART-1010",
                    category = "Safety",
                    unitPrice = 9.50
                ),
                Product(
                    codeArticle = "ART-1011",
                    description = "Lunettes de protection integrale antibuee incolore",
                    reference = "REF-BOLLE-VIPER",
                    storage = "Magasin Securite",
                    local = "Local S1",
                    emplacement = "Armoire EPI-B",
                    quantity = 20,
                    minThreshold = 10,
                    barcode = "ART-1011",
                    category = "Safety",
                    unitPrice = 12.00
                ),
                Product(
                    codeArticle = "ART-1012",
                    description = "Cable blinde LiYCY 4x0.75mm2 bobine 100m",
                    reference = "REF-LAPP-0034504",
                    storage = "Depot Central",
                    local = "Local A2",
                    emplacement = "Bobines B-01",
                    quantity = 6,
                    minThreshold = 3,
                    barcode = "ART-1012",
                    category = "Electrical",
                    unitPrice = 110.00
                )
            )
            productDao.insertProducts(seedProducts)

            // 3. Seed some realistic prior transactions to give history & trends right away
            val now = System.currentTimeMillis()
            val day = 86400000L
            val initialTransactions = listOf(
                InventoryTransaction(
                    codeArticle = "ART-1001",
                    productDescription = "Disjoncteur differentiel 16A 30mA 1P+N Acti9",
                    type = TransactionType.STOCK_IN,
                    quantityChange = 10,
                    previousQuantity = 14,
                    newQuantity = 24,
                    staffEmail = "manager@inventory.com",
                    staffName = "Sarah Connor",
                    staffRole = "Manager",
                    timestamp = now - (day * 2),
                    notes = "Monthly supplier restock delivery PO#8821"
                ),
                InventoryTransaction(
                    codeArticle = "ART-1004",
                    productDescription = "Roulement a billes rigide 6205-2RS1 C3 SKF",
                    type = TransactionType.STOCK_OUT,
                    quantityChange = -4,
                    previousQuantity = 6,
                    newQuantity = 2,
                    staffEmail = "tech@inventory.com",
                    staffName = "Marc Lavigne",
                    staffRole = "Technicien",
                    timestamp = now - (day * 1),
                    notes = "Conveyor pump motor P-102 overhaul"
                ),
                InventoryTransaction(
                    codeArticle = "ART-1007",
                    productDescription = "Distributeur 5/2 bistable 24VDC G1/8 SY Series",
                    type = TransactionType.EXECUTION,
                    quantityChange = -1,
                    previousQuantity = 2,
                    newQuantity = 1,
                    staffEmail = "tech@inventory.com",
                    staffName = "Marc Lavigne",
                    staffRole = "Technicien",
                    timestamp = now - (day * 1) + 3600000,
                    notes = "Coil burned during high voltage surge test",
                    executionReason = "Damaged during installation"
                ),
                InventoryTransaction(
                    codeArticle = "ART-1003",
                    productDescription = "Relais thermique de surcharge 9-13A Classe 10",
                    type = TransactionType.TRANSFER,
                    quantityChange = 0,
                    previousQuantity = 15,
                    newQuantity = 15,
                    staffEmail = "admin@inventory.com",
                    staffName = "Alexandre Dupont",
                    staffRole = "Admin",
                    timestamp = now - 1800000,
                    notes = "Relocated closer to production line maintenance workshop",
                    sourceLocation = "Depot Central > Local A1 > Rayon E02",
                    destLocation = "Hangar Nord > Local B2 > Casier C-08"
                )
            )
            for (tx in initialTransactions) {
                txDao.insertTransaction(tx)
            }
        }
    }
}

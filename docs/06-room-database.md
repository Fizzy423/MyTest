# 💾 Room Database - Локальная база данных

## 🎯 Что такое Room?

Room - это библиотека для работы с SQLite базой данных в Android. Она упрощает работу с БД и проверяет запросы на этапе компиляции.

## 🏗️ Три главных компонента

1. **Entity** - таблица (data class с аннотацией @Entity)
2. **DAO** - запросы к БД (interface с аннотациями)
3. **Database** - сама база данных (abstract class с аннотацией @Database)

## 📦 Подключение библиотеки

```kotlin
// build.gradle.kts (app)
plugins {
    id("org.jetbrains.kotlin.kapt")  // Важно!
}

dependencies {
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
}
```

## 📝 Шаг 1: Entity (таблица)

```kotlin
// data/Product.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val category: String
)
```

### Дополнительные аннотации

```kotlin
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]  // Уникальный email
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_name")  // Имя колонки в БД
    val name: String,
    
    val email: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @Ignore  // Не сохранять в БД
    var isSelected: Boolean = false
)
```

## 🔧 Шаг 2: DAO (Data Access Object)

```kotlin
// data/ProductDao.kt
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    // ========== SELECT ==========
    
    // Получить все продукты
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>
    
    // Получить все продукты как Flow (автообновление)
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>
    
    // Получить продукт по ID
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?
    
    // Поиск по имени
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<Product>
    
    // Фильтр по категории
    @Query("SELECT * FROM products WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<Product>
    
    // Фильтр по цене
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    suspend fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): List<Product>
    
    // Сортировка
    @Query("SELECT * FROM products ORDER BY price ASC")
    suspend fun getProductsSortedByPrice(): List<Product>
    
    // Количество
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
    
    // ========== INSERT ==========
    
    // Вставить один
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long  // Возвращает ID
    
    // Вставить несколько
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    // ========== UPDATE ==========
    
    // Обновить
    @Update
    suspend fun updateProduct(product: Product)
    
    // Обновить цену
    @Query("UPDATE products SET price = :newPrice WHERE id = :productId")
    suspend fun updatePrice(productId: Int, newPrice: Double)
    
    // ========== DELETE ==========
    
    // Удалить конкретный
    @Delete
    suspend fun deleteProduct(product: Product)
    
    // Удалить по ID
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Int)
    
    // Удалить все
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    // Удалить по категории
    @Query("DELETE FROM products WHERE category = :category")
    suspend fun deleteProductsByCategory(category: String)
}
```

## 🗄️ Шаг 3: Database

```kotlin
// data/AppDatabase.kt
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],  // Список всех Entity
    version = 1,                   // Версия БД
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"  // Имя файла БД
                )
                    .fallbackToDestructiveMigration()  // При изменении схемы - пересоздать БД
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

## 🚀 Использование в Activity

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var database: AppDatabase
    private lateinit var productDao: ProductDao
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Получить БД
        database = AppDatabase.getDatabase(this)
        productDao = database.productDao()
        
        // Работа с БД через корутины
        lifecycleScope.launch {
            // Вставить данные
            insertSampleData()
            
            // Получить данные
            val products = productDao.getAllProducts()
            adapter.items = products
        }
    }
    
    private suspend fun insertSampleData() {
        val products = listOf(
            Product(name = "Телефон", price = 50000.0, description = "...", imageUrl = "...", category = "Электроника"),
            Product(name = "Ноутбук", price = 80000.0, description = "...", imageUrl = "...", category = "Электроника"),
            Product(name = "Футболка", price = 1500.0, description = "...", imageUrl = "...", category = "Одежда")
        )
        productDao.insertProducts(products)
    }
}
```

## 🔄 Flow для автообновления

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Подписаться на изменения
        lifecycleScope.launch {
            productDao.getAllProductsFlow().collect { products ->
                // Автоматически вызывается при изменении БД
                adapter.items = products
            }
        }
    }
    
    private fun addProduct() {
        lifecycleScope.launch {
            val newProduct = Product(
                name = "Новый товар",
                price = 10000.0,
                description = "...",
                imageUrl = "...",
                category = "..."
            )
            productDao.insertProduct(newProduct)
            // RecyclerView обновится автоматически!
        }
    }
}
```

## 🔍 Сложные запросы

### JOIN

```kotlin
// Две Entity
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int
)

// Результат JOIN
data class ProductWithCategory(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)

// DAO
@Query("""
    SELECT * FROM products
    INNER JOIN categories ON products.categoryId = categories.id
""")
suspend fun getProductsWithCategories(): List<ProductWithCategory>
```

### Агрегация

```kotlin
@Dao
interface ProductDao {
    
    // Средняя цена
    @Query("SELECT AVG(price) FROM products")
    suspend fun getAveragePrice(): Double
    
    // Сумма цен
    @Query("SELECT SUM(price) FROM products")
    suspend fun getTotalPrice(): Double
    
    // Максимальная цена
    @Query("SELECT MAX(price) FROM products")
    suspend fun getMaxPrice(): Double
    
    // Минимальная цена
    @Query("SELECT MIN(price) FROM products")
    suspend fun getMinPrice(): Double
    
    // Количество по категориям
    @Query("SELECT category, COUNT(*) as count FROM products GROUP BY category")
    suspend fun getCountByCategory(): List<CategoryCount>
}

data class CategoryCount(
    val category: String,
    val count: Int
)
```

## 🔄 Миграция БД

```kotlin
// При изменении схемы (добавили новую колонку)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE products ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
    }
}

// Создание БД с миграцией
val database = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .addMigrations(MIGRATION_1_2)
    .build()
```

## 💾 TypeConverter (для сложных типов)

```kotlin
// Для Date
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // Для List<String>
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return value.split(",")
    }
    
    @TypeConverter
    fun toStringList(list: List<String>): String {
        return list.joinToString(",")
    }
}

// Добавить в Database
@Database(...)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // ...
}

// Использование
@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val createdAt: Date,
    val tags: List<String>
)
```

## 🎯 Repository Pattern

```kotlin
// data/ProductRepository.kt
class ProductRepository(private val productDao: ProductDao) {
    
    // Flow для автообновления
    val allProducts: Flow<List<Product>> = productDao.getAllProductsFlow()
    
    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }
    
    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }
    
    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }
    
    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }
    
    suspend fun searchProducts(query: String): List<Product> {
        return productDao.searchProducts(query)
    }
}

// Использование в Activity
class MainActivity : AppCompatActivity() {
    
    private lateinit var repository: ProductRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        repository = ProductRepository(database.productDao())
        
        // Подписаться на изменения
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                adapter.items = products
            }
        }
    }
    
    private fun addProduct() {
        lifecycleScope.launch {
            val product = Product(...)
            repository.insertProduct(product)
        }
    }
}
```

## ⚠️ Частые ошибки

### 1. Работа с БД в главном потоке

```kotlin
// ❌ ОШИБКА
val products = productDao.getAllProducts()  // Crash!

// ✅ ПРАВИЛЬНО
lifecycleScope.launch {
    val products = productDao.getAllProducts()
}
```

### 2. Забыли suspend

```kotlin
// ❌ ОШИБКА
@Query("SELECT * FROM products")
fun getAllProducts(): List<Product>  // Нет suspend!

// ✅ ПРАВИЛЬНО
@Query("SELECT * FROM products")
suspend fun getAllProducts(): List<Product>
```

### 3. Не закрыли БД

```kotlin
// Обычно не нужно закрывать, но если требуется:
override fun onDestroy() {
    super.onDestroy()
    database.close()
}
```

---

**Совет**: Используй Flow для автообновления UI при изменении БД!

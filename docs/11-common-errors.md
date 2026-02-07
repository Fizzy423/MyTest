# ⚠️ Частые ошибки и их решения

## 🔴 Компиляция и запуск

### 1. Не запускается приложение

**Ошибка:**
```
Invoke-customs are only supported starting with Android O (--min-api 26)
```

**Решение:**
```kotlin
// build.gradle.kts (app)
android {
    defaultConfig {
        minSdk = 24  // Или выше
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

### 2. ViewBinding не работает

**Ошибка:**
```
Unresolved reference: ActivityMainBinding
```

**Решение:**
```kotlin
// 1. Включить ViewBinding в build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
    }
}

// 2. Синхронизировать Gradle
// File → Sync Project with Gradle Files

// 3. Очистить проект
// Build → Clean Project
// Build → Rebuild Project
```

### 3. Проблемы с версиями библиотек

**Ошибка:**
```
Duplicate class found
```

**Решение:**
```kotlin
// Убедись, что версии совместимы
dependencies {
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")  // Одинаковая версия!
}
```

## 🌐 Сеть (Retrofit)

### 1. Забыли разрешение INTERNET

**Ошибка:**
```
java.net.UnknownServiceException: CLEARTEXT communication not permitted
```

**Решение:**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Если API использует HTTP (не HTTPS) -->
<application
    android:usesCleartextTraffic="true"
    ...>
```

### 2. Запрос в главном потоке

**Ошибка:**
```
android.os.NetworkOnMainThreadException
```

**Решение:**
```kotlin
// ❌ НЕПРАВИЛЬНО
val products = apiService.getProducts()

// ✅ ПРАВИЛЬНО
lifecycleScope.launch {
    val products = apiService.getProducts()
}
```

### 3. Неправильный BASE_URL

**Ошибка:**
```
IllegalArgumentException: baseUrl must end in /
```

**Решение:**
```kotlin
// ❌ НЕПРАВИЛЬНО
private const val BASE_URL = "https://api.example.com"

// ✅ ПРАВИЛЬНО
private const val BASE_URL = "https://api.example.com/"
```

### 4. JSON не парсится

**Ошибка:**
```
Expected BEGIN_OBJECT but was STRING
```

**Решение:**
```kotlin
// Проверь структуру JSON и модели данных

// JSON:
// {"id": 1, "name": "Test"}

// Модель должна совпадать:
data class Product(
    val id: Int,
    val name: String
)

// Если имена отличаются:
data class Product(
    @SerializedName("product_id")
    val id: Int,
    
    @SerializedName("product_name")
    val name: String
)
```

## 💾 База данных (Room)

### 1. Забыли suspend

**Ошибка:**
```
Cannot access database on the main thread
```

**Решение:**
```kotlin
// ❌ НЕПРАВИЛЬНО
@Query("SELECT * FROM products")
fun getAllProducts(): List<Product>

// ✅ ПРАВИЛЬНО
@Query("SELECT * FROM products")
suspend fun getAllProducts(): List<Product>

// Или используй Flow
@Query("SELECT * FROM products")
fun getAllProducts(): Flow<List<Product>>
```

### 2. Забыли kapt

**Ошибка:**
```
Cannot find implementation for database
```

**Решение:**
```kotlin
// build.gradle.kts (app)
plugins {
    id("org.jetbrains.kotlin.kapt")  // Добавь это!
}

dependencies {
    kapt("androidx.room:room-compiler:2.6.1")  // kapt, не implementation!
}
```

### 3. Не обновляется схема БД

**Ошибка:**
```
Room cannot verify the data integrity
```

**Решение:**
```kotlin
// Вариант 1: Увеличить версию и добавить миграцию
@Database(entities = [Product::class], version = 2)

// Вариант 2: Пересоздать БД (для разработки)
Room.databaseBuilder(...)
    .fallbackToDestructiveMigration()  // Удалит старую БД
    .build()
```

## ♻️ RecyclerView

### 1. Пустой экран

**Проблема:**
RecyclerView ничего не показывает

**Решение:**
```kotlin
// Проверь:
// 1. LayoutManager установлен?
binding.rvProducts.layoutManager = LinearLayoutManager(this)

// 2. Adapter привязан?
binding.rvProducts.adapter = adapter

// 3. Данные установлены?
adapter.items = products
Log.d("TAG", "Products count: ${products.size}")

// 4. ViewHolder правильно создаётся?
override fun onCreateViewHolder(...): ViewHolder {
    val binding = ItemProductBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,  // ВАЖНО: parent, а не null!
        false    // ВАЖНО: false, а не true!
    )
    return ViewHolder(binding)
}
```

### 2. Клик не работает

**Проблема:**
Нажатие на элемент не реагирует

**Решение:**
```kotlin
// Установи слушатель в bind(), а не в onCreateViewHolder()

inner class ViewHolder(...) : RecyclerView.ViewHolder(...) {
    fun bind(product: Product) {
        // ✅ ПРАВИЛЬНО - в bind()
        binding.root.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }
}

// ❌ НЕПРАВИЛЬНО - в onCreateViewHolder()
override fun onCreateViewHolder(...): ViewHolder {
    val holder = ViewHolder(binding)
    holder.itemView.setOnClickListener { /* Не сработает правильно */ }
    return holder
}
```

## 🧵 Coroutines

### 1. Корутина не отменяется

**Проблема:**
Утечка памяти при выходе из Activity

**Решение:**
```kotlin
// ❌ НЕПРАВИЛЬНО
GlobalScope.launch {
    // Продолжит работу после уничтожения Activity
}

// ✅ ПРАВИЛЬНО
lifecycleScope.launch {
    // Автоматически отменится при destroy
}
```

### 2. Обновление UI из фонового потока

**Ошибка:**
```
Only the original thread that created a view hierarchy can touch its views
```

**Решение:**
```kotlin
lifecycleScope.launch {
    // Загрузка в IO потоке
    val data = withContext(Dispatchers.IO) {
        loadData()
    }
    
    // Обновление UI в Main потоке (автоматически)
    textView.text = data
}

// Или явно:
lifecycleScope.launch(Dispatchers.IO) {
    val data = loadData()
    
    withContext(Dispatchers.Main) {
        textView.text = data
    }
}
```

### 3. CancellationException

**Ошибка:**
```
Job was cancelled
```

**Решение:**
```kotlin
// Это нормально! Корутина была отменена.
// Не нужно обрабатывать CancellationException

lifecycleScope.launch {
    try {
        loadData()
    } catch (e: CancellationException) {
        throw e  // Пробросить дальше
    } catch (e: Exception) {
        // Обрабатывать другие ошибки
    }
}
```

## 🎨 UI и Layout

### 1. Элемент не отображается

**Проблема:**
View есть в XML, но не видно на экране

**Решение:**
```kotlin
// Проверь visibility
binding.textView.visibility = View.VISIBLE  // Не GONE или INVISIBLE

// Проверь размеры
// ❌ НЕПРАВИЛЬНО
android:layout_width="0dp"
android:layout_height="0dp"

// ✅ ПРАВИЛЬНО (для ConstraintLayout)
android:layout_width="0dp"
android:layout_height="0dp"
app:layout_constraintTop_toTopOf="parent"
app:layout_constraintBottom_toBottomOf="parent"
```

### 2. ConstraintLayout не работает

**Проблема:**
Элементы наложились друг на друга

**Решение:**
```xml
<!-- Каждый элемент должен иметь constraints -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintBottom_toBottomOf="parent" />
```

### 3. RecyclerView не прокручивается

**Проблема:**
Список не скроллится

**Решение:**
```xml
<!-- Убедись, что RecyclerView имеет фиксированную высоту -->
<androidx.recyclerview.widget.RecyclerView
    android:layout_width="match_parent"
    android:layout_height="match_parent" />  <!-- Не wrap_content! -->

<!-- Или в LinearLayout -->
<LinearLayout
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <TextView ... />
    
    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />  <!-- Займёт оставшееся место -->
</LinearLayout>
```

## 🔧 Kotlin

### 1. NullPointerException

**Ошибка:**
```
kotlin.KotlinNullPointerException
```

**Решение:**
```kotlin
// ❌ ОПАСНО
val name: String? = null
val length = name.length  // Crash!

// ✅ БЕЗОПАСНО
val length = name?.length  // null, если name == null
val length = name?.length ?: 0  // 0, если name == null
val length = name!!.length  // Crash, если name == null (используй редко!)

// Проверка
if (name != null) {
    val length = name.length  // Теперь безопасно
}

// let
name?.let {
    val length = it.length  // Выполнится только если name != null
}
```

### 2. lateinit не инициализирован

**Ошибка:**
```
UninitializedPropertyAccessException: lateinit property has not been initialized
```

**Решение:**
```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализировать до использования!
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Теперь можно использовать
        binding.textView.text = "Hello"
    }
    
    // Проверка инициализации
    fun isBindingInitialized() = ::binding.isInitialized
}
```

### 3. Type mismatch

**Ошибка:**
```
Type mismatch: inferred type is List<Product?> but List<Product> was expected
```

**Решение:**
```kotlin
// ❌ НЕПРАВИЛЬНО
val products: List<Product> = listOf(null, Product(...))

// ✅ ПРАВИЛЬНО
val products: List<Product?> = listOf(null, Product(...))

// Или фильтруй null
val products: List<Product> = listOfNotNull(null, Product(...))
```

## 📱 Android Manifest

### 1. Activity не зарегистрирована

**Ошибка:**
```
Unable to find explicit activity class
```

**Решение:**
```xml
<!-- AndroidManifest.xml -->
<application ...>
    
    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    
    <!-- Добавь все свои Activity! -->
    <activity
        android:name=".DetailActivity"
        android:exported="false" />
    
</application>
```

## 🐛 Отладка

### Полезные команды

```kotlin
// Логирование
Log.d("TAG", "Debug message")
Log.e("TAG", "Error message")
Log.i("TAG", "Info message")

// Проверка типа
if (value is String) {
    println("It's a String: $value")
}

// Breakpoint в коде
println("Check value: $value")  // Поставь breakpoint здесь

// Вывод стека вызовов
Thread.currentThread().stackTrace.forEach {
    println(it)
}

// Проверка потока
println("Current thread: ${Thread.currentThread().name}")
```

---

**Совет**: Когда получаешь ошибку, ЧИТАЙ сообщение полностью! В нём часто есть подсказка!

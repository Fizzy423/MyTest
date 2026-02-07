# ⚡ Coroutines - Асинхронное программирование

## 🎯 Что такое Coroutines?

Coroutines (корутины) - это легковесные потоки в Kotlin, которые позволяют выполнять асинхронный код просто и понятно.

## 📦 Подключение

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

## 🚀 Основы

### suspend функции

```kotlin
// suspend - функция может быть приостановлена и возобновлена
suspend fun loadData(): String {
    delay(1000)  // Имитация загрузки (не блокирует поток!)
    return "Data loaded"
}

// suspend функцию можно вызвать только из другой suspend функции или корутины
suspend fun processData() {
    val data = loadData()  // Ждём результат
    println(data)
}
```

### Запуск корутин

```kotlin
// 1. launch - запустить корутину (ничего не возвращает)
lifecycleScope.launch {
    val data = loadData()
    println(data)
}

// 2. async - запустить корутину (возвращает Deferred<T>)
lifecycleScope.launch {
    val deferred = async { loadData() }
    val data = deferred.await()  // Получить результат
    println(data)
}

// 3. withContext - сменить контекст выполнения
lifecycleScope.launch {
    val data = withContext(Dispatchers.IO) {
        loadData()  // Выполнится в IO потоке
    }
    // Здесь обратно в Main потоке
    println(data)
}
```

## 🔀 Dispatchers (диспетчеры)

```kotlin
// Main - главный поток (UI)
lifecycleScope.launch(Dispatchers.Main) {
    // Обновление UI
    textView.text = "Loading..."
}

// IO - операции ввода/вывода (сеть, БД)
lifecycleScope.launch(Dispatchers.IO) {
    val data = loadFromNetwork()
    val dbData = database.getData()
}

// Default - вычисления (сортировка, парсинг JSON)
lifecycleScope.launch(Dispatchers.Default) {
    val result = heavyCalculation()
}

// Unconfined - не привязан к потоку (редко используется)
lifecycleScope.launch(Dispatchers.Unconfined) {
    // ...
}
```

## 🎯 Scopes (области видимости)

### lifecycleScope (в Activity/Fragment)

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Автоматически отменяется при уничтожении Activity
        lifecycleScope.launch {
            val data = loadData()
            updateUI(data)
        }
    }
}
```

### viewModelScope (в ViewModel)

```kotlin
class MainViewModel : ViewModel() {
    
    fun loadData() {
        // Автоматически отменяется при очистке ViewModel
        viewModelScope.launch {
            val data = repository.getData()
            _uiState.value = data
        }
    }
}
```

### GlobalScope (НЕ рекомендуется!)

```kotlin
// ❌ Плохо - живёт всё время работы приложения
GlobalScope.launch {
    // ...
}

// ✅ Хорошо - используй lifecycleScope или viewModelScope
lifecycleScope.launch {
    // ...
}
```

### Создание своего Scope

```kotlin
class MyRepository {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun loadData() {
        scope.launch {
            val data = fetchData()
        }
    }
    
    fun cleanup() {
        scope.cancel()  // Отменить все корутины
    }
}
```

## 🔄 Примеры использования

### Загрузка данных из сети

```kotlin
private fun loadProducts() {
    binding.progressBar.visibility = View.VISIBLE
    
    lifecycleScope.launch {
        try {
            // Переключаемся на IO поток
            val products = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getProducts()
            }
            
            // Возвращаемся на Main поток
            adapter.items = products
            binding.rvProducts.visibility = View.VISIBLE
            
        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity,
                "Ошибка: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }
}
```

### Параллельное выполнение

```kotlin
lifecycleScope.launch {
    // Запустить параллельно
    val productsDeferred = async { apiService.getProducts() }
    val categoriesDeferred = async { apiService.getCategories() }
    
    // Дождаться обоих результатов
    val products = productsDeferred.await()
    val categories = categoriesDeferred.await()
    
    // Оба результата готовы
    updateUI(products, categories)
}
```

### Последовательное выполнение

```kotlin
lifecycleScope.launch {
    // Сначала одно
    val user = apiService.getUser()
    
    // Потом другое (зависит от первого)
    val orders = apiService.getUserOrders(user.id)
    
    // Обновить UI
    updateUI(user, orders)
}
```

### Периодическое выполнение

```kotlin
lifecycleScope.launch {
    while (isActive) {  // isActive - корутина не отменена
        val data = loadData()
        updateUI(data)
        delay(5000)  // Ждём 5 секунд
    }
}
```

### Таймаут

```kotlin
lifecycleScope.launch {
    try {
        withTimeout(5000) {  // Максимум 5 секунд
            val data = loadData()
            updateUI(data)
        }
    } catch (e: TimeoutCancellationException) {
        Toast.makeText(this@MainActivity, "Превышено время ожидания", Toast.LENGTH_SHORT).show()
    }
}
```

## 🛑 Отмена корутин

### Автоматическая отмена

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Отменится автоматически при destroy
        lifecycleScope.launch {
            loadData()
        }
    }
}
```

### Ручная отмена

```kotlin
class MainActivity : AppCompatActivity() {
    
    private var loadingJob: Job? = null
    
    private fun startLoading() {
        loadingJob = lifecycleScope.launch {
            while (isActive) {
                loadData()
                delay(1000)
            }
        }
    }
    
    private fun stopLoading() {
        loadingJob?.cancel()  // Остановить загрузку
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLoading()
    }
}
```

### Проверка отмены

```kotlin
lifecycleScope.launch {
    repeat(100) { i ->
        if (!isActive) return@launch  // Выйти если отменено
        
        // Тяжелая работа
        processItem(i)
    }
}
```

## 🔄 Flow - Поток данных

### Создание Flow

```kotlin
// Простой Flow
fun numbersFlow(): Flow<Int> = flow {
    repeat(5) { i ->
        delay(1000)
        emit(i)  // Отправить значение
    }
}

// Flow из коллекции
val numbersFlow = listOf(1, 2, 3, 4, 5).asFlow()

// Flow из одного значения
val singleFlow = flowOf(42)
```

### Подписка на Flow

```kotlin
lifecycleScope.launch {
    numbersFlow().collect { value ->
        println("Received: $value")
    }
}
```

### Операторы Flow

```kotlin
lifecycleScope.launch {
    numbersFlow()
        .map { it * 2 }              // Преобразовать
        .filter { it > 5 }           // Фильтровать
        .take(3)                     // Взять первые 3
        .collect { value ->
            println(value)
        }
}

// Обработка ошибок
numbersFlow()
    .catch { e ->
        println("Error: $e")
        emit(-1)  // Отправить значение по умолчанию
    }
    .collect { value ->
        println(value)
    }

// onEach - выполнить для каждого
numbersFlow()
    .onEach { println("Processing: $it") }
    .collect()
```

### Flow в Room

```kotlin
// DAO
@Query("SELECT * FROM products")
fun getAllProductsFlow(): Flow<List<Product>>

// Activity
lifecycleScope.launch {
    productDao.getAllProductsFlow().collect { products ->
        // Автоматически обновляется при изменении БД
        adapter.items = products
    }
}
```

### StateFlow и SharedFlow

```kotlin
class MainViewModel : ViewModel() {
    
    // StateFlow - хранит текущее состояние
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products
    
    fun loadProducts() {
        viewModelScope.launch {
            val data = repository.getProducts()
            _products.value = data  // Обновить состояние
        }
    }
}

// Activity
lifecycleScope.launch {
    viewModel.products.collect { products ->
        adapter.items = products
    }
}

// SharedFlow - может иметь несколько подписчиков
private val _events = MutableSharedFlow<Event>()
val events: SharedFlow<Event> = _events

fun sendEvent(event: Event) {
    viewModelScope.launch {
        _events.emit(event)
    }
}
```

## ⚠️ Обработка ошибок

### try-catch

```kotlin
lifecycleScope.launch {
    try {
        val data = loadData()
        updateUI(data)
    } catch (e: IOException) {
        showError("Нет интернета")
    } catch (e: HttpException) {
        showError("Ошибка сервера")
    } catch (e: Exception) {
        showError("Неизвестная ошибка: ${e.message}")
    }
}
```

### CoroutineExceptionHandler

```kotlin
val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("Caught exception: $exception")
}

lifecycleScope.launch(exceptionHandler) {
    // Код, который может выбросить исключение
    loadData()
}
```

### supervisorScope

```kotlin
lifecycleScope.launch {
    supervisorScope {
        // Ошибка в одной корутине не отменит другие
        launch {
            loadProducts()  // Может упасть
        }
        launch {
            loadCategories()  // Продолжит работу
        }
    }
}
```

## 🎯 Практические примеры

### Поиск с задержкой (debounce)

```kotlin
class MainActivity : AppCompatActivity() {
    
    private var searchJob: Job? = null
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            searchJob?.cancel()  // Отменить предыдущий поиск
            
            searchJob = lifecycleScope.launch {
                delay(300)  // Подождать 300мс после последнего ввода
                val query = text.toString()
                if (query.isNotEmpty()) {
                    searchProducts(query)
                }
            }
        }
    }
    
    private suspend fun searchProducts(query: String) {
        val products = withContext(Dispatchers.IO) {
            repository.searchProducts(query)
        }
        adapter.items = products
    }
}
```

### Retry (повтор при ошибке)

```kotlin
suspend fun <T> retry(
    times: Int = 3,
    delay: Long = 1000,
    block: suspend () -> T
): T {
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            delay(delay)
        }
    }
    return block()  // Последняя попытка
}

// Использование
lifecycleScope.launch {
    try {
        val data = retry(times = 3, delay = 2000) {
            apiService.getProducts()
        }
        updateUI(data)
    } catch (e: Exception) {
        showError("Не удалось загрузить после 3 попыток")
    }
}
```

---

**Совет**: Всегда используй lifecycleScope в Activity/Fragment для автоматической отмены!

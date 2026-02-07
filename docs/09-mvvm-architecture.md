# 🏛️ MVVM и Architecture Components

## 🎯 Что такое MVVM?

**MVVM (Model-View-ViewModel)** - это архитектурный паттерн, который разделяет код на три слоя:

- **Model** - данные (Room, Retrofit, Repository)
- **View** - UI (Activity, Fragment)
- **ViewModel** - связующее звено, бизнес-логика

## 📦 Подключение ViewModel

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
}
```

## 🏗️ Структура MVVM проекта

```
app/src/main/java/com/example/myapp/
├── data/                    # Слой данных
│   ├── local/              # Локальные данные
│   │   ├── AppDatabase.kt
│   │   └── ProductDao.kt
│   ├── remote/             # Сетевые данные
│   │   └── ApiService.kt
│   ├── repository/         # Репозитории
│   │   └── ProductRepository.kt
│   └── model/              # Модели данных
│       └── Product.kt
├── ui/                      # UI слой
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   └── ProductAdapter.kt
│   └── detail/
│       ├── DetailActivity.kt
│       └── DetailViewModel.kt
└── util/                    # Утилиты
    └── Resource.kt
```

## 📝 Model (Модель данных)

```kotlin
// data/model/Product.kt
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val image: String
)
```

## 🗄️ Repository (Репозиторий)

```kotlin
// data/repository/ProductRepository.kt
class ProductRepository(
    private val apiService: ApiService,
    private val productDao: ProductDao
) {
    
    // Flow из БД для автообновления
    val allProducts: Flow<List<Product>> = productDao.getAllProductsFlow()
    
    // Загрузить данные из сети и сохранить в БД
    suspend fun refreshProducts() {
        try {
            val products = apiService.getProducts()
            productDao.insertProducts(products)
        } catch (e: Exception) {
            throw e
        }
    }
    
    // Получить продукт по ID
    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }
    
    // Поиск
    suspend fun searchProducts(query: String): List<Product> {
        return productDao.searchProducts(query)
    }
}
```

## 🎨 ViewModel (ViewModel)

### Базовый ViewModel

```kotlin
// ui/main/MainViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // Список продуктов
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadProducts()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.refreshProducts()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchProducts(query)
                // Обновить UI
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Search error")
            }
        }
    }
    
    // UI States
    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
```

### ViewModel с ViewModelFactory

```kotlin
// ui/main/MainViewModelFactory.kt
class MainViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## 📱 View (Activity/Fragment)

```kotlin
// ui/main/MainActivity.kt
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()
    
    // Создать ViewModel
    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(
            RetrofitClient.apiService,
            database.productDao()
        )
        MainViewModelFactory(repository)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        observeData()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }
    
    private fun observeData() {
        // Наблюдать за продуктами
        lifecycleScope.launch {
            viewModel.products.collect { products ->
                adapter.items = products
                updateEmptyState(products.isEmpty())
            }
        }
        
        // Наблюдать за состоянием UI
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainViewModel.UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.errorView.visibility = View.GONE
                    }
                    is MainViewModel.UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorView.visibility = View.GONE
                    }
                    is MainViewModel.UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorView.visibility = View.VISIBLE
                        binding.tvError.text = state.message
                    }
                }
            }
        }
    }
    
    private fun setupListeners() {
        // Обновить данные
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProducts()
            binding.swipeRefresh.isRefreshing = false
        }
        
        // Клик на элемент
        adapter.onItemClick = { product ->
            openProductDetail(product.id)
        }
        
        // Поиск
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchProducts(query)
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvProducts.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvProducts.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }
    
    private fun openProductDetail(productId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        startActivity(intent)
    }
}
```

## 🎯 UI State Pattern

```kotlin
// util/UiState.kt
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// В ViewModel
class MainViewModel(...) : ViewModel() {
    
    private val _products = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val products: StateFlow<UiState<List<Product>>> = _products
    
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = UiState.Loading
            try {
                val data = repository.getProducts()
                _products.value = UiState.Success(data)
            } catch (e: Exception) {
                _products.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// В Activity
lifecycleScope.launch {
    viewModel.products.collect { state ->
        when (state) {
            is UiState.Loading -> showLoading()
            is UiState.Success -> showData(state.data)
            is UiState.Error -> showError(state.message)
        }
    }
}
```

## 🔄 Resource Pattern (альтернативный)

```kotlin
// util/Resource.kt
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

// В Repository
suspend fun getProducts(): Resource<List<Product>> {
    return try {
        val products = apiService.getProducts()
        Resource.Success(products)
    } catch (e: IOException) {
        Resource.Error("No internet connection")
    } catch (e: HttpException) {
        Resource.Error("Server error: ${e.code()}")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error")
    }
}
```

## 🎨 LiveData (альтернатива StateFlow)

```kotlin
class MainViewModel(...) : ViewModel() {
    
    // LiveData
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    fun loadProducts() {
        viewModelScope.launch {
            val data = repository.getProducts()
            _products.value = data  // Обновить LiveData
        }
    }
}

// В Activity
viewModel.products.observe(this) { products ->
    adapter.items = products
}
```

## 🧪 SavedStateHandle (сохранение состояния)

```kotlin
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {
    
    // Получить аргумент
    private val productId: Int = savedStateHandle["PRODUCT_ID"] ?: -1
    
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product
    
    init {
        loadProduct()
    }
    
    private fun loadProduct() {
        viewModelScope.launch {
            _product.value = repository.getProductById(productId)
        }
    }
}
```

## 🎯 Полный пример MVVM

### Data Layer

```kotlin
// Data class
data class Product(val id: Int, val title: String, val price: Double)

// API
interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
}

// DAO
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
}

// Repository
class ProductRepository(
    private val api: ApiService,
    private val dao: ProductDao
) {
    val products: Flow<List<Product>> = dao.getAllProductsFlow()
    
    suspend fun refreshProducts() {
        val products = api.getProducts()
        dao.insertProducts(products)
    }
}
```

### ViewModel Layer

```kotlin
class MainViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    val products = repository.products
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshProducts()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### View Layer

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val viewModel: MainViewModel by viewModels { /* factory */ }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            viewModel.products.collect { products ->
                adapter.items = products
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
        }
    }
}
```

## 💡 Преимущества MVVM

1. **Разделение ответственности** - каждый слой отвечает за своё
2. **Тестируемость** - ViewModel можно тестировать без Android
3. **Переиспользование** - ViewModel можно использовать в разных Activity/Fragment
4. **Сохранение состояния** - ViewModel переживает поворот экрана

---

**Совет**: Используй MVVM для сложных приложений, простые экраны можно делать без него!

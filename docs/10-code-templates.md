# 📋 Готовые шаблоны кода для конкурса

## 🚀 Быстрый старт проекта

### Минимальный проект с RecyclerView

```kotlin
// 1. data/Product.kt
data class Product(
    val id: Int,
    val name: String,
    val price: Double
)

// 2. ui/ProductAdapter.kt
class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    
    var items: List<Product> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    
    var onItemClick: ((Product) -> Unit)? = null
    
    inner class ViewHolder(private val binding: ItemProductBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "${product.price} ₽"
            binding.root.setOnClickListener { onItemClick?.invoke(product) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
}

// 3. MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
        
        adapter.items = listOf(
            Product(1, "Товар 1", 1000.0),
            Product(2, "Товар 2", 2000.0),
            Product(3, "Товар 3", 3000.0)
        )
        
        adapter.onItemClick = { product ->
            Toast.makeText(this, product.name, Toast.LENGTH_SHORT).show()
        }
    }
}
```

## 🌐 Retrofit + Room + MVVM (полный шаблон)

### 1. Модели данных

```kotlin
// domain/Product.kt
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: Int,
    val title: String,
    val price: Double,
    val image: String
)
```

### 2. API Service

```kotlin
// data/ApiService.kt
interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product
}

// data/RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "https://fakestoreapi.com/"
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### 3. Database

```kotlin
// data/ProductDao.kt
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

// data/AppDatabase.kt
@Database(entities = [Product::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
```

### 4. Repository

```kotlin
// data/ProductRepository.kt
class ProductRepository(
    private val api: ApiService,
    private val dao: ProductDao
) {
    val products: Flow<List<Product>> = dao.getAllProductsFlow()
    
    suspend fun refreshProducts() {
        try {
            val products = api.getProducts()
            dao.deleteAll()
            dao.insertProducts(products)
        } catch (e: Exception) {
            throw e
        }
    }
}
```

### 5. ViewModel

```kotlin
// ui/MainViewModel.kt
class MainViewModel(private val repository: ProductRepository) : ViewModel() {
    
    val products = repository.products
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    init {
        refresh()
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshProducts()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ui/MainViewModelFactory.kt
class MainViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
```

### 6. Activity

```kotlin
// ui/MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()
    
    private val viewModel: MainViewModel by viewModels {
        val db = AppDatabase.getDatabase(this)
        val repo = ProductRepository(RetrofitClient.apiService, db.productDao())
        MainViewModelFactory(repo)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeData()
    }
    
    private fun setupUI() {
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            viewModel.products.collect { products ->
                adapter.items = products
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
                binding.progressBar.isVisible = isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

## 🎨 UI Шаблоны

### SwipeRefreshLayout

```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvProducts"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

### Пустой список + ошибка + загрузка

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvProducts"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <TextView
        android:id="@+id/tvEmpty"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="Список пуст"
        android:visibility="gone" />
    
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />
    
    <LinearLayout
        android:id="@+id/errorView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:orientation="vertical"
        android:visibility="gone">
        
        <TextView
            android:id="@+id/tvError"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Ошибка загрузки" />
        
        <Button
            android:id="@+id/btnRetry"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Повторить" />
    </LinearLayout>
    
</FrameLayout>
```

## 🔍 Поиск в списке

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val allProducts = mutableListOf<Product>()
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtered = if (query.isEmpty()) {
                allProducts
            } else {
                allProducts.filter { 
                    it.name.lowercase().contains(query) 
                }
            }
            adapter.items = filtered
        }
    }
}
```

## 📊 Сортировка и фильтрация

```kotlin
fun sortProducts(products: List<Product>, sortType: SortType): List<Product> {
    return when (sortType) {
        SortType.NAME_ASC -> products.sortedBy { it.name }
        SortType.NAME_DESC -> products.sortedByDescending { it.name }
        SortType.PRICE_ASC -> products.sortedBy { it.price }
        SortType.PRICE_DESC -> products.sortedByDescending { it.price }
    }
}

enum class SortType {
    NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC
}

// Использование
binding.btnSort.setOnClickListener {
    val sorted = sortProducts(adapter.items, SortType.PRICE_ASC)
    adapter.items = sorted
}
```

## 🎯 Навигация между экранами

```kotlin
// Открыть детальный экран
fun openDetail(productId: Int) {
    val intent = Intent(this, DetailActivity::class.java)
    intent.putExtra("PRODUCT_ID", productId)
    startActivity(intent)
}

// Получить данные в DetailActivity
class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        loadProduct(productId)
    }
}
```

## ⚡ Debounce для поиска

```kotlin
private var searchJob: Job? = null

private fun setupSearch() {
    binding.etSearch.addTextChangedListener { text ->
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(500)  // Ждём 500мс после последнего ввода
            val query = text.toString()
            searchProducts(query)
        }
    }
}
```

## 💾 SharedPreferences шаблон

```kotlin
class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, default: String = ""): String {
        return prefs.getString(key, default) ?: default
    }
    
    fun saveInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, default: Int = 0): Int {
        return prefs.getInt(key, default)
    }
    
    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }
    
    fun clear() {
        prefs.edit().clear().apply()
    }
}
```

## 🎨 Glide Image Loading

```kotlin
// Extension функция
fun ImageView.loadImage(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.error)
        .into(this)
}

// Использование
binding.ivProduct.loadImage(product.imageUrl)
```

## 🔄 Pull to Refresh

```kotlin
binding.swipeRefresh.setOnRefreshListener {
    lifecycleScope.launch {
        loadData()
        binding.swipeRefresh.isRefreshing = false
    }
}
```

## 📱 Проверка интернета

```kotlin
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
        as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// Использование
if (isNetworkAvailable(this)) {
    loadDataFromNetwork()
} else {
    Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show()
}
```

## 🎯 ViewBinding Helper для Fragment

```kotlin
fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingFactory: (View) -> T
): ReadOnlyProperty<Fragment, T> =
    object : ReadOnlyProperty<Fragment, T> {
        private var binding: T? = null

        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>
        ): T {
            return binding ?: viewBindingFactory(thisRef.requireView()).also {
                binding = it
            }
        }
    }

// Использование
class MyFragment : Fragment() {
    private val binding by viewBinding(FragmentMyBinding::bind)
}
```

---

**Совет**: Сохрани эти шаблоны локально и используй как основу для своих экранов!

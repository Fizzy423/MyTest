# 🌐 Retrofit - Работа с сетью и API

## 🎯 Что такое Retrofit?

Retrofit - это библиотека для работы с REST API. Она превращает HTTP запросы в вызовы методов Kotlin.

## 📦 Подключение библиотек

```kotlin
// build.gradle.kts (app)
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp (для логирования)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Gson (для парсинга JSON)
    implementation("com.google.code.gson:gson:2.10.1")
}
```

## 🔑 Разрешение интернета

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 📝 Шаг 1: Модели данных (JSON → Kotlin)

```kotlin
// domain/Models.kt
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
)

data class Rating(
    val rate: Double,
    val count: Int
)

// Если имена отличаются от JSON
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    val id: Int,
    
    @SerializedName("user_name")
    val name: String,
    
    val email: String
)
```

### Пример JSON ответа

```json
{
  "id": 1,
  "title": "Fjallraven Backpack",
  "price": 109.95,
  "description": "Your perfect pack...",
  "category": "men's clothing",
  "image": "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_.jpg",
  "rating": {
    "rate": 3.9,
    "count": 120
  }
}
```

## 🔧 Шаг 2: API Interface

```kotlin
// data/ApiService.kt
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ========== GET запросы ==========
    
    // Получить все продукты
    @GET("products")
    suspend fun getProducts(): List<Product>
    
    // Получить продукт по ID
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") productId: Int): Product
    
    // Получить по категории
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<Product>
    
    // Query параметры (?limit=5&sort=desc)
    @GET("products")
    suspend fun getProductsWithParams(
        @Query("limit") limit: Int,
        @Query("sort") sort: String
    ): List<Product>
    
    // ========== POST запросы ==========
    
    // Создать продукт
    @POST("products")
    suspend fun createProduct(@Body product: Product): Product
    
    // Логин
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse
    
    // ========== PUT запросы ==========
    
    // Обновить продукт
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int,
        @Body product: Product
    ): Product
    
    // ========== DELETE запросы ==========
    
    // Удалить продукт
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): Response<Unit>
    
    // ========== Response с обработкой ошибок ==========
    
    @GET("products")
    suspend fun getProductsResponse(): Response<List<Product>>
}

data class LoginResponse(
    val token: String,
    val userId: Int
)
```

## 🏗️ Шаг 3: Создание Retrofit Instance

```kotlin
// data/RetrofitClient.kt
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val BASE_URL = "https://fakestoreapi.com/"
    
    // Логирование запросов
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttp клиент с настройками
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // API сервис
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

## 🚀 Использование в Activity

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        loadProducts()
    }
    
    private fun setupRecyclerView() {
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }
    
    private fun loadProducts() {
        // Показать загрузку
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Запрос к API
                val products = RetrofitClient.apiService.getProducts()
                
                // Обновить UI
                adapter.items = products
                binding.rvProducts.visibility = View.VISIBLE
                
            } catch (e: Exception) {
                // Обработка ошибки
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MainActivity", "Error loading products", e)
                
            } finally {
                // Скрыть загрузку
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun createProduct() {
        lifecycleScope.launch {
            try {
                val newProduct = Product(
                    id = 0,
                    title = "Новый товар",
                    price = 100.0,
                    description = "Описание",
                    category = "electronics",
                    image = "https://...",
                    rating = Rating(4.5, 100)
                )
                
                val created = RetrofitClient.apiService.createProduct(newProduct)
                Toast.makeText(this@MainActivity, "Создано: ${created.id}", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error creating product", e)
            }
        }
    }
}
```

## 🔄 Response для обработки ошибок

```kotlin
private fun loadProductsWithErrorHandling() {
    lifecycleScope.launch {
        try {
            val response = RetrofitClient.apiService.getProductsResponse()
            
            if (response.isSuccessful) {
                val products = response.body()
                if (products != null) {
                    adapter.items = products
                } else {
                    showError("Пустой ответ")
                }
            } else {
                // Код ошибки (404, 500 и т.д.)
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                showError("Ошибка $errorCode: $errorBody")
            }
            
        } catch (e: IOException) {
            // Нет интернета
            showError("Нет подключения к интернету")
        } catch (e: HttpException) {
            // Ошибка HTTP
            showError("Ошибка сервера: ${e.code()}")
        } catch (e: Exception) {
            // Другие ошибки
            showError("Неизвестная ошибка: ${e.message}")
        }
    }
}

private fun showError(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

## 🎯 Repository Pattern

```kotlin
// data/ProductRepository.kt
class ProductRepository(private val apiService: ApiService) {
    
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val products = apiService.getProducts()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(id: Int): Result<Product> {
        return try {
            val product = apiService.getProductById(id)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(product: Product): Result<Product> {
        return try {
            val created = apiService.createProduct(product)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Использование
class MainActivity : AppCompatActivity() {
    
    private val repository = ProductRepository(RetrofitClient.apiService)
    
    private fun loadProducts() {
        lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { products ->
                    adapter.items = products
                }
                .onFailure { exception ->
                    showError(exception.message ?: "Ошибка")
                }
        }
    }
}
```

## 🔒 Авторизация (Headers)

```kotlin
// Добавить токен в каждый запрос
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

// Добавить в OkHttpClient
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor("YOUR_TOKEN_HERE"))
    .addInterceptor(loggingInterceptor)
    .build()

// Или в конкретном запросе
@GET("products")
suspend fun getProducts(
    @Header("Authorization") token: String
): List<Product>

// Вызов
val products = apiService.getProducts("Bearer $token")
```

## 📤 Загрузка файлов (Multipart)

```kotlin
@Multipart
@POST("upload")
suspend fun uploadImage(
    @Part image: MultipartBody.Part,
    @Part("description") description: RequestBody
): UploadResponse

// Использование
private fun uploadImage(imageUri: Uri) {
    lifecycleScope.launch {
        try {
            val file = File(getRealPathFromUri(imageUri))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val description = "My image".toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = RetrofitClient.apiService.uploadImage(imagePart, description)
            Toast.makeText(this@MainActivity, "Загружено: ${response.url}", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("Upload", "Error", e)
        }
    }
}
```

## 🔄 Кэширование

```kotlin
// Добавить кэш в OkHttpClient
private val cacheSize = 10 * 1024 * 1024  // 10 MB
private val cache = Cache(context.cacheDir, cacheSize.toLong())

private val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()

// Контролировать кэш через заголовки
@GET("products")
@Headers("Cache-Control: max-age=3600")  // Кэшировать на 1 час
suspend fun getProducts(): List<Product>
```

## 🌐 Популярные API для тестирования

```kotlin
// 1. JSONPlaceholder (посты, пользователи)
const val BASE_URL = "https://jsonplaceholder.typicode.com/"

// 2. Fake Store API (магазин)
const val BASE_URL = "https://fakestoreapi.com/"

// 3. ReqRes (пользователи)
const val BASE_URL = "https://reqres.in/api/"

// 4. OpenWeatherMap (погода) - требует API ключ
const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

// 5. The Movie Database (фильмы) - требует API ключ
const val BASE_URL = "https://api.themoviedb.org/3/"
```

## ⚠️ Частые ошибки

### 1. Забыли разрешение INTERNET

```xml
<!-- Добавить в AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. Запрос в главном потоке

```kotlin
// ❌ ОШИБКА
val products = RetrofitClient.apiService.getProducts()  // Crash!

// ✅ ПРАВИЛЬНО
lifecycleScope.launch {
    val products = RetrofitClient.apiService.getProducts()
}
```

### 3. Неправильный BASE_URL

```kotlin
// ❌ ОШИБКА
const val BASE_URL = "https://fakestoreapi.com/products"  // Лишний /products

// ✅ ПРАВИЛЬНО
const val BASE_URL = "https://fakestoreapi.com/"

@GET("products")  // Полный URL: https://fakestoreapi.com/products
```

---

**Совет**: Используй Repository Pattern для отделения логики работы с сетью!

# Модуль В: Клиент‑сервер взаимодействие

## Требование из регламента
Все данные через API (Supabase REST), корректная обработка ошибок, индикация загрузки.

## Retrofit + Supabase
```kotlin
// 1. Интерфейс API
interface ApiService {
    @GET("rest/v1/users")
    suspend fun getUsers(): List<UserDto>
    
    @POST("rest/v1/users")
    suspend fun createUser(@Body user: UserDto): UserDto
    
    @POST("rest/v1/auth/v1/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse
}

// 2. Создание Retrofit
object RetrofitInstance {
    private const val BASE_URL = "https://your-supabase-url.supabase.co/"
    
    val api: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", "YOUR_ANON_KEY")
                    .addHeader("Authorization", "Bearer YOUR_TOKEN")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

## Запрос Sign Up
```kotlin
data class SignupRequest(val email: String, val password: String)
data class AuthResponse(val user: User, val session: String)

// В ViewModel
fun signup(email: String, password: String) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val response = apiService.signup(SignupRequest(email, password))
            // Сохранить токен
            prefs.edit().putString("auth_token", response.session).apply()
            _signupSuccess.value = true
        } catch (e: HttpException) {
            _error.value = "Email already exists"
        } catch (e: Exception) {
            _error.value = "Network error"
        } finally {
            _isLoading.value = false
        }
    }
}
```

## Обработка ошибок
```kotlin
// Нет интернета
fun showNetworkError() {
    AlertDialog.Builder(this)
        .setTitle("No Internet")
        .setMessage("Please check your connection")
        .setPositiveButton("Retry") { _, _ ->
            loadData()
        }
        .show()
}

// Ошибка сервера
fun handleServerError(code: Int) {
    val message = when (code) {
        400 -> "Bad request"
        401 -> "Unauthorized"
        404 -> "Not found"
        500 -> "Server error"
        else -> "Unknown error"
    }
    showError(message)
}
```

## Индикация загрузки (ProgressBar)
```xml
<ProgressBar
    android:id="@+id/progressBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:visibility="gone"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

```kotlin
viewModelScope.launch {
    _isLoading.observe(this@MainActivity) { isLoading ->
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
```

## WebSocket для Game
```kotlin
// Подключение через OkHttp WebSocket
val client = OkHttpClient()
val request = Request.Builder()
    .url("wss://your-server/game")
    .build()

val listener = object : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, text: String) {
        val event = Gson().fromJson(text, GameEvent::class.java)
        viewModelScope.launch {
            EventBus.emit(event)
        }
    }
    
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Error: ${t.message}")
    }
}

val ws = client.newWebSocket(request, listener)
```

## Retry логика
```kotlin
suspend fun <T> withRetry(maxRetries: Int = 3, block: suspend () -> T): T {
    var lastException: Exception? = null
    
    repeat(maxRetries) {
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            delay(1000 * (it + 1))  // Экспоненциальная задержка
        }
    }
    
    throw lastException ?: Exception("Failed after $maxRetries retries")
}

// Использование
val users = withRetry {
    apiService.getUsers()
}
```

---

Следующее: [Модуль Г: Хранение](D-storage.md)

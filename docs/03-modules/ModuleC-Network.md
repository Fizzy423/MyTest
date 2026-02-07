# Module C: Сеть и API (полная реализация)

## ApiService (интерфейс)
```kotlin
/**
 * Назначение: Работа с Supabase API
 * Дата: 07.10.2023
 * Автор: Student
 * Вложенные элементы:
 * - signup() — регистрация пользователя
 * - login() — вход в систему
 * - getUsers() — получить список пользователей
 * - getExercises() — получить упражнения
 * - postResult() — отправить результат упражнения
 */
interface ApiService {
    // Auth
    @POST("auth/v1/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse
    
    @POST("auth/v1/signin")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    // Data
    @GET("rest/v1/users?select=*")
    suspend fun getUsers(): List<UserDto>
    
    @GET("rest/v1/exercises?select=*")
    suspend fun getExercises(): List<ExerciseDto>
    
    @GET("rest/v1/leaderboard?select=*&order=score.desc&limit=3")
    suspend fun getLeaderboard(): List<LeaderboardDto>
    
    // Results
    @POST("rest/v1/results")
    suspend fun submitExerciseResult(@Body result: ExerciseResultDto): ResponseDto
}
```

## Data Transfer Objects (DTOs)
```kotlin
// Request/Response models
data class SignupRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val user: UserDto, val session: String)
data class ResponseDto(val success: Boolean, val message: String)

// API Models
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val avatar_url: String? = null
)

data class ExerciseDto(
    val id: Int,
    val name: String,
    val type: String,
    val description: String
)

data class ExerciseResultDto(
    val exercise_id: Int,
    val user_id: Int,
    val correct: Boolean,
    val points: Int,
    val timestamp: String
)
```

## RetrofitInstance (инициализация)
```kotlin
/**
 * Назначение: Создание и конфигурация Retrofit клиента
 * Содержит: API интерфейс, OkHttp клиент с Supabase заголовками
 */
object RetrofitInstance {
    private const val BASE_URL = "https://your-supabase-url.supabase.co/"
    private const val API_KEY = "YOUR_ANON_KEY"
    
    val api: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(ErrorInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = getToken()  // Из SharedPreferences
        
        val newRequest = originalRequest.newBuilder()
            .addHeader("apikey", "YOUR_ANON_KEY")
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
    
    private fun getToken(): String {
        // Получить из SharedPreferences
        return "your_stored_token"
    }
}

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        if (!response.isSuccessful) {
            when (response.code) {
                401 -> Log.e("API", "Unauthorized")
                404 -> Log.e("API", "Not found")
                500 -> Log.e("API", "Server error")
            }
        }
        
        return response
    }
}
```

## Repository с обработкой ошибок
```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val prefs: PreferencesManager
) {
    // Получить пользователей с fallback на БД
    suspend fun getUsers(): List<User> {
        return withRetry(maxRetries = 3) {
            try {
                val remote = apiService.getUsers()
                userDao.insertUser(remote.map { it.toEntity() })
                remote.map { it.toModel() }
            } catch (e: HttpException) {
                handleHttpError(e)
                userDao.getAllUsers().first().map { it.toModel() }
            } catch (e: IOException) {
                handleNetworkError()
                userDao.getAllUsers().first().map { it.toModel() }
            }
        }
    }
    
    // Вход в систему
    suspend fun login(email: String, password: String): User {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            prefs.saveToken(response.session)
            response.user.toModel()
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> throw Exception("Invalid credentials")
                else -> throw Exception("Login failed")
            }
        } catch (e: IOException) {
            throw Exception("Network error")
        }
    }
    
    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        delayMs: Long = 1000,
        block: suspend () -> T
    ): T {
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                delay(delayMs * (attempt + 1))
            }
        }
        throw Exception("All retries failed")
    }
    
    private fun handleHttpError(e: HttpException) {
        Log.e("API", "HTTP ${e.code()}: ${e.message()}")
    }
    
    private fun handleNetworkError() {
        Log.e("API", "Network error - using cached data")
    }
}
```

## WebSocket для Game
```kotlin
class GameWebSocketManager(
    private val userId: String,
    private val roomId: String
) {
    private var webSocket: WebSocket? = null
    private val listeners = mutableListOf<GameEventListener>()
    
    fun connect() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("wss://your-server/game/$roomId")
            .build()
        
        webSocket = client.newWebSocket(request, GameWebSocketListener())
    }
    
    fun sendMove(word: String, correct: Boolean) {
        val message = Gson().toJson(mapOf(
            "type" to "move",
            "user_id" to userId,
            "word" to word,
            "correct" to correct
        ))
        webSocket?.send(message)
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Game ended")
    }
    
    private inner class GameWebSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            val event = Gson().fromJson(text, GameEvent::class.java)
            listeners.forEach { it.onEvent(event) }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}")
            listeners.forEach { it.onError(t.message ?: "Unknown error") }
        }
    }
    
    interface GameEventListener {
        fun onEvent(event: GameEvent)
        fun onError(message: String)
    }
}
```

---

Следующее: [Module D: Storage](ModuleD-Storage.md)

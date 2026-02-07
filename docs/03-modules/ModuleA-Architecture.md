# Module A: Архитектура (полная реализация)

## Структура проекта
```
app/src/main/java/com/example/myapp/
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   └── RetrofitInstance.kt
│   ├── db/
│   │   ├── entities/
│   │   │   └── UserEntity.kt
│   │   ├── dao/
│   │   │   └── UserDao.kt
│   │   └── AppDatabase.kt
│   ├── local/
│   │   └── PreferencesManager.kt
│   └── repository/
│       └── UserRepository.kt
├── domain/
│   └── models/
│       ├── User.kt
│       ├── Exercise.kt
│       └── GameEvent.kt
├── ui/
│   ├── screens/
│   │   ├── MainActivity.kt
│   │   ├── LoginActivity.kt
│   │   ├── SignUpActivity.kt
│   │   └── ...
│   ├── adapters/
│   │   ├── ExerciseAdapter.kt
│   │   └── LeaderboardAdapter.kt
│   └── viewmodels/
│       ├── MainViewModel.kt
│       ├── LoginViewModel.kt
│       └── ...
└── common/
    ├── utils/
    │   ├── Constants.kt
    │   └── Extensions.kt
    └── di/
        └── Modules.kt
```

## Файлы реализации

### API интерфейс
```kotlin
// data/api/ApiService.kt
interface ApiService {
    @POST("auth/v1/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse
    
    @POST("auth/v1/signin")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("rest/v1/users?select=*")
    suspend fun getUsers(): List<UserDto>
    
    @GET("rest/v1/exercises?select=*")
    suspend fun getExercises(): List<ExerciseDto>
}
```

### Retrofit Instance
```kotlin
// data/api/RetrofitInstance.kt
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

### Room Entity
```kotlin
// data/db/entities/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)
```

### DAO
```kotlin
// data/db/dao/UserDao.kt
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?
    
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>
}
```

### Database
```kotlin
// data/db/AppDatabase.kt
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
                    .build()
                    .also { instance = it }
            }
        }
    }
}
```

### Repository
```kotlin
// data/repository/UserRepository.kt
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val prefs: PreferencesManager
) {
    suspend fun getUsers(): List<User> {
        return try {
            val remote = apiService.getUsers()
            userDao.insertUser(remote.map { it.toEntity() })
            remote.map { it.toModel() }
        } catch (e: Exception) {
            userDao.getAllUsers().first().map { it.toModel() }
        }
    }
    
    suspend fun login(email: String, password: String): User {
        val response = apiService.login(LoginRequest(email, password))
        prefs.saveToken(response.session)
        return response.user
    }
}
```

### Model (domain)
```kotlin
// domain/models/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)

// DTO для API
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val avatar_url: String? = null
) {
    fun toModel() = User(id, name, email, avatar_url)
}
```

### Preferences Manager
```kotlin
// data/local/PreferencesManager.kt
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    
    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }
    
    fun getToken(): String? = prefs.getString("auth_token", null)
    
    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }
}
```

### ViewModel
```kotlin
// ui/viewmodels/MainViewModel.kt
class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getUsers()
                _users.value = data
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

Следующее: [Module B: UI](ModuleB-UI.md)

# Модуль Г: Хранение данных

## Требование из регламента
Кэш, локальное сохранение состояния, язык, сессия.

## SharedPreferences (быстрое хранилище)
```kotlin
// Сохранить
val prefs = getSharedPreferences("app", MODE_PRIVATE)
prefs.edit().putString("lang", "en").apply()
prefs.edit().putBoolean("is_logged_in", true).apply()
prefs.edit().putInt("user_id", 123).apply()

// Прочитать
val lang = prefs.getString("lang", "en") ?: "en"
val isLoggedIn = prefs.getBoolean("is_logged_in", false)
val userId = prefs.getInt("user_id", -1)
```

## Сохранение языка
```kotlin
object LanguageManager {
    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", langCode).apply()
        
        // Применить систему
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        return prefs.getString("lang", "en") ?: "en"
    }
}
```

## Сохранение состояния Sign Up (после перезагрузки)
```kotlin
class SignUpViewModel : ViewModel() {
    private val prefs = App.instance.getSharedPreferences("signup", MODE_PRIVATE)
    
    var email: String
        get() = prefs.getString("email", "") ?: ""
        set(value) = prefs.edit().putString("email", value).apply()
    
    var firstName: String
        get() = prefs.getString("firstName", "") ?: ""
        set(value) = prefs.edit().putString("firstName", value).apply()
    
    fun clearForm() {
        prefs.edit().clear().apply()
    }
}

// В Activity
override fun onPause() {
    super.onPause()
    viewModel.email = binding.etEmail.text.toString()
    viewModel.firstName = binding.etName.text.toString()
}

override fun onResume() {
    super.onResume()
    binding.etEmail.setText(viewModel.email)
    binding.etName.setText(viewModel.firstName)
}
```

## Сохранение сессии Log In
```kotlin
object AuthManager {
    private const val TOKEN_KEY = "auth_token"
    private const val USER_ID_KEY = "user_id"
    private const val PREFS_NAME = "auth"
    
    fun saveSession(context: Context, token: String, userId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putInt(USER_ID_KEY, userId)
            .apply()
    }
    
    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
    
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
```

## Room Database (локальная БД)
```kotlin
// 1. Сущность
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String
)

// 2. DAO
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?
}

// 3. База данных
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

## Хранение серии успешных ответов (Упражнения)
```kotlin
class ExerciseViewModel : ViewModel() {
    private val prefs = App.instance.getSharedPreferences("exercises", MODE_PRIVATE)
    
    fun incrementStreak(exerciseId: String) {
        val current = prefs.getInt("streak_$exerciseId", 0)
        prefs.edit().putInt("streak_$exerciseId", current + 1).apply()
    }
    
    fun getStreak(exerciseId: String): Int {
        return prefs.getInt("streak_$exerciseId", 0)
    }
    
    fun resetStreak(exerciseId: String) {
        prefs.edit().putInt("streak_$exerciseId", 0).apply()
    }
}
```

## Кэширование изображений (Glide)
```kotlin
// Глайд автоматически кэшит
Glide.with(this)
    .load("https://example.com/image.jpg")
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView)
```

## Открыть PDF (условия)
```kotlin
fun openPdf(context: Context) {
    try {
        val file = File(context.filesDir, "policy.pdf")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("PDF", e.message ?: "Error opening PDF")
    }
}
```

---

Следующее: [Модуль Е: Аппаратные функции](E-hardware.md)

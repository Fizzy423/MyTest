# Module D: Хранение (полная реализация)

## SharedPreferences Manager
```kotlin
/**
 * Назначение: Управление локальным хранилищем
 * Содержит: язык, токен, состояние Sign Up, серии упражнений
 */
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    
    // Auth
    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }
    
    fun getToken(): String? = prefs.getString("auth_token", null)
    fun clearToken() = prefs.edit().remove("auth_token").apply()
    
    // Language
    fun setLanguage(langCode: String) {
        prefs.edit().putString("language", langCode).apply()
    }
    
    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"
    
    // Sign Up state (сохранение после перезагрузки)
    fun saveSignUpData(email: String, firstName: String, lastName: String) {
        prefs.edit()
            .putString("signup_email", email)
            .putString("signup_first_name", firstName)
            .putString("signup_last_name", lastName)
            .apply()
    }
    
    fun getSignUpData(): Triple<String, String, String> {
        val email = prefs.getString("signup_email", "") ?: ""
        val firstName = prefs.getString("signup_first_name", "") ?: ""
        val lastName = prefs.getString("signup_last_name", "") ?: ""
        return Triple(email, firstName, lastName)
    }
    
    fun clearSignUpData() {
        prefs.edit()
            .remove("signup_email")
            .remove("signup_first_name")
            .remove("signup_last_name")
            .apply()
    }
    
    // Exercise streaks
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
    
    // Theme
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }
    
    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    
    // Onboarding
    fun setOnboardingSeen() {
        prefs.edit().putBoolean("onboarding_seen", true).apply()
    }
    
    fun hasSeenOnboarding(): Boolean = prefs.getBoolean("onboarding_seen", false)
}
```

## Room Database (реальная реализация)
```kotlin
// Сущности
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val description: String
)

@Entity(tableName = "exercise_results")
data class ExerciseResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val userId: Int,
    val correct: Boolean,
    val points: Int,
    val timestamp: String
)

// DAO
@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    @Insert
    suspend fun insertResult(result: ExerciseResultEntity)
    
    @Query("SELECT SUM(points) as total FROM exercise_results WHERE userId = :userId")
    suspend fun getTotalPoints(userId: Int): Int
}

// Database
@Database(
    entities = [UserEntity::class, ExerciseEntity::class, ExerciseResultEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
```

## Локализация (переводы)
```kotlin
/**
 * Назначение: Система локализации приложения
 * Использует готовые переводы и SharedPreferences
 */
object LocalizationManager {
    private var currentLocale = Locale.getDefault()
    
    fun setLanguage(context: Context, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        currentLocale = locale
        
        // Сохранить
        PreferencesManager(context).setLanguage(langCode)
    }
    
    fun getStringForLanguage(context: Context, stringResId: Int): String {
        return context.resources.getString(stringResId)
    }
}

// strings.xml (основной)
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Language Learning</string>
    <string name="login">Log In</string>
    <string name="signup">Sign Up</string>
</resources>

// values-ru/strings.xml (русский)
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Изучение языков</string>
    <string name="login">Войти</string>
    <string name="signup">Регистрация</string>
</resources>
```

## Кэширование с Glide
```kotlin
// В build.gradle
implementation("com.github.bumptech.glide:glide:4.15.1")

// Использование с кэшем
class ImageLoader {
    fun loadImage(context: Context, url: String, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .into(imageView)
    }
    
    fun clearCache(context: Context) {
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
}
```

## Открытие PDF (политика)
```kotlin
class PdfManager(private val context: Context) {
    fun openPolicy() {
        try {
            // Скопировать PDF из assets
            val inputStream = context.assets.open("policy.pdf")
            val file = File(context.cacheDir, "policy.pdf")
            
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PDF", e.message ?: "Error")
        }
    }
}
```

---

Следующее: [Module E: Hardware](ModuleE-Hardware.md)

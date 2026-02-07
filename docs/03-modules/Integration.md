# Интеграция: как соединить все модули

## Зависимости в gradle
```gradle
dependencies {
    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
    
    // UI
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("com.google.android.material:material:1.9.0")
    
    // Images
    implementation("com.github.bumptech.glide:glide:4.15.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    
    // TensorFlow
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
}
```

## Application класс (инициализация)
```kotlin
/**
 * Назначение: Инициализация приложения, создание синглтонов
 */
class MyApp : Application() {
    // Database
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    // Preferences
    val prefs: PreferencesManager by lazy {
        PreferencesManager(this)
    }
    
    // API
    val apiService by lazy {
        RetrofitInstance.api
    }
    
    // Repository
    val userRepository: UserRepository by lazy {
        UserRepository(apiService, database.userDao(), prefs)
    }
    
    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepository(apiService, database.exerciseDao())
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация Glide
        Glide.get(this)
        
        // Загрузить язык из настроек
        val lang = prefs.getLanguage()
        LocalizationManager.setLanguage(this, lang)
        
        // Установить тему
        val isDark = prefs.isDarkMode()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
```

## Главный Activity с интеграцией
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val app: MyApp by lazy { application as MyApp }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Создать ViewModel с Repository из Application
        val factory = MainViewModelFactory(app.userRepository, app.exerciseRepository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        
        setupUI()
        observeData()
    }
    
    private fun setupUI() {
        // RecyclerViews
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = LeaderboardAdapter()
        
        binding.rvExercises.layoutManager = GridLayoutManager(this, 2)
        binding.rvExercises.adapter = ExerciseAdapter { exercise ->
            navigateToExercise(exercise)
        }
        
        // Обновить данные
        viewModel.loadLeaderboard()
        viewModel.loadExercises()
    }
    
    private fun observeData() {
        viewModel.leaderboard.observe(this) { leaderboard ->
            (binding.rvLeaderboard.adapter as LeaderboardAdapter).submitList(leaderboard)
        }
        
        viewModel.exercises.observe(this) { exercises ->
            (binding.rvExercises.adapter as ExerciseAdapter).submitList(exercises)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) showError(error)
        }
    }
    
    private fun navigateToExercise(exercise: Exercise) {
        // Здесь интегрируются модули упражнений
        when (exercise.type) {
            "guess_animal" -> startActivity(Intent(this, GuessAnimalActivity::class.java))
            "choose" -> startActivity(Intent(this, ChooseAnswerActivity::class.java))
            "listening" -> startActivity(Intent(this, ListeningActivity::class.java))
        }
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ -> viewModel.loadLeaderboard() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
```

## ViewModel Factory
```kotlin
class MainViewModelFactory(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(userRepository, exerciseRepository) as T
    }
}
```

## Главный ViewModel (интеграция всех модулей)
```kotlin
class MainViewModel(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    // Лидерборд (модуль B, C, D)
    private val _leaderboard = MutableLiveData<List<User>>()
    val leaderboard: LiveData<List<User>> = _leaderboard
    
    // Упражнения (модуль F)
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises
    
    // Состояние загрузки
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Ошибки
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = userRepository.getUsers()
                _leaderboard.value = users.sortedByDescending { it.score }.take(3)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadExercises() {
        viewModelScope.launch {
            try {
                val exercises = exerciseRepository.getExercises()
                _exercises.value = exercises
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load exercises"
            }
        }
    }
}
```

## AndroidManifest.xml (все необходимые разрешения)
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Network -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Camera & Storage -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <!-- Microphone -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    
    <!-- Notifications -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application
        android:name=".MyApp"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Provider для FileProvider (PDF, фото) -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
        
    </application>
</manifest>
```

---

Следующее: [Toggles: включение/отключение модулей](Toggles.md)

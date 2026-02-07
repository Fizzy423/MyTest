# Модуль А: Архитектура приложения

## Требование из регламента
Разделить слои бизнес‑логики, UI и домена. Изменение одного экрана не должно ломать другие.

## Связка слоёв
```
UI (Activity/Fragment)
       ↓
   ViewModel
       ↓
    Repository
       ↓
   Data (API/Room/SharedPrefs)
```

## Структура проекта
```
app/src/main/java/com/example/myapp/
├── data/
│   ├── api/              // Retrofit интерфейсы
│   ├── db/               // Room сущности и DAO
│   ├── local/            // SharedPreferences
│   └── repository/       // Логика получения данных
├── domain/
│   └── models/           // Data classes (User, Product и т.д.)
├── ui/
│   ├── screens/          // Activity/Fragment
│   ├── adapters/         // RecyclerView адаптеры
│   └── viewmodels/       // ViewModel
└── common/
    ├── utils/            // Утилиты
    └── constants/        // Константы
```

## Обязательные комментарии в каждом классе
```kotlin
/**
 * Назначение: Главный экран приложения с лидербордом
 * Дата создания: 07.10.2023
 * Автор: Студент_123
 */
class MainActivity : AppCompatActivity() {
    // ...
}

/**
 * Назначение: Загрузка данных из API
 * Вложенные элементы:
 * - getUsers(): List<User> — получить список пользователей
 * - getExercises(): List<Exercise> — получить упражнения
 */
class ApiService {
    // ...
}
```

## Event Bus для WebSocket (Модуль Г)
На экране "Игра" используется WebSocket. Для интеграции используй Event Bus:

```kotlin
// 1. Создай Event Bus (singleton)
object EventBus {
    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()
    
    suspend fun emit(event: GameEvent) {
        _events.emit(event)
    }
}

// 2. Определи события
sealed class GameEvent {
    data class PlayerJoined(val playerId: String) : GameEvent()
    data class WordReceived(val word: String) : GameEvent()
    data class GameOver(val winner: String) : GameEvent()
}

// 3. Слушай события
lifecycleScope.launch {
    EventBus.events.collect { event ->
        when (event) {
            is GameEvent.PlayerJoined -> updateUI()
            is GameEvent.WordReceived -> displayWord()
            is GameEvent.GameOver -> showResult()
        }
    }
}

// 4. Отправляй события
viewModelScope.launch {
    EventBus.emit(GameEvent.PlayerJoined("player123"))
}
```

## ViewModel для связи Activity ↔ Repository
```kotlin
class MainViewModel(private val repo: Repository) : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    fun loadUsers() {
        viewModelScope.launch {
            try {
                val data = repo.getUsers()
                _users.value = data
            } catch (e: Exception) {
                Log.e("MainVM", e.message ?: "Unknown error")
            }
        }
    }
}
```

## Repository (логика источников данных)
```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val prefs: SharedPreferences
) {
    suspend fun getUsers(): List<User> {
        return try {
            // Попытка сети
            val remote = apiService.getUsers()
            // Сохранить локально
            userDao.insertUsers(remote.map { it.toEntity() })
            remote
        } catch (e: Exception) {
            // Если сеть упала — берём из БД
            userDao.getAllUsers().map { it.toModel() }
        }
    }
}
```

## Зависимости (как инжектить)
Либо вручную:
```kotlin
class App : Application() {
    val apiService by lazy { RetrofitInstance.api }
    val database by lazy { AppDatabase.getInstance(this) }
    val repo by lazy { UserRepository(apiService, database.userDao()) }
}

// В Activity
val repo = (application as App).repo
```

Или с Hilt (если будет время):
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(repo: Repository) : ViewModel()
```

---

Следующее: [Модуль Б: Верстка UI](B-ui.md)

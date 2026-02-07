# MVVM и Architecture Components

## Что такое MVVM?

**MVVM** = Model-View-ViewModel

```
View (Activity/Fragment)
    ↓↑
ViewModel
    ↓↑
Model (Repository, Use Cases)
    ↓↑
Data (API, Database)
```

## Model (Модель данных)

```kotlin
// domain/models/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val score: Int = 0
)
```

## ViewModel (Связь между View и Model)

```kotlin
class MainViewModel(private val repo: UserRepository) : ViewModel() {
    // LiveData для UI обновления
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Методы для загрузки данных
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repo.getUsers()
                _users.value = data
            } catch (e: Exception) {
                Log.e("ViewModel", e.message ?: "Error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

## View (Activity/Fragment)

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        // Слушаем изменения данных
        viewModel.users.observe(this) { users ->
            updateUI(users)
        }
        
        // Загружаем данные
        viewModel.loadUsers()
    }
    
    private fun updateUI(users: List<User>) {
        // Обновляем UI
    }
}
```

## LiveData vs StateFlow

```kotlin
// LiveData (старый способ)
val liveData = MutableLiveData<String>()
liveData.observe(this) { value -> }

// StateFlow (новый способ)
val stateFlow = MutableStateFlow<String>("")
stateFlow.collect { value -> }
```

## ViewModel Lifecycle

```
onCreate() → ViewModel создан
onPause() → ViewModel сохранён
onResume() → ViewModel восстановлен
onDestroy() → ViewModel удалён
```

**Важно:** ViewModel **не** удаляется при rotации экрана!

---

Следующее: [Code Templates](09-code-templates.md)

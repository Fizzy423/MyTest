# Частые ошибки и их решения

## 1. NullPointerException

### ❌ Ошибка
```kotlin
val user: User? = null
println(user.name)  // Crash!
```

### ✅ Решение
```kotlin
val user: User? = null
println(user?.name ?: "Unknown")  // null или "Unknown"

// Или проверка
if (user != null) {
    println(user.name)
}
```

## 2. lateinit не инициализирован

### ❌ Ошибка
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    fun doSomething() {
        binding.tvText.text = "Hi"  // Если onCreate не вызван → Crash!
    }
}
```

### ✅ Решение
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    
    fun doSomething() {
        if (::binding.isInitialized) {
            binding.tvText.text = "Hi"
        }
    }
}
```

## 3. Работа с сетью в главном потоке

### ❌ Ошибка
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val response = apiService.getUsers()  // ANR (приложение зависнет)!
}
```

### ✅ Решение
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    viewModelScope.launch {
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getUsers()
            }
            // Обновляем UI
            updateUI(response)
        } catch (e: Exception) {
            Log.e("Error", e.message ?: "Unknown")
        }
    }
}
```

## 4. Memory Leak в Observer

### ❌ Ошибка
```kotlin
val observer = Observer<String> { value ->
    println(value)
}

liveData.observe(this, observer)
// observer никогда не удалится, утечка памяти!
```

### ✅ Решение
```kotlin
liveData.observe(this) { value ->
    println(value)
}
// Автоматически удалится с Activity
```

## 5. Забыл добавить разрешение в Manifest

### ❌ Ошибка
```kotlin
// В Activity
startActivity(Intent(Intent.ACTION_VIEW))
// Crash: permission denied
```

### ✅ Решение
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

## 6. Ошибка типа при прямом приведении

### ❌ Ошибка
```kotlin
val obj: Any = "Hello"
val num = obj as Int  // ClassCastException!
```

### ✅ Решение
```kotlin
val obj: Any = "Hello"
val num = obj as? Int  // null, без краша
println(num ?: "Not a number")
```

## 7. RecyclerView crash (IndexOutOfBounds)

### ❌ Ошибка
```kotlin
override fun onBindViewHolder(holder: VH, position: Int) {
    holder.bind(items[position])  // Если список изменился между обновлениями!
}
```

### ✅ Решение
```kotlin
override fun onBindViewHolder(holder: VH, position: Int) {
    if (position < items.size) {
        holder.bind(items[position])
    }
}
```

## 8. View не обновляется

### ❌ Ошибка
```kotlin
val text = "Hello"
binding.tvText.text = text

text = "World"  // binding.tvText всё ещё показывает "Hello"!
```

### ✅ Решение (использовать LiveData)
```kotlin
private val _text = MutableLiveData("Hello")
val text: LiveData<String> = _text

_text.observe(this) { value ->
    binding.tvText.text = value
}

_text.value = "World"  // Теперь обновляется!
```

## 9. Ошибка при rotации экрана

### ❌ Ошибка
```kotlin
class MainActivity : AppCompatActivity() {
    private var adapter: ExerciseAdapter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ExerciseAdapter()  // Пересоздаётся при rotation!
    }
}
```

### ✅ Решение (ViewModel сохраняет состояние)
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // viewModel НЕ пересоздаётся при rotation!
    }
}
```

## 10. Image не загружается (Glide)

### ❌ Ошибка
```kotlin
Glide.with(this)
    .load(url)
    .into(imageView)
// Если Activity уже закрыта → crash
```

### ✅ Решение
```kotlin
Glide.with(this)
    .load(url)
    .error(R.drawable.placeholder)
    .into(imageView)
```

---

Следующее: [Competition Checklist](11-competition-checklist.md)

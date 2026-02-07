# Модуль Б: Верстка и UI

## Требование из регламента
Адаптивная верстка, точное соответствие макету, без пустых зон.

## Обязательные экраны
1. SplashScreen
2. Onboarding
3. Language select
4. Sign Up (2 шага)
5. Log In
6. Main screen
7. Profile
8. Profile resize photo
9. Exercise: Guess animal
10. Exercise: Choose correct
11. Game
12. Exercise: Listening

## ViewBinding
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
```

## Адаптивная верстка (ConstraintLayout)
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Welcome"
        android:textSize="24sp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        android:layout_margin="16dp" />
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

## Важные детали экранов

### Sign Up (2 шага)
**Шаг 1:**
- Email с проверкой: `^[a-z0-9]+@[a-z0-9]+\.[a-z]{2,3}$`
- Кнопка "Next"

**Шаг 2:**
- Пароль (>= 8, заглавные, строчные, цифры, спецсимволы)
- Согласие с политикой (checkbox)
- Кнопка "Sign Up"

```kotlin
// Проверка email
fun isValidEmail(email: String): Boolean {
    return email.matches("^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,3}$".toRegex())
}

// Проверка пароля
fun isStrongPassword(password: String): Boolean {
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    return password.length >= 8 && hasUpper && hasLower && hasDigit && hasSpecial
}
```

### Main screen
- Аватар + имя в шапке
- Лидерборд: топ‑3 по баллам
- Плитки упражнений (RecyclerView с GridLayout)

### Profile
- Аватар (с выбором камера/галерея)
- Смена темы (кнопка с текстом, который меняется)
- Выбор языка

### Language select
- Доступен только **системный** и **английский**

### Onboarding
- Слайды (ViewPager2)
- Последний слайд → Button меняется
- Пропуск → Language select

## Показ/скрытие пароля
```kotlin
var isPasswordVisible = false

binding.btnTogglePassword.setOnClickListener {
    isPasswordVisible = !isPasswordVisible
    
    if (isPasswordVisible) {
        binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_open)
    } else {
        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_closed)
    }
    
    binding.etPassword.setSelection(binding.etPassword.text.length)
}
```

## Диалоги ошибок
```kotlin
fun showError(message: String) {
    AlertDialog.Builder(this)
        .setTitle("Error")
        .setMessage(message)
        .setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}
```

---

Следующее: [Модуль В: Сеть](C-network.md)

# 🎨 Android - Работа с UI (Views, ViewBinding)

## 📐 XML Layouts

### LinearLayout (линейное расположение)

```xml
<!-- activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Заголовок"
        android:textSize="24sp"
        android:textStyle="bold" />

    <EditText
        android:id="@+id/etInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Введите текст"
        android:layout_marginTop="16dp" />

    <Button
        android:id="@+id/btnSubmit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Отправить"
        android:layout_marginTop="16dp" />

</LinearLayout>
```

### ConstraintLayout (гибкое расположение)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Центрированный элемент -->
    <TextView
        android:id="@+id/tvCenter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="По центру"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <!-- Кнопка внизу -->
    <Button
        android:id="@+id/btnBottom"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Кнопка внизу"
        android:layout_margin="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <!-- Два элемента рядом -->
    <Button
        android:id="@+id/btnLeft"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Слева"
        app:layout_constraintEnd_toStartOf="@id/btnRight"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/btnRight"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Справа"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/btnLeft"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### FrameLayout (элементы друг на друге)

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/background"
        android:scaleType="centerCrop" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Поверх изображения"
        android:layout_gravity="center"
        android:textColor="@android:color/white"
        android:textSize="24sp" />

</FrameLayout>
```

## 🔗 ViewBinding (рекомендуемый способ)

### Настройка ViewBinding

```kotlin
// build.gradle.kts (app)
android {
    buildFeatures {
        viewBinding = true
    }
}
```

### Использование в Activity

```kotlin
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    // Объявить binding
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализировать binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Теперь можно обращаться к Views
        binding.tvTitle.text = "Новый заголовок"
        binding.etInput.hint = "Введите имя"
        
        binding.btnSubmit.setOnClickListener {
            val text = binding.etInput.text.toString()
            binding.tvTitle.text = "Вы ввели: $text"
        }
    }
}
```

### Использование во Fragment

```kotlin
import com.example.myapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Работа с UI
        binding.tvTitle.text = "Фрагмент"
        binding.btnClick.setOnClickListener {
            // действие
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Важно! Избежать утечек памяти
    }
}
```

## 📝 Основные View компоненты

### TextView

```kotlin
// Установить текст
binding.tvTitle.text = "Новый текст"

// Получить текст
val text = binding.tvTitle.text.toString()

// Цвет текста
binding.tvTitle.setTextColor(Color.RED)
binding.tvTitle.setTextColor(getColor(R.color.primary))

// Размер текста
binding.tvTitle.textSize = 20f  // в sp

// Видимость
binding.tvTitle.visibility = View.VISIBLE   // Видим
binding.tvTitle.visibility = View.INVISIBLE // Невидим, но занимает место
binding.tvTitle.visibility = View.GONE      // Невидим и не занимает место

// Проверка видимости
if (binding.tvTitle.visibility == View.VISIBLE) {
    // виден
}
```

### EditText (поле ввода)

```kotlin
// Получить текст
val text = binding.etInput.text.toString()

// Установить текст
binding.etInput.setText("Новый текст")

// Очистить
binding.etInput.text.clear()

// Hint (подсказка)
binding.etInput.hint = "Введите что-нибудь"

// Тип клавиатуры (в XML)
// android:inputType="text"           // Обычный текст
// android:inputType="textEmailAddress"  // Email
// android:inputType="phone"          // Телефон
// android:inputType="number"         // Числа
// android:inputType="textPassword"   // Пароль

// Слушатель изменений
binding.etInput.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s.toString()
        println("Текст изменился: $text")
    }
    
    override fun afterTextChanged(s: Editable?) {}
})
```

### Button

```kotlin
// Клик
binding.btnSubmit.setOnClickListener {
    Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_SHORT).show()
}

// Изменить текст
binding.btnSubmit.text = "Отправлено"

// Включить/выключить
binding.btnSubmit.isEnabled = false  // Выключить
binding.btnSubmit.isEnabled = true   // Включить

// Длинное нажатие
binding.btnSubmit.setOnLongClickListener {
    Toast.makeText(this, "Долгое нажатие", Toast.LENGTH_SHORT).show()
    true  // true = событие обработано
}
```

### ImageView

```kotlin
// Установить изображение из ресурсов
binding.ivImage.setImageResource(R.drawable.ic_launcher)

// Из URL (с Glide - см. раздел про Glide)
Glide.with(this)
    .load("https://example.com/image.jpg")
    .into(binding.ivImage)

// ScaleType
binding.ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
// CENTER_CROP - обрезать, заполнить весь View
// FIT_CENTER - вписать без обрезки
// CENTER_INSIDE - вписать, не больше оригинала
```

### RecyclerView

```kotlin
// Настройка
binding.rvItems.layoutManager = LinearLayoutManager(this)
binding.rvItems.adapter = myAdapter

// Горизонтальный список
binding.rvItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

// Сетка (Grid)
binding.rvItems.layoutManager = GridLayoutManager(this, 2)  // 2 колонки
```

### ProgressBar

```kotlin
// Показать/скрыть
binding.progressBar.visibility = View.VISIBLE
binding.progressBar.visibility = View.GONE

// Прогресс (для горизонтального ProgressBar)
binding.progressBar.max = 100
binding.progressBar.progress = 50  // 50%
```

### CheckBox

```kotlin
// Проверить состояние
val isChecked = binding.checkBox.isChecked

// Установить состояние
binding.checkBox.isChecked = true

// Слушатель изменений
binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        println("Выбрано")
    } else {
        println("Не выбрано")
    }
}
```

### RadioButton / RadioGroup

```xml
<RadioGroup
    android:id="@+id/radioGroup"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <RadioButton
        android:id="@+id/rbOption1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Вариант 1" />

    <RadioButton
        android:id="@+id/rbOption2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Вариант 2" />

</RadioGroup>
```

```kotlin
// Слушатель выбора
binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
    when (checkedId) {
        R.id.rbOption1 -> println("Выбран вариант 1")
        R.id.rbOption2 -> println("Выбран вариант 2")
    }
}

// Получить выбранный ID
val selectedId = binding.radioGroup.checkedRadioButtonId

// Выбрать программно
binding.radioGroup.check(R.id.rbOption1)
```

### Switch

```kotlin
// Проверить состояние
val isOn = binding.switch1.isChecked

// Установить
binding.switch1.isChecked = true

// Слушатель
binding.switch1.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        println("Включено")
    } else {
        println("Выключено")
    }
}
```

## 🎨 Стилизация Views

### Программно

```kotlin
// Цвет фона
binding.tvTitle.setBackgroundColor(Color.BLUE)
binding.tvTitle.setBackgroundResource(R.drawable.background)

// Отступы (padding)
binding.tvTitle.setPadding(16, 8, 16, 8)  // left, top, right, bottom (в px)

// Margin (программно сложнее)
val params = binding.tvTitle.layoutParams as ViewGroup.MarginLayoutParams
params.setMargins(16, 8, 16, 8)
binding.tvTitle.layoutParams = params

// Размеры
val widthPx = 200
val heightPx = 100
binding.tvTitle.layoutParams.width = widthPx
binding.tvTitle.layoutParams.height = heightPx
binding.tvTitle.requestLayout()
```

### В XML

```xml
<TextView
    android:id="@+id/tvStyled"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Стилизованный текст"
    android:textSize="20sp"
    android:textColor="@color/primary"
    android:textStyle="bold"
    android:background="@color/accent"
    android:padding="16dp"
    android:layout_margin="8dp"
    android:gravity="center"
    android:elevation="4dp" />
```

## 🖼️ Glide - Загрузка изображений

### Базовое использование

```kotlin
// Простая загрузка
Glide.with(this)
    .load("https://example.com/image.jpg")
    .into(binding.ivImage)

// С placeholder и ошибкой
Glide.with(this)
    .load(imageUrl)
    .placeholder(R.drawable.placeholder)  // Пока грузится
    .error(R.drawable.error)              // Если ошибка
    .into(binding.ivImage)

// Круглое изображение
Glide.with(this)
    .load(imageUrl)
    .circleCrop()
    .into(binding.ivImage)

// Скругленные углы
Glide.with(this)
    .load(imageUrl)
    .transform(RoundedCorners(20))  // радиус в px
    .into(binding.ivImage)

// Кэширование
Glide.with(this)
    .load(imageUrl)
    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Кэшировать всё
    .into(binding.ivImage)
```

## 🔄 Диалоги

### AlertDialog

```kotlin
// Простой диалог
AlertDialog.Builder(this)
    .setTitle("Заголовок")
    .setMessage("Это сообщение")
    .setPositiveButton("OK") { dialog, _ ->
        dialog.dismiss()
    }
    .show()

// С кнопками
AlertDialog.Builder(this)
    .setTitle("Удалить?")
    .setMessage("Вы уверены, что хотите удалить этот элемент?")
    .setPositiveButton("Да") { _, _ ->
        // Удалить
        Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show()
    }
    .setNegativeButton("Нет") { dialog, _ ->
        dialog.dismiss()
    }
    .show()

// Список выбора
val items = arrayOf("Вариант 1", "Вариант 2", "Вариант 3")
AlertDialog.Builder(this)
    .setTitle("Выберите вариант")
    .setItems(items) { _, which ->
        Toast.makeText(this, "Выбран: ${items[which]}", Toast.LENGTH_SHORT).show()
    }
    .show()

// Single choice (radio buttons)
var selectedItem = 0
AlertDialog.Builder(this)
    .setTitle("Выберите один")
    .setSingleChoiceItems(items, selectedItem) { _, which ->
        selectedItem = which
    }
    .setPositiveButton("OK") { _, _ ->
        Toast.makeText(this, "Выбран: ${items[selectedItem]}", Toast.LENGTH_SHORT).show()
    }
    .show()

// Multiple choice (checkboxes)
val checkedItems = booleanArrayOf(false, false, false)
AlertDialog.Builder(this)
    .setTitle("Выберите несколько")
    .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
        checkedItems[which] = isChecked
    }
    .setPositiveButton("OK") { _, _ ->
        val selected = items.filterIndexed { index, _ -> checkedItems[index] }
        Toast.makeText(this, "Выбрано: ${selected.joinToString()}", Toast.LENGTH_SHORT).show()
    }
    .show()
```

## 📏 Единицы измерения

```kotlin
// dp → px
fun dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

// px → dp
fun pxToDp(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}

// Использование
val paddingPx = dpToPx(16)
view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
```

---

**Совет**: Всегда используй ViewBinding вместо findViewById!

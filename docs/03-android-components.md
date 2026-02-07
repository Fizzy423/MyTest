# 📱 Android - Основы компонентов

## 🏠 Activity - Экран приложения

### Базовая Activity

```kotlin
package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Здесь инициализация UI
        // Настройка слушателей
        // Загрузка данных
    }
}
```

### Жизненный цикл Activity

```kotlin
class MainActivity : AppCompatActivity() {
    
    // 1. Activity создается
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("onCreate - Activity создана")
    }
    
    // 2. Activity становится видимой
    override fun onStart() {
        super.onStart()
        println("onStart - Activity видна")
    }
    
    // 3. Activity на переднем плане (пользователь взаимодействует)
    override fun onResume() {
        super.onResume()
        println("onResume - Activity активна")
        // Начать анимации, запустить камеру и т.д.
    }
    
    // 4. Activity теряет фокус (диалог открылся)
    override fun onPause() {
        super.onPause()
        println("onPause - Activity теряет фокус")
        // Остановить анимации, сохранить черновики
    }
    
    // 5. Activity больше не видна
    override fun onStop() {
        super.onStop()
        println("onStop - Activity не видна")
        // Освободить ресурсы
    }
    
    // 6. Activity уничтожается
    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy - Activity уничтожена")
        // Очистить всё
    }
    
    // Activity возобновляется после onStop
    override fun onRestart() {
        super.onRestart()
        println("onRestart - Activity перезапускается")
    }
}
```

### Навигация между Activity

```kotlin
// Открыть другую Activity
val intent = Intent(this, SecondActivity::class.java)
startActivity(intent)

// Передать данные
val intent = Intent(this, DetailActivity::class.java)
intent.putExtra("USER_ID", 123)
intent.putExtra("USER_NAME", "Иван")
startActivity(intent)

// Получить данные в DetailActivity
class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME") ?: "Неизвестно"
        
        println("User: $userName ($userId)")
    }
}

// Закрыть текущую Activity
finish()
```

## 📄 Fragment - Часть UI

### Базовый Fragment

```kotlin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Создать View из XML
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Здесь работать с UI
        // view.findViewById<TextView>(R.id.textView)
    }
}
```

### Добавление Fragment в Activity

```kotlin
// В Activity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Добавить Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }
}
```

```xml
<!-- activity_main.xml -->
<FrameLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Передача данных во Fragment

```kotlin
// Создать Fragment с данными
val fragment = DetailFragment()
val bundle = Bundle()
bundle.putInt("PRODUCT_ID", 123)
bundle.putString("PRODUCT_NAME", "Телефон")
fragment.arguments = bundle

supportFragmentManager.beginTransaction()
    .replace(R.id.container, fragment)
    .addToBackStack(null)  // Добавить в back stack
    .commit()

// Получить данные во Fragment
class DetailFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val productId = arguments?.getInt("PRODUCT_ID") ?: -1
        val productName = arguments?.getString("PRODUCT_NAME") ?: ""
        
        println("Product: $productName ($productId)")
    }
}
```

## 📱 Context

```kotlin
// Context в Activity
val context = this  // Activity сама является Context

// Context во Fragment
val context = requireContext()  // Или context (nullable)

// Application Context (живет всё время работы приложения)
val appContext = applicationContext

// Использование Context
// 1. Получить ресурсы
val text = context.getString(R.string.app_name)
val color = context.getColor(R.color.primary)
val drawable = context.getDrawable(R.drawable.ic_launcher)

// 2. Создать Toast
Toast.makeText(context, "Привет!", Toast.LENGTH_SHORT).show()

// 3. Запустить Activity
val intent = Intent(context, MainActivity::class.java)
context.startActivity(intent)

// 4. Получить системные сервисы
val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
```

## 📝 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp">

    <!-- Разрешения -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">

        <!-- Главная Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Другие Activity -->
        <activity
            android:name=".DetailActivity"
            android:exported="false" />

    </application>

</manifest>
```

## 🎨 Resources (Ресурсы)

### strings.xml

```xml
<resources>
    <string name="app_name">Моё Приложение</string>
    <string name="welcome_message">Добро пожаловать!</string>
    <string name="error_message">Произошла ошибка</string>
    
    <!-- С параметрами -->
    <string name="greeting">Привет, %1$s! Тебе %2$d лет.</string>
</resources>
```

```kotlin
// Использование
val appName = getString(R.string.app_name)
val greeting = getString(R.string.greeting, "Иван", 25)
// "Привет, Иван! Тебе 25 лет."
```

### colors.xml

```xml
<resources>
    <color name="primary">#6200EE</color>
    <color name="primary_dark">#3700B3</color>
    <color name="accent">#03DAC5</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
</resources>
```

```kotlin
// Использование
val color = getColor(R.color.primary)
view.setBackgroundColor(color)
```

### dimens.xml

```xml
<resources>
    <dimen name="padding_small">8dp</dimen>
    <dimen name="padding_medium">16dp</dimen>
    <dimen name="padding_large">24dp</dimen>
    <dimen name="text_size_normal">16sp</dimen>
    <dimen name="text_size_large">20sp</dimen>
</resources>
```

## 🔔 Toast и Snackbar

### Toast (всплывающее сообщение)

```kotlin
// Короткое сообщение
Toast.makeText(this, "Привет!", Toast.LENGTH_SHORT).show()

// Длинное сообщение
Toast.makeText(this, "Это длинное сообщение", Toast.LENGTH_LONG).show()
```

### Snackbar (сообщение с действием)

```kotlin
// Простой Snackbar
Snackbar.make(view, "Файл удален", Snackbar.LENGTH_SHORT).show()

// С действием
Snackbar.make(view, "Файл удален", Snackbar.LENGTH_LONG)
    .setAction("Отменить") {
        // Восстановить файл
        Toast.makeText(this, "Восстановлено", Toast.LENGTH_SHORT).show()
    }
    .show()
```

## 🎯 Intent - Намерения

### Явный Intent (открыть конкретную Activity)

```kotlin
// Просто открыть
val intent = Intent(this, SecondActivity::class.java)
startActivity(intent)

// С данными
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra("ID", 123)
    putExtra("NAME", "Товар")
    putExtra("PRICE", 50000.0)
    putExtra("IS_AVAILABLE", true)
}
startActivity(intent)
```

### Неявный Intent (системные действия)

```kotlin
// Открыть браузер
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
startActivity(intent)

// Позвонить
val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+79991234567"))
startActivity(intent)

// Отправить SMS
val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+79991234567"))
intent.putExtra("sms_body", "Привет!")
startActivity(intent)

// Поделиться текстом
val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "Текст для отправки")
}
startActivity(Intent.createChooser(intent, "Поделиться через"))

// Открыть камеру
val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
startActivity(intent)
```

## 🔧 SharedPreferences (настройки)

```kotlin
// Сохранить данные
val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
prefs.edit().apply {
    putString("username", "Иван")
    putInt("age", 25)
    putBoolean("is_logged_in", true)
    apply()  // Или commit() для синхронного сохранения
}

// Прочитать данные
val username = prefs.getString("username", "Гость")  // "Гость" - значение по умолчанию
val age = prefs.getInt("age", 0)
val isLoggedIn = prefs.getBoolean("is_logged_in", false)

// Удалить данные
prefs.edit().remove("username").apply()

// Очистить все
prefs.edit().clear().apply()
```

## ⚙️ Log - Логирование

```kotlin
import android.util.Log

class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Уровни логирования (от низкого к высокому)
        Log.v(TAG, "Verbose - подробная информация")
        Log.d(TAG, "Debug - отладочная информация")
        Log.i(TAG, "Info - общая информация")
        Log.w(TAG, "Warning - предупреждение")
        Log.e(TAG, "Error - ошибка")
        
        // С исключением
        try {
            // код
        } catch (e: Exception) {
            Log.e(TAG, "Произошла ошибка", e)
        }
    }
}

// Просмотр в Logcat: adb logcat или в Android Studio (Logcat внизу)
```

---

**Совет**: Всегда используй `super.method()` в lifecycle методах!

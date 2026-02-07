# Toggles: включение и отключение модулей

## Система Feature Flags

Если нужно работать над одним модулем, можно отключить остальные через Feature Flags:

```kotlin
// common/FeatureFlags.kt
object FeatureFlags {
    // Modules
    const val ENABLE_AUTHENTICATION = true
    const val ENABLE_EXERCISES = true
    const val ENABLE_GAME = true
    const val ENABLE_LEADERBOARD = true
    const val ENABLE_HARDWARE = true  // Камера, микрофон
    const val ENABLE_NOTIFICATIONS = true
    
    // Advanced
    const val ENABLE_TF_LITE = true
    const val ENABLE_AR_MODE = true
    const val ENABLE_WIDGET = true
}
```

## Использование в коде

### Условное отображение экранов
```kotlin
binding.btnGame.visibility = if (FeatureFlags.ENABLE_GAME) View.VISIBLE else View.GONE

binding.btnExercises.isEnabled = FeatureFlags.ENABLE_EXERCISES
```

### Условное выполнение логики
```kotlin
fun submitExerciseResult(result: ExerciseResult) {
    if (FeatureFlags.ENABLE_EXERCISES) {
        viewModelScope.launch {
            repository.submitResult(result)
        }
    }
}
```

### Вывод в Logcat
```kotlin
if (FeatureFlags.ENABLE_HARDWARE) {
    startRecording()
} else {
    Log.d("FeatureFlags", "Hardware features disabled")
}
```

## Включение/отключение модулей по сессиям

### Сессия 1 (Архитектура + UI + Auth)
```kotlin
object FeatureFlags {
    const val ENABLE_AUTHENTICATION = true      // ✅
    const val ENABLE_EXERCISES = false          // ❌ Оставить на потом
    const val ENABLE_GAME = false               // ❌
    const val ENABLE_LEADERBOARD = false        // ❌
    const val ENABLE_HARDWARE = false           // ❌
    const val ENABLE_NOTIFICATIONS = false      // ❌
    const val ENABLE_TF_LITE = false            // ❌
    const val ENABLE_AR_MODE = false            // ❌
    const val ENABLE_WIDGET = false             // ❌
}
```

### Сессия 2 (Упражнения + Сеть)
```kotlin
object FeatureFlags {
    const val ENABLE_AUTHENTICATION = true      // ✅ (готово)
    const val ENABLE_EXERCISES = true           // ✅ Работаем здесь
    const val ENABLE_GAME = false               // ❌
    const val ENABLE_LEADERBOARD = true         // ✅ Для отображения результатов
    const val ENABLE_HARDWARE = false           // ❌
    const val ENABLE_NOTIFICATIONS = false      // ❌
    const val ENABLE_TF_LITE = true             // ✅ Для угадай животное
    const val ENABLE_AR_MODE = false            // ❌
    const val ENABLE_WIDGET = false             // ❌
}
```

### Сессия 3 (Игра + оставшееся)
```kotlin
object FeatureFlags {
    const val ENABLE_AUTHENTICATION = true      // ✅ (готово)
    const val ENABLE_EXERCISES = true           // ✅ (готово)
    const val ENABLE_GAME = true                // ✅ Работаем здесь
    const val ENABLE_LEADERBOARD = true         // ✅ (готово)
    const val ENABLE_HARDWARE = true            // ✅ Включить камеру/микрофон
    const val ENABLE_NOTIFICATIONS = true       // ✅ Включить уведомления
    const val ENABLE_TF_LITE = true             // ✅ (готово)
    const val ENABLE_AR_MODE = true             // ✅ Финальные штрихи
    const val ENABLE_WIDGET = true              // ✅ Виджет
}
```

## Программное получение значения флага
```kotlin
class FeatureManager {
    fun isModuleEnabled(moduleName: String): Boolean {
        return when (moduleName) {
            "auth" -> FeatureFlags.ENABLE_AUTHENTICATION
            "exercises" -> FeatureFlags.ENABLE_EXERCISES
            "game" -> FeatureFlags.ENABLE_GAME
            "leaderboard" -> FeatureFlags.ENABLE_LEADERBOARD
            "hardware" -> FeatureFlags.ENABLE_HARDWARE
            "notifications" -> FeatureFlags.ENABLE_NOTIFICATIONS
            "tflite" -> FeatureFlags.ENABLE_TF_LITE
            "ar" -> FeatureFlags.ENABLE_AR_MODE
            "widget" -> FeatureFlags.ENABLE_WIDGET
            else -> false
        }
    }
    
    fun logStatus() {
        val enabled = mutableListOf<String>()
        val disabled = mutableListOf<String>()
        
        mapOf(
            "auth" to FeatureFlags.ENABLE_AUTHENTICATION,
            "exercises" to FeatureFlags.ENABLE_EXERCISES,
            "game" to FeatureFlags.ENABLE_GAME,
            "leaderboard" to FeatureFlags.ENABLE_LEADERBOARD,
            "hardware" to FeatureFlags.ENABLE_HARDWARE,
            "notifications" to FeatureFlags.ENABLE_NOTIFICATIONS,
            "tflite" to FeatureFlags.ENABLE_TF_LITE,
            "ar" to FeatureFlags.ENABLE_AR_MODE,
            "widget" to FeatureFlags.ENABLE_WIDGET
        ).forEach { (name, enabled) ->
            if (enabled) enabled.add(name) else disabled.add(name)
        }
        
        Log.d("Features", "Enabled: ${enabled.joinToString()}")
        Log.d("Features", "Disabled: ${disabled.joinToString()}")
    }
}
```

## Альтернатива: Feature Toggles через SharedPreferences
```kotlin
class FeatureToggleManager(context: Context) {
    private val prefs = context.getSharedPreferences("features", Context.MODE_PRIVATE)
    
    fun setFeatureEnabled(feature: String, enabled: Boolean) {
        prefs.edit().putBoolean(feature, enabled).apply()
    }
    
    fun isFeatureEnabled(feature: String): Boolean {
        return prefs.getBoolean(feature, true)  // По умолчанию включены
    }
}

// Использование
val toggleManager = FeatureToggleManager(context)
if (toggleManager.isFeatureEnabled("exercises")) {
    // Загрузить упражнения
}
```

## Таблица модулей с зависимостями
```
Модуль                Зависит от              Нужен с дня
──────────────────────────────────────────────────────────
A. Архитектура        Ничего                  День 1
B. UI                 A                       День 1
C. Сеть               A, B                    День 2
D. Хранение           A, C                    День 2
E. Упражнения         B, C, D                 День 2
F. Игра               B, C, D, E              День 3
G. Аппаратные         B, C, D                 День 3
H. TensorFlow         G (камера)              День 3
I. Уведомления        A                       День 3
J. Виджет             A, D                    День 3
```

---

Это полная структура модулей с возможностью независимого разработки каждого!

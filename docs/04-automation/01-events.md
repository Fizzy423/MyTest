# События приложения

#automation #events #analytics

[Назад к docs/README.md](../README.md)

## Что логируем
- Экран открыт
- Клик по кнопке
- Успех/ошибка упражнения
- Время выполнения

## Пример простого EventLogger
```kotlin
object EventLogger {
    fun log(event: String, data: Map<String, String> = emptyMap()) {
        val payload = buildString {
            append("event=$event")
            data.forEach { (k, v) -> append(" $k=$v") }
        }
        Log.d("Event", payload)
    }
}

// Использование
EventLogger.log("screen_open", mapOf("screen" to "Main"))
```

## Мини‑схема события
```
Event:
- name: screen_open
- timestamp: 2025-01-01T12:00:00
- screen: Main
```

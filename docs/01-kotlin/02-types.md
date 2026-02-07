# Kotlin: Типы данных и переменные

## Числовые типы
```kotlin
val byte: Byte = 127
val short: Short = 32767
val int: Int = 2147483647
val long: Long = 9223372036854775807L

val float: Float = 3.14f
val double: Double = 3.14159
```

## Логический тип
```kotlin
val active: Boolean = true
val inactive: Boolean = false

if (active) {
    println("Активно")
}
```

## Символ и строка
```kotlin
val char: Char = 'A'
val string: String = "Hello"

// Длина строки
val len = string.length    // 5

// Первый символ
val first = string[0]      // 'H'

// Подстрока
val sub = string.substring(0, 2)  // "He"
```

## Преобразование типов
```kotlin
val num = "123".toInt()           // 123
val text = 100.toString()         // "100"

// Безопасное преобразование
val safe = "123a".toIntOrNull()   // null
val withDefault = safe ?: 0       // 0
```

## Константы
```kotlin
const val MAX = 100   // Глобальная константа (compile-time)

// В функции
val localMax = 100    // Локальная константа
```

---

Следующее: [Функции и лямбды](03-functions.md)

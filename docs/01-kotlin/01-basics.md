# Kotlin: Основы синтаксиса

## Переменные: val и var
```kotlin
val name = "Ivan"      // неизменяемая (immutable)
var age = 20           // изменяемая (mutable)

// С явным типом
val id: Int = 123
var score: Double = 95.5
```

**val vs var:** используй `val` по умолчанию, `var` только если нужно менять.

## Базовые типы
```kotlin
val num: Int = 10
val decimal: Double = 3.14
val text: String = "Hello"
val flag: Boolean = true
```

## Строки с интерполяцией
```kotlin
val name = "World"
println("Hello, $name")                    // Hello, World
println("Значение: ${10 + 5}")             // Значение: 15
println("Длина: ${name.length}")           // Длина: 5
```

## Комментарии
```kotlin
// Однострочный комментарий

/* Многострочный
   комментарий */

/** Документация
    @param x первое число
    @return сумма
*/
```

## Простая программа
```kotlin
fun main() {
    val greeting = "Hi!"
    println(greeting)
}
```

---

Следующее: [Типы данных](02-types.md)

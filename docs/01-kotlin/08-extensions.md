# Kotlin: Расширения и scope‑функции

## Extension функции
Добавить метод к существующему классу:

```kotlin
// Расширить String
fun String.isValidEmail(): Boolean {
    return this.contains("@")
}

val email = "user@example.com"
println(email.isValidEmail())  // true
```

## Scope‑функции

| Функция | Доступ | Возвращает | Для чего |
|---------|--------|-----------|---------|
| **apply** | `this` | объект | Инициализация |
| **let** | `it` | результат | Проверка null |
| **run** | `this` | результат | Вычисления |
| **with** | `this` | результат | Множественные вызовы |
| **also** | `it` | объект | Логирование |

### apply — инициализация
```kotlin
val person = Person().apply {
    name = "Alice"
    age = 30
    email = "alice@example.com"
}
```

### let — проверка null
```kotlin
val name: String? = "Bob"
name?.let {
    println("Name length: ${it.length}")
}
```

### run — вычисления
```kotlin
val result = "hello".run {
    this.uppercase() + "!"
}
println(result)  // HELLO!
```

### with — множественные вызовы
```kotlin
val list = mutableListOf(1, 2, 3)
with(list) {
    add(4)
    remove(1)
    println(this)  // [2, 3, 4]
}
```

### also — логирование
```kotlin
val number = 5.also {
    println("Value is $it")
}.times(2)
```

---

Это основы Kotlin. Для Android смотри: `../02-competencies/`

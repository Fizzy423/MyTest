# Kotlin: Null‑safety

## Nullable типы
```kotlin
val name: String? = "Alice"  // может быть null
val age: String? = null

val nonNull: String = "Bob"  // не может быть null
```

## Безопасный доступ (?.)
```kotlin
val text: String? = "Hello"
val len = text?.length  // 5 или null

val empty: String? = null
val len2 = empty?.length  // null (без ошибки!)
```

## Elvis оператор (?:)
```kotlin
val name: String? = null
val displayName = name ?: "Guest"  // "Guest"

val age: Int? = null
val years = age ?: 0  // 0
```

## Комбинация: ?. и ?:
```kotlin
val user: User? = null
val email = user?.email ?: "no@email.com"

// Если user null → "no@email.com"
// Если user не null, но email null → "no@email.com"
```

## Опасный доступ (!!)
```kotlin
val text: String? = "Hello"
val len = text!!.length  // Работает, но опасно!

val empty: String? = null
val len2 = empty!!.length  // Падает! NullPointerException
```

**Совет:** используй `!!` только если **100% уверен**, что значение не null.

## Проверка перед использованием
```kotlin
val value: String? = "test"

if (value != null) {
    println(value.length)  // value уже не null
}

// Или let
value?.let {
    println(it.length)
}
```

---

Следующее: [Управляющие конструкции](07-control.md)

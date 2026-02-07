# Kotlin: Коллекции

## Списки
```kotlin
// Неизменяемый список
val numbers = listOf(1, 2, 3, 4, 5)

// Изменяемый список
val mutable = mutableListOf(1, 2, 3)
mutable.add(4)
mutable.remove(1)

// Доступ
val first = numbers[0]      // 1
val last = numbers.last()   // 5
```

## Операции со списками
```kotlin
val list = listOf(1, 2, 3, 4, 5)

list.map { it * 2 }              // [2, 4, 6, 8, 10]
list.filter { it > 2 }           // [3, 4, 5]
list.find { it == 3 }            // 3
list.any { it > 4 }              // true
list.all { it > 0 }              // true
list.sorted()                    // [1, 2, 3, 4, 5]
list.sortedByDescending { it }   // [5, 4, 3, 2, 1]
```

## Множества (Set)
```kotlin
val set = setOf(1, 2, 2, 3, 3, 3)
println(set)              // [1, 2, 3] - дубликаты удалены

val mutableSet = mutableSetOf("a", "b")
mutableSet.add("c")
```

## Словари (Map)
```kotlin
val map = mapOf("a" to 1, "b" to 2, "c" to 3)

// Получить значение
val value = map["a"]      // 1

// Если ключа нет
val safe = map["x"] ?: 0  // 0

// Итерация
for ((key, value) in map) {
    println("$key -> $value")
}

// Изменяемый
val mutableMap = mutableMapOf<String, Int>()
mutableMap["a"] = 10
```

## Группировка
```kotlin
val words = listOf("a", "bb", "ccc", "dd")
val byLength = words.groupBy { it.length }
// {1=[a], 2=[bb, dd], 3=[ccc]}
```

## Преобразование
```kotlin
val list = listOf(1, 2, 3)
val set = list.toSet()      // {1, 2, 3}
val str = list.joinToString(", ")  // "1, 2, 3"
```

---

Следующее: [Null‑safety](06-nullsafety.md)

# Kotlin: Управляющие конструкции

## If‑else
```kotlin
val age = 20

if (age >= 18) {
    println("Adult")
} else {
    println("Minor")
}

// If как выражение
val status = if (age >= 18) "Adult" else "Minor"
```

## When (вместо switch)
```kotlin
val day = 3

val name = when (day) {
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    else -> "Other"
}

println(name)  // Wednesday
```

## When с диапазонами
```kotlin
val score = 75

val grade = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    in 70..79 -> "C"
    else -> "F"
}
```

## When с типами
```kotlin
val value: Any = "Hello"

when (value) {
    is String -> println("Length: ${value.length}")
    is Int -> println("Number: $value")
    else -> println("Other")
}
```

## For цикл
```kotlin
for (i in 1..5) {
    println(i)  // 1, 2, 3, 4, 5
}

for (i in 1 until 5) {
    println(i)  // 1, 2, 3, 4
}

for (i in 5 downTo 1) {
    println(i)  // 5, 4, 3, 2, 1
}

// По коллекции
val items = listOf("a", "b", "c")
for (item in items) {
    println(item)
}
```

## While цикл
```kotlin
var count = 0
while (count < 5) {
    println(count)
    count++
}
```

## Break и continue
```kotlin
for (i in 1..10) {
    if (i == 3) continue  // пропустить
    if (i == 7) break     // выход
    println(i)
}
```

---

Следующее: [Расширения и scope‑функции](08-extensions.md)

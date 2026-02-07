# Kotlin: Функции и лямбды

## Функция (обычная)
```kotlin
fun greet(name: String): String {
    return "Hello, $name"
}

val result = greet("World")  // "Hello, World"
```

## Однострочная функция
```kotlin
fun sum(a: Int, b: Int) = a + b

val total = sum(5, 3)  // 8
```

## Функция без возврата
```kotlin
fun printMessage(msg: String) {
    println(msg)  // Unit (аналог void)
}
```

## Параметры по умолчанию
```kotlin
fun greet(name: String = "Guest") = "Hello, $name"

greet()           // "Hello, Guest"
greet("Alice")    // "Hello, Alice"
```

## Лямбды (анонимные функции)
```kotlin
val add = { a: Int, b: Int -> a + b }
val result = add(10, 5)  // 15

// С одним параметром — используется 'it'
val double = { x: Int -> x * 2 }
val ten = double(5)  // 10
```

## Функция, принимающая функцию
```kotlin
fun applyTwice(x: Int, f: (Int) -> Int): Int {
    return f(f(x))
}

val squared = { n: Int -> n * n }
val result = applyTwice(2, squared)  // 16
```

## Лямбды с коллекциями
```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// map — преобразовать каждый элемент
val doubled = numbers.map { it * 2 }      // [2, 4, 6, 8, 10]

// filter — оставить только подходящие
val evens = numbers.filter { it % 2 == 0 }  // [2, 4]

// find — найти первый
val first = numbers.find { it > 2 }       // 3
```

---

Следующее: [Классы и объекты](04-classes.md)

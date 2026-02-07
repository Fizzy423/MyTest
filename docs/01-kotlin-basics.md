# 📘 Kotlin - Основы синтаксиса

## 🔤 Переменные

```kotlin
// val - неизменяемая (как final в Java)
val name: String = "Иван"
val age = 20  // тип выводится автоматически

// var - изменяемая
var count = 0
count = 5

// Nullable типы (может быть null)
var nullableName: String? = null
nullableName = "Петр"

// Non-null (не может быть null)
val nonNullName: String = "Мария"
// nonNullName = null  // ОШИБКА!
```

## 🎯 Типы данных

```kotlin
// Числа
val int: Int = 42
val long: Long = 42L
val double: Double = 3.14
val float: Float = 3.14f

// Строки
val text: String = "Привет"
val multiline = """
    Многострочный
    текст
""".trimIndent()

// Строковые шаблоны
val userName = "Алексей"
val greeting = "Привет, $userName!"
val info = "У меня ${count + 5} яблок"

// Boolean
val isActive: Boolean = true
val isCompleted = false

// Char
val letter: Char = 'A'
```

## 📦 Функции

```kotlin
// Обычная функция
fun sum(a: Int, b: Int): Int {
    return a + b
}

// Однострочная функция
fun multiply(a: Int, b: Int): Int = a * b

// Функция без возврата (Unit = void в Java)
fun printMessage(message: String) {
    println(message)
}

// Значения по умолчанию
fun greet(name: String = "Гость", age: Int = 18) {
    println("Привет, $name! Тебе $age лет.")
}

// Вызов
greet()                    // Привет, Гость! Тебе 18 лет.
greet("Иван")              // Привет, Иван! Тебе 18 лет.
greet("Мария", 25)         // Привет, Мария! Тебе 25 лет.
greet(age = 30, name = "Петр")  // Именованные параметры
```

## 🔀 Условия

```kotlin
// if-else как выражение
val max = if (a > b) a else b

// Традиционный if
if (score >= 90) {
    println("Отлично!")
} else if (score >= 70) {
    println("Хорошо")
} else {
    println("Старайся лучше")
}

// when (switch в Java)
when (x) {
    1 -> println("Один")
    2 -> println("Два")
    in 3..10 -> println("От 3 до 10")
    else -> println("Другое")
}

// when как выражение
val result = when (grade) {
    "A", "B" -> "Отлично"
    "C" -> "Хорошо"
    "D" -> "Удовлетворительно"
    else -> "Неудовлетворительно"
}

// when без аргумента
when {
    age < 18 -> println("Несовершеннолетний")
    age < 65 -> println("Взрослый")
    else -> println("Пенсионер")
}
```

## 🔄 Циклы

```kotlin
// for по диапазону
for (i in 1..5) {
    println(i)  // 1, 2, 3, 4, 5
}

// for с шагом
for (i in 1..10 step 2) {
    println(i)  // 1, 3, 5, 7, 9
}

// for в обратном порядке
for (i in 5 downTo 1) {
    println(i)  // 5, 4, 3, 2, 1
}

// for по списку
val names = listOf("Иван", "Мария", "Петр")
for (name in names) {
    println(name)
}

// for с индексом
for ((index, name) in names.withIndex()) {
    println("$index: $name")
}

// while
var count = 0
while (count < 5) {
    println(count)
    count++
}

// do-while
do {
    println("Выполнится хотя бы раз")
} while (false)
```

## 🏗️ Классы

```kotlin
// Простой класс
class Person {
    var name: String = ""
    var age: Int = 0
}

// Использование
val person = Person()
person.name = "Иван"
person.age = 25

// Класс с конструктором
class User(val name: String, var age: Int) {
    // Инициализация
    init {
        println("Создан пользователь $name")
    }
    
    // Методы
    fun introduce() {
        println("Меня зовут $name, мне $age лет")
    }
}

val user = User("Мария", 22)
user.introduce()

// Data class (для моделей данных)
data class Product(
    val id: Int,
    val name: String,
    val price: Double
)

// Автоматически создаются: equals(), hashCode(), toString(), copy()
val product = Product(1, "Телефон", 50000.0)
println(product)  // Product(id=1, name=Телефон, price=50000.0)
val copy = product.copy(price = 45000.0)
```

## 📋 Null Safety

```kotlin
// Безопасный вызов ?.
var name: String? = null
println(name?.length)  // null, а не ошибка

// Elvis оператор ?:
val length = name?.length ?: 0  // Если null, то 0

// Not-null assertion !!
val definitelyNotNull = name!!.length  // Бросит NullPointerException если null

// let для работы с nullable
name?.let {
    println("Имя: $it")  // Выполнится только если name не null
}

// Безопасное приведение типов
val text = value as? String  // null если не String
```

## 🎨 Лямбды и функции высшего порядка

```kotlin
// Лямбда
val sum = { a: Int, b: Int -> a + b }
println(sum(5, 3))  // 8

// Функция как параметр
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

val result = calculate(10, 5) { x, y -> x + y }  // 15

// it для одного параметра
val numbers = listOf(1, 2, 3, 4, 5)
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]
```

## 📚 Полезные extension функции

```kotlin
// String
"hello".capitalize()           // "Hello" (deprecated, используй replaceFirstChar)
"  text  ".trim()             // "text"
"test".startsWith("te")       // true
"test".contains("es")         // true

// Числа
42.toString()                 // "42"
"42".toInt()                  // 42
"3.14".toDoubleOrNull()       // 3.14 (или null если ошибка)

// Коллекции
listOf(1, 2, 3).size          // 3
listOf(1, 2, 3).isEmpty()     // false
listOf(1, 2, 3).first()       // 1
listOf(1, 2, 3).last()        // 3
```

## ⚠️ Частые ошибки

### Java → Kotlin различия

```kotlin
// ❌ Java стиль
String name = "Иван";
System.out.println(name);

// ✅ Kotlin стиль
val name = "Иван"
println(name)

// ❌ Getter/Setter в Java
person.setName("Иван");
String name = person.getName();

// ✅ Properties в Kotlin
person.name = "Иван"
val name = person.name

// ❌ new в Java
Person person = new Person();

// ✅ Без new в Kotlin
val person = Person()

// ❌ Точка с запятой
val x = 5;  // Не нужна!

// ✅ Без точки с запятой
val x = 5
```

---

**Совет**: Kotlin более лаконичный чем Java, но основные концепции те же!

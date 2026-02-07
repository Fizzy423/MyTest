# 📦 Kotlin - Коллекции и функции высшего порядка

## 📝 Типы коллекций

### List (список)

```kotlin
// Неизменяемый список (read-only)
val numbers = listOf(1, 2, 3, 4, 5)
val names = listOf("Иван", "Мария", "Петр")
val empty = emptyList<String>()

// Изменяемый список (mutable)
val mutableNumbers = mutableListOf(1, 2, 3)
mutableNumbers.add(4)
mutableNumbers.remove(2)
mutableNumbers[0] = 10

// ArrayList (тот же MutableList)
val arrayList = ArrayList<String>()
arrayList.add("первый")
```

### Set (множество - без дубликатов)

```kotlin
// Неизменяемое множество
val uniqueNumbers = setOf(1, 2, 3, 2, 1)  // [1, 2, 3]

// Изменяемое множество
val mutableSet = mutableSetOf("A", "B", "C")
mutableSet.add("D")
mutableSet.remove("A")
```

### Map (словарь ключ-значение)

```kotlin
// Неизменяемая карта
val ages = mapOf(
    "Иван" to 25,
    "Мария" to 22,
    "Петр" to 30
)

// Получение значения
val age = ages["Иван"]  // 25 (или null если нет)
val ageOrDefault = ages.getOrDefault("Света", 0)  // 0

// Изменяемая карта
val mutableMap = mutableMapOf<String, Int>()
mutableMap["Иван"] = 25
mutableMap.put("Мария", 22)
mutableMap.remove("Иван")

// Перебор
for ((name, age) in ages) {
    println("$name: $age лет")
}
```

### Array (массив)

```kotlin
// Массив
val array = arrayOf(1, 2, 3, 4, 5)
val arrayOfNulls = arrayOfNulls<String>(5)
val intArray = intArrayOf(1, 2, 3)  // Примитивный массив

// Доступ
val first = array[0]
array[0] = 10
```

## 🔧 Операции с коллекциями

### filter - Фильтрация

```kotlin
val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

// Только четные
val even = numbers.filter { it % 2 == 0 }  // [2, 4, 6, 8, 10]

// Больше 5
val moreThan5 = numbers.filter { it > 5 }  // [6, 7, 8, 9, 10]

// Фильтр по типу
val mixed = listOf(1, "text", 2, "hello", 3)
val onlyInts = mixed.filterIsInstance<Int>()  // [1, 2, 3]

// filterNot (обратный фильтр)
val odd = numbers.filterNot { it % 2 == 0 }  // [1, 3, 5, 7, 9]
```

### map - Преобразование

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Умножить на 2
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

// Строки
val names = listOf("иван", "мария", "петр")
val capitalized = names.map { it.capitalize() }  // [Иван, Мария, Петр]

// Сложное преобразование
data class Person(val name: String, val age: Int)
val people = listOf(
    Person("Иван", 25),
    Person("Мария", 22)
)
val ages = people.map { it.age }  // [25, 22]

// mapNotNull - игнорирует null
val strings = listOf("1", "2", "abc", "3")
val ints = strings.mapNotNull { it.toIntOrNull() }  // [1, 2, 3]
```

### forEach - Перебор

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Простой перебор
numbers.forEach { println(it) }

// С индексом
numbers.forEachIndexed { index, value ->
    println("$index: $value")
}
```

### find / first / last

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Найти первый элемент
val first = numbers.first()  // 1
val last = numbers.last()    // 5

// Найти по условию
val firstEven = numbers.first { it % 2 == 0 }  // 2
val firstOdd = numbers.find { it % 2 != 0 }     // 1 (то же что first)

// firstOrNull - безопасная версия
val firstLarge = numbers.firstOrNull { it > 10 }  // null

// last по условию
val lastEven = numbers.last { it % 2 == 0 }  // 4
```

### any / all / none

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Есть ли хотя бы один четный?
val hasEven = numbers.any { it % 2 == 0 }  // true

// Все четные?
val allEven = numbers.all { it % 2 == 0 }  // false

// Нет четных?
val noEven = numbers.none { it % 2 == 0 }  // false

// Содержит элемент?
val contains = numbers.contains(3)  // true
val contains2 = 3 in numbers        // true (то же самое)
```

### sum / count / min / max

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Сумма
val sum = numbers.sum()  // 15

// Количество
val count = numbers.count()  // 5
val evenCount = numbers.count { it % 2 == 0 }  // 2

// Минимум и максимум
val min = numbers.minOrNull()  // 1
val max = numbers.maxOrNull()  // 5

// Среднее
val average = numbers.average()  // 3.0
```

### sorted / sortedBy

```kotlin
val numbers = listOf(5, 2, 8, 1, 9)

// Сортировка
val sorted = numbers.sorted()  // [1, 2, 5, 8, 9]
val sortedDesc = numbers.sortedDescending()  // [9, 8, 5, 2, 1]

// Сортировка объектов
data class Person(val name: String, val age: Int)
val people = listOf(
    Person("Иван", 25),
    Person("Мария", 22),
    Person("Петр", 30)
)

val sortedByAge = people.sortedBy { it.age }
// [Person(Мария, 22), Person(Иван, 25), Person(Петр, 30)]

val sortedByName = people.sortedBy { it.name }
```

### groupBy

```kotlin
data class Student(val name: String, val grade: Int)
val students = listOf(
    Student("Иван", 5),
    Student("Мария", 4),
    Student("Петр", 5),
    Student("Анна", 4)
)

// Группировка по оценке
val byGrade = students.groupBy { it.grade }
// {5=[Student(Иван, 5), Student(Петр, 5)], 4=[Student(Мария, 4), Student(Анна, 4)]}

// Группировка строк по длине
val words = listOf("a", "ab", "abc", "abcd", "ab")
val byLength = words.groupBy { it.length }
// {1=[a], 2=[ab, ab], 3=[abc], 4=[abcd]}
```

### distinct / distinctBy

```kotlin
val numbers = listOf(1, 2, 2, 3, 3, 3, 4, 5, 5)
val unique = numbers.distinct()  // [1, 2, 3, 4, 5]

// Уникальные по свойству
data class Person(val name: String, val age: Int)
val people = listOf(
    Person("Иван", 25),
    Person("Мария", 22),
    Person("Иван", 30)  // Тот же Иван, но другой возраст
)
val uniqueNames = people.distinctBy { it.name }
// [Person(Иван, 25), Person(Мария, 22)]
```

### take / drop

```kotlin
val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

// Взять первые N
val first3 = numbers.take(3)  // [1, 2, 3]

// Взять последние N
val last3 = numbers.takeLast(3)  // [8, 9, 10]

// Пропустить первые N
val without3 = numbers.drop(3)  // [4, 5, 6, 7, 8, 9, 10]

// takeWhile / dropWhile (по условию)
val takeLessThan5 = numbers.takeWhile { it < 5 }  // [1, 2, 3, 4]
val dropLessThan5 = numbers.dropWhile { it < 5 }  // [5, 6, 7, 8, 9, 10]
```

### partition

```kotlin
val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

// Разделить на две группы
val (even, odd) = numbers.partition { it % 2 == 0 }
// even = [2, 4, 6, 8, 10]
// odd = [1, 3, 5, 7, 9]
```

### zip

```kotlin
val names = listOf("Иван", "Мария", "Петр")
val ages = listOf(25, 22, 30)

// Объединить две коллекции
val pairs = names.zip(ages)
// [(Иван, 25), (Мария, 22), (Петр, 30)]

// С преобразованием
val formatted = names.zip(ages) { name, age -> "$name: $age лет" }
// [Иван: 25 лет, Мария: 22 лет, Петр: 30 лет]
```

### flatten / flatMap

```kotlin
// flatten - "разглаживание" списка списков
val lists = listOf(
    listOf(1, 2, 3),
    listOf(4, 5),
    listOf(6, 7, 8, 9)
)
val flat = lists.flatten()  // [1, 2, 3, 4, 5, 6, 7, 8, 9]

// flatMap - map + flatten
val numbers = listOf(1, 2, 3)
val result = numbers.flatMap { listOf(it, it * 10) }
// [1, 10, 2, 20, 3, 30]
```

## 🎯 Цепочки операций (Chaining)

```kotlin
data class Product(val name: String, val price: Double, val category: String)

val products = listOf(
    Product("Телефон", 50000.0, "Электроника"),
    Product("Ноутбук", 80000.0, "Электроника"),
    Product("Футболка", 1500.0, "Одежда"),
    Product("Джинсы", 3000.0, "Одежда"),
    Product("Планшет", 30000.0, "Электроника")
)

// Сложная цепочка
val result = products
    .filter { it.category == "Электроника" }  // Только электроника
    .filter { it.price < 60000 }              // Дешевле 60000
    .sortedBy { it.price }                    // Сортировать по цене
    .map { it.name }                          // Только названия
    .take(2)                                  // Первые 2

println(result)  // [Планшет, Телефон]

// Средняя цена электроники
val avgPrice = products
    .filter { it.category == "Электроника" }
    .map { it.price }
    .average()

println(avgPrice)  // 53333.33
```

## 🔥 Sequence для больших коллекций

```kotlin
// Обычные операции - создают промежуточные списки
val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
val result = numbers
    .filter { it % 2 == 0 }   // Создает новый список
    .map { it * 2 }           // Создает еще один список
    .take(3)                  // И еще один

// Sequence - ленивые вычисления (как Stream в Java)
val result2 = numbers.asSequence()
    .filter { it % 2 == 0 }   // Не создает список
    .map { it * 2 }           // Не создает список
    .take(3)                  // Не создает список
    .toList()                 // Только здесь происходят вычисления

// Используй sequence для больших данных (1000+ элементов)
```

## 💡 Полезные примеры для Android

### Фильтрация списка продуктов

```kotlin
data class Product(val id: Int, val name: String, val price: Double)

val products = listOf(
    Product(1, "Телефон", 50000.0),
    Product(2, "Ноутбук", 80000.0),
    Product(3, "Мышка", 500.0)
)

// Поиск по имени
fun searchProducts(query: String): List<Product> {
    return products.filter { 
        it.name.contains(query, ignoreCase = true) 
    }
}

// Сортировка
val sortedByPrice = products.sortedBy { it.price }
val sortedByName = products.sortedBy { it.name }
```

### Преобразование для RecyclerView

```kotlin
// Из сети в UI модель
data class NetworkProduct(val id: Int, val title: String, val cost: Double)
data class UiProduct(val name: String, val formattedPrice: String)

val networkProducts = listOf(
    NetworkProduct(1, "Телефон", 50000.0),
    NetworkProduct(2, "Ноутбук", 80000.0)
)

val uiProducts = networkProducts.map { 
    UiProduct(
        name = it.title,
        formattedPrice = "${it.cost} ₽"
    )
}
```

---

**Совет**: Все эти функции работают одинаково для List, Set, Array!

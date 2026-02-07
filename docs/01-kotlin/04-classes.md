# Kotlin: Классы и объекты

## Простой класс
```kotlin
class Person(val name: String, var age: Int) {
    fun greet() {
        println("Hi, I'm $name")
    }
}

val person = Person("Alice", 25)
person.greet()  // Hi, I'm Alice
```

## Data class
```kotlin
data class User(val id: Int, val name: String, val email: String)

val user = User(1, "Bob", "bob@example.com")

// Автоматические методы:
println(user)           // User(id=1, name=Bob, email=bob@example.com)
println(user.copy(id = 2))  // User(id=2, name=Bob, email=bob@example.com)
```

## Наследование
```kotlin
open class Animal(val name: String) {
    fun sound() {
        println("Some sound")
    }
}

class Dog(name: String) : Animal(name) {
    override fun sound() {
        println("Woof!")
    }
}

val dog = Dog("Buddy")
dog.sound()  // Woof!
```

## Интерфейс
```kotlin
interface Drawable {
    fun draw()
}

class Circle : Drawable {
    override fun draw() {
        println("Drawing circle")
    }
}
```

## Getter и setter
```kotlin
class Account {
    var balance: Double = 0.0
        set(value) {
            if (value >= 0) field = value
        }

    val isActive: Boolean
        get() = balance > 0
}
```

## Object (singleton)
```kotlin
object Config {
    val apiUrl = "https://api.example.com"
    val timeout = 5000
}

println(Config.apiUrl)
```

---

Следующее: [Коллекции](05-collections.md)

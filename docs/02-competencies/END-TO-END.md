# Инструкция от А до Я: как написать приложение по заданию

Ниже — пошаговый план разработки, с короткими пояснениями и мини‑примерами.

## Шаг 0. Подготовка проекта
1. Открыть проект в Android Studio.
2. Проверить JDK 11+ (см. `PROJECT-ISSUES.md`).
3. Синхронизировать Gradle.

## Шаг 1. Архитектура и структура
Создай папки:
```
app/src/main/java/com/example/mytest/
  data/          // API, БД, репозитории
  domain/        // модели данных (data class)
  ui/            // экраны, адаптеры
```

Мини‑пояснение:
- `domain` хранит модели (например `User`, `Exercise`).
- `data` отвечает за получение данных (сеть/БД).
- `ui` показывает данные и ловит клики.

## Шаг 2. Модели данных
Создай модели под сервер:
```kotlin
data class User(val id: Int, val name: String, val avatarUrl: String)
```

## Шаг 3. Верстка экранов (UI)
Сначала сделай базовые экраны по макету:
- Splash
- Onboarding
- Language select
- Sign Up / Log In
- Main screen
- Profile

Пример: экран с кнопкой и списком
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvItems"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Шаг 4. ViewBinding
Подключи ViewBinding и используй его в Activity:
```kotlin
binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)
```

## Шаг 5. Список (RecyclerView)
Создай адаптер:
```kotlin
class ProductAdapter : RecyclerView.Adapter<ProductAdapter.VH>() {
    var items: List<Product> = emptyList()
    // ...
}
```

Связка:
- `Adapter` берёт `items` и рисует в `RecyclerView`.
- Экран обновляет `adapter.items`.

## Шаг 6. Сеть (Retrofit)
Создай API интерфейс:
```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}
```

Где используем:
- Sign Up / Log In → запросы на сервер.
- Main screen → загрузка профиля, лидерборда, упражнений.

## Шаг 7. Хранение (Room/SharedPreferences)
Пример для языка:
```kotlin
val prefs = getSharedPreferences("app", MODE_PRIVATE)
prefs.edit().putString("lang", "en").apply()
```

## Шаг 8. Упражнения
- **Угадай животное**: сервер выдаёт изображение → пользователь вводит слово → сравнить с распознаванием.
- **Выбери вариант**: сервер отдаёт слово → 4 кнопки → подсветить верный.
- **Аудирование**: слово + транскрипция → записать голос → сравнить текст.

## Шаг 9. Аппаратные функции
- Камера/галерея → выбор фото.
- Микрофон → запись.
- Уведомление через NotificationManager.

Мини‑пример запроса разрешения:
```kotlin
ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
```

## Шаг 10. Игровой режим
- Создать комнату.
- Ждать второго игрока 10 сек.
- Если нет — бот с шансом победы 20%.
- Одинаковые слова для обоих.

## Шаг 11. Тестирование (если модуль Е)
TDD:
- RED → тесты падают.
- GREEN → реализация.
- REFACTOR → улучшение.

## Шаг 12. Подготовка продукта
- Презентация: архитектура, классы, производительность.
- Ветка `Path-X`, merge в `main`.

---

Это базовый маршрут. Для деталей смотри:
- `05-recyclerview.md`
- `07-retrofit-network.md`
- `08-coroutines.md`
- `10-code-templates.md`

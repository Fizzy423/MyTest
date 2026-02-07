# Обработка ошибок

#automation #errors #handling

[Назад к docs/README.md](../README.md)

## Типовые ошибки
- Нет интернета
- Ошибка сервера
- Неверный ввод

## Шаблон обработчика
```kotlin
sealed class AppError(val message: String) {
    class Network : AppError("Нет интернета")
    class Server : AppError("Ошибка сервера")
    class Validation : AppError("Некорректные данные")
}

fun handleError(error: AppError) {
    AlertDialog.Builder(context)
        .setTitle("Ошибка")
        .setMessage(error.message)
        .setPositiveButton("OK", null)
        .show()
}
```

## Пример
```kotlin
try {
    api.getUsers()
} catch (e: IOException) {
    handleError(AppError.Network())
}
```

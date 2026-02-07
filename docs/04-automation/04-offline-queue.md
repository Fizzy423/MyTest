# Офлайн‑очередь и ретраи

#automation #offline #retry

[Назад к docs/README.md](../README.md)

## Зачем
Если нет интернета — сохраняем запрос в очередь и отправляем позже.

## Пример очереди
```kotlin
data class PendingRequest(
    val url: String,
    val body: String,
    val method: String
)

class OfflineQueue(private val context: Context) {
    private val prefs = context.getSharedPreferences("queue", Context.MODE_PRIVATE)

    fun enqueue(request: PendingRequest) {
        val list = getAll().toMutableList()
        list.add(request)
        save(list)
    }

    fun getAll(): List<PendingRequest> {
        val json = prefs.getString("requests", "[]") ?: "[]"
        return Gson().fromJson(json, Array<PendingRequest>::class.java).toList()
    }

    fun clear() {
        prefs.edit().remove("requests").apply()
    }

    private fun save(list: List<PendingRequest>) {
        prefs.edit().putString("requests", Gson().toJson(list)).apply()
    }
}

// Использование
if (!isNetworkAvailable()) {
    queue.enqueue(PendingRequest("/results", body, "POST"))
} else {
    sendNow()
}
```

## Повторная отправка
```kotlin
fun syncQueue() {
    if (!isNetworkAvailable()) return
    val pending = queue.getAll()
    pending.forEach { sendRequest(it) }
    queue.clear()
}
```

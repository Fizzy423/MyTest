# Crash‑handler

#automation #crash #stability

[Назад к docs/README.md](../README.md)

## Простой обработчик падений
```kotlin
class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        val error = "${ex.message}\n${Log.getStackTraceString(ex)}"
        saveToFile(error)
        // Можно показать сообщение или перезапустить
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun saveToFile(text: String) {
        val file = File(context.filesDir, "crash.log")
        file.appendText(text + "\n\n")
    }
}

// В Application
Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
```

## Где смотреть
- `files/crash.log`
- Logcat

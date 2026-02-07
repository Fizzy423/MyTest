# 🔧 Проблемы проекта и их решения

## ❌ Текущая проблема

### Ошибка при запуске
```
Could not resolve com.android.tools.build:gradle:8.2.0
Required Java 11, but found Java 8
```

### Причина
- Установлена Java 8 (OpenJDK 1.8.0)
- Android Gradle Plugin 8.2.0 требует Java 11+
- Gradle 8.7 также требует Java 11+

## ✅ Решение 1: Установить Java 11+ (Рекомендуется)

### Вариант A: Через Android Studio
1. Открыть Android Studio
2. File → Settings → Build, Execution, Deployment → Build Tools → Gradle
3. Gradle JDK → выбрать JDK 11 или 17
4. Если нет в списке: Download JDK → выбрать JDK 17

### Вариант B: Установить вручную
1. Скачать JDK 17: https://adoptium.net/
2. Установить
3. В Android Studio: File → Project Structure → SDK Location → JDK Location
4. Выбрать путь к установленному JDK

### Вариант C: Использовать JDK из Android Studio
Android Studio обычно идёт со встроенным JDK:
- Windows: `C:\Program Files\Android\Android Studio\jbr`
- Установить JAVA_HOME на этот путь

## ✅ Решение 2: Понизить версии (Временное)

Если нет возможности обновить Java, можно понизить версии в проекте:

### 1. build.gradle.kts (корневой)
```kotlin
plugins {
    id("com.android.application") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("org.jetbrains.kotlin.kapt") version "1.8.22" apply false
}
```

### 2. gradle/wrapper/gradle-wrapper.properties
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
```

### 3. gradle.properties
Добавить:
```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

## 🎯 Проверка Java после установки

```bash
# Проверить версию
java -version

# Должно показать Java 11 или выше:
# openjdk version "17.0.x"
```

## 📱 Запуск проекта после исправления

### Через Android Studio
1. File → Open → выбрать папку MyTest
2. Дождаться синхронизации Gradle
3. Run → Run 'app'

### Через командную строку
```bash
cd C:\Users\LifeCoreLoopExe\Documents\GitHub\MyTest
.\gradlew.bat assembleDebug
```

## 🔍 Дополнительная диагностика

### Проверить какая Java используется Gradle
```bash
.\gradlew.bat --version
```

### Очистить кэш Gradle
```bash
.\gradlew.bat clean
.\gradlew.bat --stop
```

### Пересобрать проект
```bash
.\gradlew.bat clean build
```

## 💡 Для конкурса

### Что нужно проверить заранее:
- [ ] Android Studio установлена и запускается
- [ ] JDK 11+ настроена
- [ ] Эмулятор создан и работает
- [ ] Тестовый проект создаётся и запускается
- [ ] Gradle синхронизируется без ошибок

### Если на конкурсе будет такая же проблема:
1. Используй 10 минут интернета чтобы скачать JDK 17
2. Или используй встроенный JDK из Android Studio
3. Или создай новый проект с настройками по умолчанию

## 📞 Быстрая помощь

Если ничего не помогает - создай НОВЫЙ проект:
1. File → New → New Project
2. Empty Views Activity
3. Название: MyApp
4. Package: com.example.myapp
5. Language: Kotlin
6. Minimum SDK: API 24
7. Finish

Новый проект создастся с правильными версиями для твоей системы!
